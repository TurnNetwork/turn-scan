package com.turn.browser.task;

import com.turn.browser.client.JobTurnClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class Web3jUpdateTask {

    @Resource
    private JobTurnClient jobTurnClient;

    @Scheduled(cron = "0/10 * * * * ?")
    public void cron() {
        try {
            jobTurnClient.updateCurrentWeb3jWrapper();
        } catch (Exception e) {
            log.error("detect exception:{}", e);
        }
    }

}
