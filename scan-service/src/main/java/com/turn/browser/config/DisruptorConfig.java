package com.turn.browser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: disruptor configuration
 */
@Data
@Configuration
@ConfigurationProperties(prefix="disruptor.queue")
public class DisruptorConfig {
    private int blockBufferSize; // block event ring buffer size
    private int collectionBufferSize; // Collection event ring buffer size
    private int complementBufferSize; // Data complement ring buffer size
    private int gasEstimateBufferSize; // gas price estimates message ring buffer size
    private int persistenceBufferSize; // Data persistence ring buffer size
    private int persistenceBatchSize; //Data persistence batch size
}