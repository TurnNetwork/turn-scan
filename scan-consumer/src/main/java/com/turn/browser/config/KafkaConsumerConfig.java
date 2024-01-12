package com.turn.browser.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka consumer group configuration
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${scan.kafka.consumer.client-id}")
    private String clientId;

    @Value("${scan.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${scan.kafka.consumer.group-id}")
    private String groupId;

    @Value("${scan.kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Value("${scan.kafka.consumer.session-timeout-ms}")
    private String sessionTimeout;

    @Value("${scan.kafka.consumer.max-poll-records}")
    private String maxPollRecords;

    @Value("${scan.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${scan.kafka.consumer.isolation-level}")
    private String isolationLevel;

    @Value("${scan.kafka.consumer.request-timeout-ms}")
    private Integer requestTimeoutMs;

    @Value("${scan.kafka.consumer.listener-missing-topics-fatal}")
    private boolean missingTopicsFatal;

    @Value("${scan.kafka.consumer.listener-poll-timeout}")
    private long pollTimeout;

    @Value("${scan.kafka.consumer.listener-batch-listener}")
    private boolean batchListener;

    @Value("${scan.kafka.consumer.listener-concurrency}")
    private Integer concurrency;

    @Value("${scan.kafka.consumer.retry-interval}")
    private Integer retryInterval;

    @Value("${scan.kafka.consumer.retry-max-attempts}")
    private Integer retryMaxAttempts;

    public Map<String, Object> consumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>();
        //Client ID
        propsMap.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        //Kafka server
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        //Consumer group ID
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        //Whether to automatically submit the offset, the default value is true, in order to avoid duplicate data and data loss, you can set it to false, and then manually submit the offset
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        //The time interval for automatic submission, effective when automatic submission is turned on
        //propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "2000");
        //This attribute specifies what the consumer should do when reading a partition without an offset or an invalid offset:
        //earliest: When there is a submitted offset under each partition, consumption starts from the submitted offset; when there is no submitted offset, consumption of partition records starts from the beginning.
        //latest: When there is a submitted offset under each partition, consumption starts from the submitted offset; when there is no submitted offset, the newly generated data under the partition is consumed (records generated after the consumer is started)
        //none: When each partition has a submitted offset, consumption starts from the submitted offset; as long as one partition does not have a submitted offset, an exception is thrown.
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        //This parameter defines the maximum number of messages that the poll method can pull. The default value is 500. If there are less than 500 new messages when pulling messages, as many as there are will be returned; if there are more than 500, only 500 will be returned each time.
        //This default value is too large in some scenarios. In some scenarios, it is difficult to guarantee that 500 messages can be processed within 5 minutes.
        //If the consumer cannot process 500 messages within 5 minutes, reBalance will be triggered,
        //Then this batch of messages will be assigned to another consumer, and it will still be processed, so this batch of messages will never be processed.
        //To avoid the above problems, evaluate in advance the maximum time required to process a message, and then override the default max.poll.records parameter
        //Note: BatchListener needs to be turned on for batch monitoring to take effect. If BatchListener is not turned on, reBalance will not occur.
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        //Request timed out
        propsMap.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        //When the broker does not receive the consumer's heartbeat request for a long time, reBalance is triggered. The default value is 10s.
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        //Serialization (it is recommended to use Json, this serialization method can transmit entity classes without additional configuration)
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel);
        return propsMap;
    }

    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(
            KafkaTemplate kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        //The number of threads running in the listener container is generally set to the number of machines * the number of partitions
        factory.setConcurrency(concurrency);
        //When the topic monitored by the consumption listening interface does not exist, an error will be reported by default, so set it to false to ignore the error.
        factory.setMissingTopicsFatal(missingTopicsFatal);
        //Automatic submission is turned off, manual message confirmation needs to be set
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(pollTimeout);
        //true is set to batch monitoring and needs to be received using List
        factory.setBatchListener(batchListener);
        //Retry every 10 seconds, retry 3 times, if unsuccessful, send to the dead letter queue topicName.DLT
        BackOff backOff = new FixedBackOff(retryInterval, retryMaxAttempts);
        factory.setErrorHandler(new SeekToCurrentErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate),
                                                              backOff));
        return factory;
    }

}
