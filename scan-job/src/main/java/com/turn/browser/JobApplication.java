package com.turn.browser;


import com.turn.browser.enums.AppStatus;
import com.turn.browser.exception.ConfigLoadingException;
import com.turn.browser.utils.AppStatusUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@EnableRetry
@Configuration
@EnableScheduling
@SpringBootApplication
@EnableEncryptableProperties
@MapperScan(basePackages = {"com.turn.browser", "com.turn.browser.dao.mapper", "com.turn.browser.dao.custommapper"})
public class JobApplication implements ApplicationRunner {

    static {
        File saltFile = FileUtils.getFile(System.getProperty("user.dir"), "jasypt.properties");
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(saltFile)) {
            properties.load(in);
            String salt = properties.getProperty("jasypt.encryptor.password");
            if (StringUtils.isBlank(salt)) throw new ConfigLoadingException("Crypto salt cannot be empty!");
            salt = salt.trim();
            System.setProperty("JASYPT_ENCRYPTOR_PASSWORD", salt);
            log.info("salt:{}", salt);
        } catch (IOException | ConfigLoadingException e) {
            log.error("Error loading decrypted file", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // After the startup self-test and initialization are completed, set the application to the RUNNING running state so that scheduled tasks can execute business logic
        AppStatusUtil.setStatus(AppStatus.RUNNING);
    }

}
