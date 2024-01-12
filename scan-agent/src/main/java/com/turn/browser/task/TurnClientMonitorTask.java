package com.turn.browser.task;

import com.turn.browser.client.TurnClient;
import com.turn.browser.utils.AppStatusUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description: web3j instance survival monitoring task
 */
@Component
@Slf4j
public class TurnClientMonitorTask {

    @Resource
    private TurnClient turnClient;

    @Scheduled(cron = "0/10 * * * * ?")
    public void turnClientMonitor () {
        // Only perform tasks when the program is running normally
        if(AppStatusUtil.isRunning()) start();
    }

    protected void start () {
        try {
            turnClient.updateCurrentWeb3jWrapper();
        } catch (Exception e) {
            log.error("detect exception:{}", e);
        }
    }
}
