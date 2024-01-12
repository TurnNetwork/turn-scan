package com.turn;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableRetry
@Configuration
@EnableScheduling
@SpringBootApplication
@EnableEncryptableProperties
@MapperScan(basePackages = {
	"com.turn.browser",
	"com.turn.browser.dao.mapper",
	"com.turn.browser.v0150.dao",
	"com.turn.browser.v0151.dao"
})
public class AgentApplicationTest {
	public static void main(String[] args) {
		SpringApplication.run(AgentApplicationTest.class, args);
	}
}
