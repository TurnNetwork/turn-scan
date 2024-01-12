package com.turn.browser.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scan.kafka.topic")
@Data
public class TopicProperties {

    private String subChainTx;

}
