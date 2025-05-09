package com.quanquan.telnet.telnet.client;

import com.quanquan.telnet.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import both annotation packages for compatibility
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @Author qiupengjun
 * @Date 2022 06 30 10 17
 **/
public abstract class TelnetBaseService {
    protected final Logger log =  LoggerFactory.getLogger(this.getClass());
    public TelnetClient adApiTelnetClient;

    protected abstract String getApiIp();

    protected abstract int getApiPort();

    protected abstract boolean isSync();

    @PostConstruct
    public void init() {
        try {
            if (getApiPort() == 8000) {
                log.info("not config this telnet service :{},skip init", this.getClass().getName());
                return;
            }
            adApiTelnetClient = new TelnetClient(getApiIp(), getApiPort(), isSync());
        } catch (Exception e) {
            log.error(this.getClass().getName() + "init error", e);
        }
    }

    @PreDestroy
    public void close() throws InterruptedException {
        adApiTelnetClient.close();
    }

    public void sendCmdToApi(String cmd) {
        adApiTelnetClient.sendCmd(cmd);
    }

    public void sendCmdToApiQuickly(String cmd) {
        adApiTelnetClient.sendCmd(cmd);
    }

    public String sendCmdToApiSync(String cmd) {
        return adApiTelnetClient.sendCmdSync(cmd);
    }

    public void testSleep() throws InterruptedException {
        Thread.sleep(1000 * 10);
    }
}
