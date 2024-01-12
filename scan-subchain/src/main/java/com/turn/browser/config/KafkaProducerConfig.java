package com.turn.browser.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka producer configuration
 * 1. The config mode is more flexible than yml, and some configurations can only be configured through config
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${scan.kafka.producer.client-id}")
    private String clientId;

    @Value("${scan.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${scan.kafka.producer.transaction-id-prefix}")
    private String transactionIdPrefix;

    @Value("${scan.kafka.producer.retries}")
    private String retries;

    @Value("${scan.kafka.producer.acks}")
    private String acks;

    @Value("${scan.kafka.producer.batch-size}")
    private String batchSize;

    @Value("${scan.kafka.producer.buffer-memory}")
    private String bufferMemory;

    @Value("${scan.kafka.producer.compression-type}")
    private String compressionType;

    @Value("${scan.kafka.producer.linger-ms}")
    private String lingerMs;

    @Value("${scan.kafka.producer.max-request-size}")
    private Integer maxRequestSize;

    @Value("${scan.kafka.producer.max-block-ms}")
    private long maxBlockMs;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        //Client ID
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        //Kafka server
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        //After an error occurs, the number of times the message is retransmitted, the number of times the transaction is opened must be greater than 0
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        //Set idempotence. The default for opening transactions is to enable idempotence.
        //props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        //acks=0: The producer will not wait for any response from the server before successfully writing the message.
        //acks=1: As long as the leader node of the cluster receives the message, the producer will receive a successful response from the server.
        //acks=all: Only when all nodes participating in replication receive the message, the producer will receive a successful response from the server.
        //Open transaction must be set to all
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        //When multiple messages are sent to the same partition, the producer will package the messages together to reduce request interaction. Instead of sending them one by one
        //The size of the batch can be set through the batch.size parameter. The default is 16KB
        //Smaller batch sizes have the potential to reduce throughput (a batch size of 0 disables batching entirely).
        //For example, the message in Kafka takes 5 seconds to complete the batch of 16KB before it can be sent out. Then the delay of these messages is 5 seconds
        //The measured batchSize parameter is useless
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        //The size of the producer memory buffer
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        //Maximum size of each message
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);
        //Message compression: none, lz4, gzip, snappy, default is none.
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        //Sometimes there are relatively few messages, and after a long time, for example, 16KB is not collected even in 5 minutes, so the delay is very large, so a parameter is needed. Then set a time, and when this time comes,
        //Even if the data does not reach 16KB, this batch will be sent out
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        //Deserialization corresponds to the serialization method of the producer
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //The maximum blocking time of KafkaProducer.send() and partitionsFor() methods in ms
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMs);
        return props;
    }

    /**
     * Not include transactions producerFactory
     *
     * @return
     */
    public ProducerFactory<Object, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * Contains transactions producerFactory
     *
     * @return
     */
    public ProducerFactory<Object, Object> producerFactoryWithTransaction() {
        DefaultKafkaProducerFactory<Object, Object> defaultKafkaProducerFactory = new DefaultKafkaProducerFactory<>(
                producerConfigs());
        //Set transaction ID prefix
        defaultKafkaProducerFactory.setTransactionIdPrefix(transactionIdPrefix);
        return defaultKafkaProducerFactory;
    }

    /**
     * Does not contain transaction kafkaTemplate
     *
     * @return
     */
    @Bean("kafkaTemplate")
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    /**
     * Contains transaction kafkaTemplate
     *
     * @return
     */
    @Bean("kafkaTemplateWithTransaction")
    public KafkaTemplate<Object, Object> kafkaTemplateWithTransaction() {
        return new KafkaTemplate<>(producerFactoryWithTransaction());
    }

}
