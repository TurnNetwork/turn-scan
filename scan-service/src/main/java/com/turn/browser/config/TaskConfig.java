package com.turn.browser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

/**
 * Use thread pool to run scheduled tasks and determine the number of thread pools
 */
@Data
@Configuration
@ConfigurationProperties(prefix="task")
public class TaskConfig implements SchedulingConfigurer {
    private int addressBatchSize; //Address statistics task batch size
    private int gapForAdjust; //The adjustment operation is triggered when the block number differs between the agent and the actual block number on the chain.
    private int esRedisNotCatchupBatchSize; // Before catching up with the chain, the cache size of ES and Redis in batches
    private int esRedisCatchupBatchSize; //Has caught up with the chain, batch into the cache size of ES and Redis
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(5));
    }
}
