package com.turn.browser.service.account;

import com.turn.browser.bean.CommonConstant;
import com.turn.browser.client.TurnClient;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.exception.BusinessException;
import com.bubble.protocol.core.DefaultBlockParameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Account services:
 * 1. Query the balance of the incentive pool
 */
@Slf4j
@Service
public class AccountService {

    private static final String INCITE_ACCOUNT_ADDR = InnerContractAddrEnum.INCENTIVE_POOL_CONTRACT.getAddress();

    private static final String RESTRICTING_ADDR = InnerContractAddrEnum.RESTRICTING_PLAN_CONTRACT.getAddress();

    private static final String STAKING_ADDR = InnerContractAddrEnum.STAKING_CONTRACT.getAddress();

    private static final String REWARD_ADDR = InnerContractAddrEnum.REWARD_CONTRACT.getAddress();

    private static final String BLOCK_TIP = "]In block number[";

    private static final String BALANCE_TIP = "] Balance failed:";

    @Resource
    private TurnClient turnClient;

    /**
     * Get the incentive pool balance based on the block number with retry function
     *
     * @param blockNumber
     */
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum)
    public BigDecimal getInciteBalance(BigInteger blockNumber) {
        if (blockNumber.compareTo(BigInteger.ZERO) < 0) {
            blockNumber = BigInteger.ZERO;
        }
        try {
            BigInteger balance = turnClient.getWeb3jWrapper().getWeb3j().bubbleGetBalance(INCITE_ACCOUNT_ADDR, DefaultBlockParameter.valueOf(blockNumber)).send().getBalance();
            return new BigDecimal(balance);
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            String error = "Get incentive pool[" + INCITE_ACCOUNT_ADDR + BLOCK_TIP + blockNumber + BALANCE_TIP + e.getMessage();
            log.error("{}", error);
            throw new BusinessException(error);
        }
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back
     *
     * @param e:
     * @return: void
     */
    @Recover
    public BigDecimal recover(Exception e) {
        log.error("Retry is completed or the business fails, please contact the administrator for processing");
        return BigDecimal.ZERO;
    }

    /**
     * With retry function, obtain the lock pool balance based on the block number
     *
     * @param blockNumber
     */
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum)
    public BigDecimal getLockCabinBalance(BigInteger blockNumber) {
        if (blockNumber.compareTo(BigInteger.ZERO) < 0) {
            blockNumber = BigInteger.ZERO;
        }
        try {
            BigInteger balance = turnClient.getWeb3jWrapper().getWeb3j().bubbleGetBalance(RESTRICTING_ADDR, DefaultBlockParameter.valueOf(blockNumber)).send().getBalance();
            return new BigDecimal(balance);
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            String error = "Get lock contract[" + RESTRICTING_ADDR + BLOCK_TIP + blockNumber + BALANCE_TIP + e.getMessage();
            log.error("{}", error);
            throw new BusinessException(error);
        }
    }

    /**
     * Get the pledge pool balance based on the block number with retry function
     *
     * @param blockNumber
     */
    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public BigDecimal getStakingBalance(BigInteger blockNumber) {
        if (blockNumber.compareTo(BigInteger.ZERO) < 0) blockNumber = BigInteger.ZERO;
        try {
            BigInteger balance = turnClient.getWeb3jWrapper().getWeb3j().bubbleGetBalance(STAKING_ADDR, DefaultBlockParameter.valueOf(blockNumber)).send().getBalance();
            return new BigDecimal(balance);
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            String error = "Get pledge contract[" + STAKING_ADDR + BLOCK_TIP + blockNumber + BALANCE_TIP + e.getMessage();
            log.error("{}", error);
            throw new BusinessException(error);
        }
    }

    /**
     * Get the income balance based on the block number with retry function
     *
     * @param blockNumber
     */
    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public BigDecimal getRewardBalance(BigInteger blockNumber) {
        if (blockNumber.compareTo(BigInteger.ZERO) < 0) blockNumber = BigInteger.ZERO;
        try {
            BigInteger balance = turnClient.getWeb3jWrapper().getWeb3j().bubbleGetBalance(REWARD_ADDR, DefaultBlockParameter.valueOf(blockNumber)).send().getBalance();
            return new BigDecimal(balance);
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            String error = "Get pledge contract[" + REWARD_ADDR + BLOCK_TIP + blockNumber + BALANCE_TIP + e.getMessage();
            log.error("{}", error);
            throw new BusinessException(error);
        }
    }

}
