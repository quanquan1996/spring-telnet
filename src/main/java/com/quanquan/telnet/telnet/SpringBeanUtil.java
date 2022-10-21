package com.quanquan.telnet.telnet;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @Author qiupengjun
 * @Date 2022 06 23 15 10
 **/

@Component
public class SpringBeanUtil implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public Object getBeanWithName(String name) {
        try {
            return getApplicationContext().getBean(name);
        } catch (Exception e) {
            return null;
        }

    }

    public Object getBeanWithType(Class<?> z) {
        try {
            return getApplicationContext().getBean(z);
        } catch (Exception e) {
            return null;
        }

    }
}
