package com.quanquan.telnet.telnet;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * @Author qiupengjun
 * @Date 2022 06 23 15 10
 **/
@Component
public class TelnetServer {

    protected final Logger log =  LoggerFactory.getLogger(this.getClass());
    private Channel serverChannel;

    private final ServerBootstrap serverBootstrap;

    public TelnetServer(ServerBootstrap serverBootstrap) {

        this.serverBootstrap = serverBootstrap;
    }

    private Channel getTelnetPortChannel() throws Exception {
        for (int i = 0; i < 10; i++) {
            try {
                return serverBootstrap.bind(8000 + i).sync().channel();
            } catch (Exception e) {
                log.error("port:{} is used,try next", 8000 + i);
            }
        }
        return serverBootstrap.bind(0).sync().channel();
    }

    public void start() throws Exception {
        serverChannel = getTelnetPortChannel();
        InetSocketAddress localAddress = (InetSocketAddress) serverChannel.localAddress();
        int port = localAddress.getPort();
        System.out.println("start TelnetServer: " + port);
        log.info("start TelnetServer: " + port);
    }

    @PreDestroy
    public void stop() {
        if (serverChannel == null) {
            return;
        }
        serverChannel.close();
        if (serverChannel.parent() != null) {
            serverChannel.parent().close();
        }
    }


    @EventListener(ApplicationReadyEvent.class)
    public void listen() throws Exception {
        System.out.println("> start console");
        this.start();
        System.out.println("> done console");
    }
}
