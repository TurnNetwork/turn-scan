package com.turn.browser.service.block;

import cn.hutool.core.util.StrUtil;
import com.bubble.protocol.core.DefaultBlockParameter;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import com.turn.browser.client.TurnClient;
import com.turn.browser.exception.CollectionBlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * @Description: Block service with retry function
 */
@Slf4j
@Service
public class BlockRetryService {

    @Resource
    private TurnClient turnClient;

    private BigInteger latestBlockNumber;

    /**
     * Get block information based on block number
     *
     * @param blockNumber
     * @return Block information with transaction information
     * @throws
     */
    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    BubbleBlock getBlock(Long blockNumber) throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("Get block:{}({})", Thread.currentThread().getStackTrace()[1].getMethodName(), blockNumber);
            DefaultBlockParameter dp = DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber));
            BubbleBlock block = turnClient.getWeb3jWrapper().getWeb3j().bubbleGetBlockByNumber(dp, true).send();
            log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
            return block;
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            log.error(StrUtil.format("Get block[{}] exception", blockNumber), e);
            throw e;
        }
    }

    /**
     * Check whether the current block number is legal
     *
     * @param currentBlockNumber
     * @throws
     */
    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    void checkBlockNumber(Long currentBlockNumber) throws IOException, CollectionBlockException {
        try {
            if (latestBlockNumber == null || currentBlockNumber > latestBlockNumber.longValue()) {
                // If the latest block number recorded on the chain is empty, or the current block number is greater than the latest block number recorded on the chain, update the latest block number on the chain.
                latestBlockNumber = turnClient.getWeb3jWrapper().getWeb3j().bubbleBlockNumber().send().getBlockNumber();
            }
            if (currentBlockNumber > latestBlockNumber.longValue()) {
                log.warn("Preparing to collect block [{}], the highest block on the chain [{}], about to wait for retry...", currentBlockNumber, latestBlockNumber);
                // If the current block number is still greater than the latest block number on the updated chain
                throw new CollectionBlockException("currentBlockNumber(" + currentBlockNumber + ")>latestBlockNumber(" + latestBlockNumber + "), wait for chain");
            }
        } catch (Exception e) {
            log.warn("Check if the current block number is legal and abnormal{}", e.getMessage());
            throw e;
        }
    }

    public String getBubbleInfo(BigInteger bubbleId) {
        return turnClient.getBubbleInfo(bubbleId);
    }
}
