package com.turn.browser;


import com.turn.browser.service.elasticsearch.EsSubChainTxRepository;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;


@EnableCaching
@EnableEncryptableProperties
@Slf4j
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BrowserSubchainApplication implements ApplicationRunner {

    @Resource
    private EsSubChainTxRepository esSubChainTxRepository;

    public static void main(String[] args) {

        SpringApplication.run(BrowserSubchainApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        esSubChainTxRepository.initIndex();
    }
}
