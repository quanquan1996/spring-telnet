package com.quanquan.telnet.telnet;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author qiupengjun
 * @Date 2022 10 20 17 49
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({SpringBeanUtil.class,TelnetChannelInitializer.class,TelnetConfig.class,TelnetServer.class,TelnetServerHandler.class})
@Documented
public @interface EnableTelnet {
}
