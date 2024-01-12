package com.turn.browser.service.receipt;

import cn.hutool.core.util.StrUtil;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.bean.RpcParam;
import com.turn.browser.client.TurnClient;
import com.turn.browser.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description: Trading service with retry function
 */
@Slf4j
@Service
public class ReceiptRetryService {

    private static final String RECEIPT_RPC_INTERFACE = "turn_getTransactionByBlock";

    @Resource
    private TurnClient turnClient;

    /**
     * With retry function, obtain the receipt information of all transactions in the block based on the block number
     *
     * @param blockNumber
     * @return Transaction reply information
     * @throws
     */
    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public ReceiptResult getReceipt(Long blockNumber) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("Get a receipt:{}({})", Thread.currentThread().getStackTrace()[1].getMethodName(), blockNumber);
            RpcParam param = new RpcParam();
            param.setMethod(RECEIPT_RPC_INTERFACE);
            param.getParams().add(blockNumber);
            ReceiptResult result = turnClient.getReceiptResult(blockNumber);
            log.debug("Number of receipt results:{}", result.getResult().size());
            log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            log.error(StrUtil.format("Block [{}] gets receipt exception", blockNumber), e);
            throw new BusinessException(e.getMessage());
        }
    }

}
