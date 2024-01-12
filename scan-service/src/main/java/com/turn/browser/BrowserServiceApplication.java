package com.turn.browser;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
@MapperScan(basePackages = {"com.turn.browser.dao.mapper", "com.turn.browser.dao.custommapper"})
public class BrowserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrowserServiceApplication.class, args);
    }

}
