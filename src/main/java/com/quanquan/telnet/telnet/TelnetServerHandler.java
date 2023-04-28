package com.quanquan.telnet.telnet;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.tools.shell.util.CommandArgumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.beans.Introspector;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * @Author qiupengjun
 * @Date 2022 06 23 15 10
 **/
@Component
@Qualifier("springTelnetServerHandler")
@ChannelHandler.Sharable
public class TelnetServerHandler extends ChannelInboundHandlerAdapter {
    protected final Logger log =  LoggerFactory.getLogger(this.getClass());
    public final static String VERSION = "v1.1.0";
    private final ConcurrentLinkedQueue<String> history = new ConcurrentLinkedQueue<>();
    @Autowired(required = false)
    @Qualifier("backConsole")
    private Object consoleHandler;

    private final SpringBeanUtil springBeanUtil;
    private final int corePoolSize = 2;
    private final int maxPoolSize = 8;
    private final int maxQueueLength = 50;
    private final ThreadPoolExecutor asyncPool = new ThreadPoolExecutor(
            corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(maxQueueLength), new ThreadPoolExecutor.CallerRunsPolicy()
    );
    private Map<String,Object> saveObject = new HashMap<>();
    public TelnetServerHandler(SpringBeanUtil springBeanUtil) {
        this.springBeanUtil = springBeanUtil;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        InetSocketAddress inetSocketAddress = channel.remoteAddress();
        String clientIP = inetSocketAddress.getAddress().getHostAddress();
        ctx.writeAndFlush("hello~your connect ip is " + clientIP + " \r\n");
        ctx.writeAndFlush("and mine version si " + VERSION + " \r\n");
        ctx.writeAndFlush("try > hello\r\n");
        ctx.writeAndFlush("> ");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            dealWithChannel(ctx, msg);
        } catch (Exception e) {
            log.error("telnet channelRead error", e);
            ctx.writeAndFlush(">  error msg:" + getStackTrace(e) + "\r\n");
            ctx.writeAndFlush("> ");
        }
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }

    public void dealWithChannel(ChannelHandlerContext ctx, Object msg) throws Exception {
        String command = (String) msg;
        if (StringUtils.isEmpty(command)) {
            ctx.writeAndFlush("> command should not empty\r\n");
            ctx.writeAndFlush("> ");
            return;
        }
        history.add(command);
        while (history.size() > 100) {
            history.poll();
        }
        log.info("console: command: {}", command);
        Object handler = consoleHandler;
        boolean background = false;
        if (StringUtils.startsWith(command, "&")) {
            background = true;
            command = command.substring(1);
        }
        if (StringUtils.startsWith(command, "*")) {
            command = command.substring(1);
            String name = command.split(" ")[0];
            command = command.substring(name.length());
            handler = springBeanUtil.getBeanWithName(name);
            if (handler == null) {
                handler = springBeanUtil.getBeanWithName(Introspector.decapitalize(name));
                if (handler == null) {
                    Class<?> classZ = Class.forName(name);
                    handler = springBeanUtil.getBeanWithType(classZ);
                    if (handler == null) {
                        handler = classZ.newInstance();
                    }
                }
            }
        }
        if (StringUtils.startsWith(command, "-FROM")) {
            command = command.substring(5);
            String name = command.split(" ")[0];
            command = command.substring(name.length());
            handler = saveObject.get("name");
        }
        if (StringUtils.startsWith(command, "| ")) {
            // | ls, fefe, afef
            for (String subCommand : StringUtils.split(command.substring(2), ";")) {
                subCommand = StringUtils.trim(subCommand);
                if (StringUtils.isEmpty(subCommand)) {
                    continue;
                }
                this.channelRead(ctx, subCommand);
            }
            return;
        }

        if (StringUtils.equals(command, "quit") || StringUtils.equals(command, "exit")) {
            ctx.writeAndFlush("> bye\r\n");
            ctx.close();
            return;
        } else if (StringUtils.equals(command, "history")) {
            history.forEach(cc -> ctx.write(cc + "\r\n"));
            ctx.writeAndFlush("> ");
            return;
        } else if (StringUtils.equals(command, "clear")) {
            while (history.size() > 0) {
                history.clear();
            }
            return;
        } else if (StringUtils.equals(command, "hello")) {
            ctx.write("hello" + "\r\n");
            ctx.writeAndFlush("> ");
            return;
        }else if (StringUtils.equals(command, "clearObj")) {
            saveObject.clear();
            ctx.write("delete all saved object" + "\r\n");
            ctx.writeAndFlush("> ");
            return;
        }

        if (StringUtils.equals(command, "ls")) {
            Method[] methods = handler.getClass().getMethods();
            Arrays.stream(methods).forEach(method -> {
                method.setAccessible(true);
                String name = method.getName();
                if (StringUtils.equals(name, "wait") || StringUtils.equals(name, "equals")
                        || StringUtils.equals(name, "toString") || StringUtils.equals(name, "hashCode")
                        || StringUtils.equals(name, "getClass") || StringUtils.equals(name, "notify") || StringUtils.equals(name, "notifyAll")) {
                    return;
                }
                ctx.write(" " + method.getName() + "() ");
                Arrays.stream(method.getParameterTypes()).forEach(param -> ctx.write(param.getCanonicalName() + " "));
                ctx.write("\r\n");
            });
            ctx.writeAndFlush("> ");
            return;
        }

        String returnout = this.invoke(command, background, handler);
        ctx.writeAndFlush(returnout + "\r\n");
        ctx.writeAndFlush("> ");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("TelnetServerHandler.exceptionCaught", cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Assert.notNull(ctx, "ctx must not be null");
        ctx.close();
    }

    public String invoke(String command, boolean background, Object handler) throws Exception {
        if (background) {
            asyncPool.execute(() -> {
                try {
                    invokeLink(command, handler);
                } catch (Exception e) {
                    log.error("telnet cmd run background error,cmd:{}", command, e);
                }
            });
            return "run background";
        } else {
            String returnout = "";
            returnout = JSONObject.toJSONString(invokeLink(command, handler));
            return returnout;
        }
    }

    public Object invokeLink(String command, Object handler) throws Exception {
        Object obj = handler;
        String[] commands = command.split(" . ");
        for (String cmd : commands) {
            obj = invoke(cmd, obj);
        }
        String returnout = "";
        returnout = JSONObject.toJSONString(obj);
        log.info("telnet invokeLink cmd:{},result:{}", command, returnout);
        return obj;
    }
    static List<String> parseLine(final String untrimmedLine) {
        final int numTokensToCollect = -1;
        assert untrimmedLine != null;
        final String line = untrimmedLine.trim();
        List<String> tokens = new ArrayList<>();
        String currentToken = "";
        // state machine being either in neutral state, in singleHyphenOpen state, or in doubleHyphenOpen State.
        boolean singleHyphenOpen = false;
        boolean doubleHyphenOpen = false;
        int index = 0;
        for (; index < line.length(); index++) {
            if (tokens.size() == numTokensToCollect) {
                break;
            }
            char ch = line.charAt(index);
            // escaped char?
            if (ch == '\\' && (singleHyphenOpen || doubleHyphenOpen)) {
                ch = (index == line.length() - 1) ? '\\' : line.charAt(index + 1);
                index++;
                currentToken += ch;
                continue;
            }

            if (ch == '"' && !singleHyphenOpen) {
                if (doubleHyphenOpen) {
                    tokens.add(currentToken);
                    currentToken = "";
                    doubleHyphenOpen = false;
                } else {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken);
                        currentToken = "";
                    }
                    doubleHyphenOpen = true;
                }
                continue;
            }
            if (ch == '\'' && !doubleHyphenOpen) {
                if (singleHyphenOpen) {
                    tokens.add(currentToken);
                    currentToken = "";
                    singleHyphenOpen = false;
                } else {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken);
                        currentToken = "";
                    }
                    singleHyphenOpen = true;
                }
                continue;
            }
            if (ch == ' ' && !doubleHyphenOpen && !singleHyphenOpen) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken);
                    currentToken = "";
                }
                continue;
            }
            currentToken += ch;
        } // end for char in line
        if (index == line.length() && doubleHyphenOpen) {
            throw new IllegalArgumentException("Missing closing in " + line + " -- " + tokens);
        }
        if (index == line.length() && singleHyphenOpen) {
            throw new IllegalArgumentException("Missing closing \' in " + line + " -- " + tokens);
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken);
        }
        return tokens;
    }
    public Object invoke(String command, Object handler) throws Exception {
        boolean save = false;
        String saveName = null;
        List<String> args = parseLine(command);
        if (args.get(args.size() - 1).startsWith("-AS")) {
            String asStr = args.get(args.size() - 1);
            args.remove(args.size() - 1);
            save = true;
            saveName = asStr.substring(3);
        }
        if (args.get(0).equals("-self")) {
            if (save) {
                saveObject.put(saveName, handler);
                return "obj save name:" + saveName;
            }
            return handler;
        }
        List<Object> parameters = new ArrayList<>();

        Method[] methods = handler.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (!StringUtils.equals(method.getName(), args.get(0))) {
                continue;
            }
            method.setAccessible(true);
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != args.size() - 1) {
                continue;
            }
            method.setAccessible(true);
            for (i = 0; i < parameterTypes.length; i++) {
                String arg = args.get(i + 1);
                if (arg.startsWith("-FROM")) {
                    parameters.add(saveObject.get(arg.substring(5)));
                    continue;
                }
                Class<?> parameterType = parameterTypes[i];
                if (String.class.equals(parameterType)) {
                    parameters.add(arg);
                    continue;
                }
                if (Integer.class.equals(parameterType) || int.class.equals(parameterType)) {
                    parameters.add(Integer.valueOf(arg));
                    continue;
                }
                if (Long.class.equals(parameterType) || long.class.equals(parameterType)) {
                    parameters.add(Long.valueOf(arg));
                    continue;
                }
                if (Double.class.equals(parameterType) || double.class.equals(parameterType)) {
                    parameters.add(Double.valueOf(arg));
                    continue;
                }
                if (Boolean.class.equals(parameterType) || boolean.class.equals(parameterType)) {
                    parameters.add(Boolean.valueOf(arg));
                    continue;
                }
                if (Object.class.equals(parameterType)) {
                    parameters.add(arg);
                    continue;
                }
                if (Class.class.equals(parameterType)) {
                    parameters.add(Class.forName(arg));
                    continue;
                }
                try {
                    parameters.add(JSONObject.parseObject(arg, parameterType));
                } catch (Exception ex) {
                    log.error("telnet console unsupported parameter type", ex);
                    throw new Exception("telnet console unsupported parameter type: " + parameterType.toString());
                }
            }

            Object returnObj = method.invoke(handler, parameters.toArray(new Object[]{}));
            if (save) {
                saveObject.put(saveName, returnObj);
                return "obj save name:" + saveName;
            }
            return returnObj;
        }
        return "method not found";
    }

    @PreDestroy
    public void end() {
        BlockingQueue<Runnable> queue = asyncPool.getQueue();
        log.info("Destroy telnet asyncPool queue size {}", queue.size());
        while (queue.size() > 0) {
            Runnable runnable = queue.poll();
            if (runnable != null) {
                runnable.run();
            }
        }
        log.info("Destroy telnet asyncPool queue finish size {}", asyncPool.getQueue().size());
    }
}

