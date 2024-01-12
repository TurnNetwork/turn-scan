package com.turn.browser;

import com.alibaba.druid.pool.DruidDataSource;
import com.turn.browser.dao.mapper.NetworkStatMapper;
import com.turn.browser.enums.AppStatus;
import com.turn.browser.utils.AppStatusUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;
import javax.sql.DataSource;
/**
 * api文档：http://localhost:40010/browser-server/#/home
 */
@EnableCaching
@Slf4j
@EnableScheduling
@SpringBootApplication
@EnableEncryptableProperties
@EnableAsync
@MapperScan(basePackages = {"com.turn.browser.dao.mapper", "com.turn.browser.dao.custommapper"})
public class BrowserApiApplication implements ApplicationRunner {

    @Resource
    private NetworkStatMapper networkStatMapper;

    /**
     * 0 cycle access time waiting for block generation
     */
    @Value("${turn.zeroBlockNumber.wait-time:1}")
    private Integer zeroBlockNumberWaitTime;

    @Resource
    DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(BrowserApiApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (AppStatusUtil.isStopped()) {
            return;
        }
        dataSourceLog();
        zeroBlockNumberWait();
        // Set the application to RUNNING state so that scheduled tasks can execute business logic
        AppStatusUtil.setStatus(AppStatus.RUNNING);
    }

    /**
     * 0 block waiting
     *
     * @param
     * @return void
     */
    private void zeroBlockNumberWait() {
        try {
            while (true) {
                long count = networkStatMapper.countByExample(null);
                if (count > 0) {
                    log.info("Start producing blocks...");
                    break;
                }
                Thread.sleep(1000L * zeroBlockNumberWaitTime);
                log.info("Waiting for block...");
            }
        } catch (Exception e) {
            log.error("0 block waiting exception", e);
        }
    }

    /**
     * Print connection pool information
     *
     * @param
     * @return void
     */
    private void dataSourceLog() {
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        log.info("Data source: {}, Maximum number of connections: {}, Minimum number of connection pools: {}, Number of initial connections: {}, Maximum waiting time when obtaining a connection: {}, Enable fair lock: {}", dataSource.getClass(), druidDataSource.getMaxActive(), druidDataSource.getMinIdle(), druidDataSource.getInitialSize(),
                 // After configuring maxWait, fair lock is enabled by default, and the concurrency efficiency will decrease. If necessary, you can use unfair lock by configuring the useUnfairLock attribute to true.
                 druidDataSource.getMaxWait(), druidDataSource.isUseUnfairLock());
    }

}
