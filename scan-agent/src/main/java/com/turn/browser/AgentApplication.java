package com.turn.browser;

import com.turn.browser.bean.EpochMessage;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.bootstrap.bean.InitializationResult;
import com.turn.browser.bootstrap.service.ConsistencyService;
import com.turn.browser.bootstrap.service.InitializationService;
import com.turn.browser.client.RetryableClient;
import com.turn.browser.enums.AppStatus;
import com.turn.browser.publisher.BlockEventPublisher;
import com.turn.browser.service.block.BlockService;
import com.turn.browser.service.epoch.EpochService;
import com.turn.browser.service.receipt.ReceiptService;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.CommonUtil;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
//#@ServletComponentScan

@Slf4j
@EnableRetry
@Configuration
@EnableScheduling
@SpringBootApplication
@EnableEncryptableProperties
@ServletComponentScan
@ComponentScan(basePackages = {"com.turn.browser.**"})
@MapperScan(basePackages = {"com.turn.browser", "com.turn.browser.dao.mapper", "com.turn.browser.dao.custommapper", "com.turn.browser.v0150.dao", "com.turn.browser.v0151.dao"})
public class AgentApplication implements ApplicationRunner {

    @Resource
    private BlockService blockService;

    @Resource
    private ReceiptService receiptService;


    @Resource
    private BlockEventPublisher blockEventPublisher;


    @Resource
    private EpochService epochService;

    /**
     * Start the consistency check service
     */
    @Resource
    private ConsistencyService consistencyService;

    /**
     * Start initialization service
     */
    @Resource
    private InitializationService initializationService;

    @Resource
    private RetryableClient retryableClient;

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (AppStatusUtil.isStopped()) {
            return;
        }
        String traceId = CommonUtil.createTraceId();
        CommonUtil.putTraceId(traceId);
        // Set the application to BOOTING state
        AppStatusUtil.setStatus(AppStatus.BOOTING);
        retryableClient.zeroBlockNumberWait();
        InitializationResult initialResult = initializationService.init(traceId);
        consistencyService.post(traceId);
        // After the startup self-test and initialization are completed, set the application to the RUNNING running state so that scheduled tasks can execute business logic
        AppStatusUtil.setStatus(AppStatus.RUNNING);
        // The highest block number has been taken
        long collectedNumber = initialResult.getCollectedBlockNumber();
        // previous block number
        long preBlockNum;
        CommonUtil.removeTraceId();
        // Enter the main process of block collection
        while (true) {
            try {
                traceId = CommonUtil.createTraceId();
                CommonUtil.putTraceId(traceId);
                preBlockNum = collectedNumber++;
                // Check if the block number is legal
                blockService.checkBlockNumber(collectedNumber);
                // Get blocks asynchronously
                CompletableFuture<BubbleBlock> blockCF = blockService.getBlockAsync(collectedNumber);
                // Get transaction receipt asynchronously
                CompletableFuture<ReceiptResult> receiptCF = receiptService.getReceiptAsync(collectedNumber);
                // Get cycle switching message
                EpochMessage epochMessage = epochService.getEpochMessage(collectedNumber);
                blockEventPublisher.publish(blockCF, receiptCF, epochMessage, traceId);
                if (preBlockNum != 0L && (collectedNumber - preBlockNum != 1)) {
                    log.error("Abnormal collection of data, current block {}, previous block{}", collectedNumber, preBlockNum);
                    throw new AssertionError();
                }
                CommonUtil.removeTraceId();
            } catch (Exception e) {
                log.error("Program stopped with error:", e);
                break;
            } finally {
                CommonUtil.removeTraceId();
            }
        }
    }

}
