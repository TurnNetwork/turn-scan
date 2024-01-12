package com.turn.browser.service.receipt;

import com.turn.browser.bean.ReceiptResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * @Description: Transaction services
 */
@Slf4j
@Service
public class ReceiptService {
    @Resource
    private ReceiptRetryService retryService;

    /**
     * Get blocks asynchronously
     */
    public CompletableFuture<ReceiptResult> getReceiptAsync(Long blockNumber) {
        return CompletableFuture.supplyAsync(()->{
            try {
                return retryService.getReceipt(blockNumber);
            } catch (Exception  e) {
                log.error("Collection block ({}) exception",blockNumber,e);
            }
            return null;
        });
    }
}
