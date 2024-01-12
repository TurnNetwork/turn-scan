package com.turn.browser;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableEncryptableProperties
@EnableKafka
public class BrowserConsumerApplication{

    public static void main(String[] args) {
        SpringApplication.run(BrowserConsumerApplication.class, args);
    }


}
