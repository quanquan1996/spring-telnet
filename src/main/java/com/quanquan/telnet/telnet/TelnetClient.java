package com.quanquan.telnet.telnet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @Author qiupengjun
 * @Date 2022 06 29 16 14
 **/
public class TelnetClient {
    protected final Logger log =  LoggerFactory.getLogger(this.getClass());
    private Channel channel;
    private final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    private final TelnetClientHandler clientHandler;
    private final boolean isSync;
    private final String ip;
    private final int port;

    public TelnetClient(String ip, int port, boolean isSync) throws InterruptedException {
        this.isSync = isSync;
        clientHandler = new TelnetClientHandler(isSync);
        this.ip = ip;
        this.port = port;
        init();
    }

    public void init() throws InterruptedException {
        this.channel = new Bootstrap()
                // 添加事件循环
                .group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                // 初始化器,在连接建立后会调用
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(clientHandler);
                    }
                })
                .connect(new InetSocketAddress(ip, port))
                .sync()
                .channel();
        log.info("TelnetClient init ip:{},port:{}", ip, port);
    }

    public void sendCmd(String text) {
        if (isSync) {
            sendCmdSync(text);
            return;
        }
        if (channel.isOpen()) {
            channel.writeAndFlush(text);
        } else {
            log.error("sendCmd error con close");
            try {
                init();
            } catch (Exception e) {
                log.error("sendCmd error con close and init error", e);
                return;
            }
            channel.writeAndFlush(text);
            log.error("sendCmd error con close and init success");
        }

    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public synchronized String sendCmdSync(String text) {
        if (!isSync) {
            log.error("sendCmd sync will not have return,because of isSync is false");
        }
        clientHandler.responseQueue.clear();
        if (channel.isOpen()) {
            channel.writeAndFlush(text);
        } else {
            try {
                init();
            } catch (Exception e) {
                log.error("sendCmd error con close and init error", e);
            }
            channel.writeAndFlush(text);
        }
        try {
            return clientHandler.responseQueue.poll(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void close() throws InterruptedException {
        clientHandler.ctx.close().sync();
        channel.close().sync();
        channel.closeFuture().sync();
        nioEventLoopGroup.shutdownGracefully();
    }

    public static void main(String[] std) throws InterruptedException {
        TelnetClient telnetClient = new TelnetClient("127.0.0.1", 8000, true);
        Thread.sleep(3000);
        System.out.println("response1" + telnetClient.sendCmdSync("*lete.data.ad.common.utils.Md5Utils MD5Lower test123\n"));
        System.out.println("response2" + telnetClient.sendCmdSync("*lete.data.ad.common.utils.Md5Utils MD5Lower test1234\n"));
        System.out.println("response3" + telnetClient.sendCmdSync("*lete.data.ad.common.utils.Md5Utils MD5Lower test12345\n"));
        telnetClient.close();
    }
}
