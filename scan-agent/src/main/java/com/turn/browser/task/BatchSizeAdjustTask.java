package com.turn.browser.task;

import com.turn.browser.client.TurnClient;
import com.turn.browser.config.DisruptorConfig;
import com.turn.browser.config.TaskConfig;
import com.turn.browser.handler.PersistenceEventHandler;
import com.turn.browser.utils.AppStatusUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description: Dynamic adjustment tasks of batch processing related variables
 */
@Component
@Slf4j
public class BatchSizeAdjustTask {

    @Resource
    private TurnClient turnClient;

    @Resource
    private PersistenceEventHandler persistenceEventHandler;

    @Resource
    private DisruptorConfig disruptorConfig;

    @Resource
    private TaskConfig taskConfig;

    @Scheduled(cron = "0/30 * * * * ?")
    public void batchSizeAdjust() {
        // Only perform tasks when the program is running normally
        if (AppStatusUtil.isRunning())
            start();
    }

    protected void start() {
        try {
            long chainBlockNumber = turnClient.getLatestBlockNumber().longValue();
            long appBlockNumber = persistenceEventHandler.getMaxBlockNumber();
            if (chainBlockNumber - appBlockNumber < taskConfig.getGapForAdjust()) {
                log.info("---------------The chain has been caught up and the batch size is adjusted to {}---------------", taskConfig.getEsRedisCatchupBatchSize());
                disruptorConfig.setPersistenceBatchSize(taskConfig.getEsRedisCatchupBatchSize());
            } else {
                log.info("---------------Unable to catch up with the chain, adjust the batch size to {}---------------", taskConfig.getEsRedisNotCatchupBatchSize());
                disruptorConfig.setPersistenceBatchSize(taskConfig.getEsRedisNotCatchupBatchSize());
            }
        } catch (Exception e) {
            log.error("Error in dynamic adjustment of batch processing related variables:", e);
        }
    }

}
