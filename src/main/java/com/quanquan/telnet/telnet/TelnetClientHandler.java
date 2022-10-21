package com.quanquan.telnet.telnet;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author qiupengjun
 * @Date 2022 06 29 18 26
 **/
@ChannelHandler.Sharable
public class TelnetClientHandler extends ChannelInboundHandlerAdapter {
    public ChannelHandlerContext ctx;
    public boolean isSync;
    public LinkedBlockingQueue<String> responseQueue = new LinkedBlockingQueue<>(1000);
    protected final Logger log =  LoggerFactory.getLogger(this.getClass());
    public TelnetClientHandler(boolean isSync) {
        this.isSync = isSync;
    }

    /**
     * 客户端连接服务端完成时，会触发这个方法
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;

    }

    /**
     * 当通道有读取事件时，会触发。服务端发送数据给客户端时
     *
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
//        ByteBuf buf = (ByteBuf)msg;
        this.ctx = ctx;
        if (isSync) {
            responseQueue.put(msg.toString());
        }
        log.info("收到服务端消息：" + msg.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
