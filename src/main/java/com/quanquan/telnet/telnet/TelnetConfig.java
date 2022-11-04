package com.quanquan.telnet.telnet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author qiupengjun
 * @Date 2022 06 27 13 51
 **/
@Configuration
public class TelnetConfig {
    private final TelnetChannelInitializer telnetChannelInitializer;

    public TelnetConfig(@Qualifier("springTelnetChannelInitializer") TelnetChannelInitializer telnetChannelInitializer) {
        this.telnetChannelInitializer = telnetChannelInitializer;
    }

    @Bean(name = "serverBootstrapTelnet")
    public ServerBootstrap bootstrap() {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(4);
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(telnetChannelInitializer);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.SO_BACKLOG, 1024);

        return b;
    }

}
