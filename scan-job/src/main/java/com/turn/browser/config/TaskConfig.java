package com.turn.browser.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

/**
 *    Use thread pool to run scheduled tasks and determine the number of thread pools
 */
@Data
@Configuration
@ConfigurationProperties(prefix="task")
public class TaskConfig implements SchedulingConfigurer {
    private int maxAddressCount;
    private int maxBatchSize;
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(3));
    }
}
