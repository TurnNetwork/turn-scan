package com.turn.browser.client;

import com.bubble.contracts.dpos.*;
import com.bubble.contracts.dpos.dto.resp.GovernParam;
import com.bubble.contracts.dpos.dto.resp.Node;
import com.bubble.protocol.core.methods.response.bean.EconomicConfig;
import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.exception.ConfigLoadingException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Component
public class TurnClient {

    // Parallel decoding thread pool of transaction input parameters
    @Getter
    @Setter
    private ExecutorService logDecodeExecutor;

    // Number of parallel decoding threads for transaction input parameters
    @Value("${turn.txLogDecodeThreadNum}")
    private int logDecodeThreadNum;

    @Resource
    private RetryableClient retryableClient;

    @Resource
    private SpecialApi specialApi;

    public DelegateContract getDelegateContract() {
        return retryableClient.getDelegateContract();
    }

    public NodeContract getNodeContract() {
        return retryableClient.getNodeContract();
    }

    public ProposalContract getProposalContract() {
        return retryableClient.getProposalContract();
    }

    public RestrictingPlanContract getRestrictingPlanContract() {
        return retryableClient.getRestrictingPlanContract();
    }

    public SlashContract getSlashContract() {
        return retryableClient.getSlashContract();
    }

    public StakingContract getStakingContract() {
        return retryableClient.getStakingContract();
    }

    public RewardContract getRewardContract() {
        return retryableClient.getRewardContract();
    }

    public BubbleContract getBubbleContract(){
        return retryableClient.getBubbleContract();
    }

    @PostConstruct
    private void init() throws ConfigLoadingException {
        logDecodeExecutor = Executors.newFixedThreadPool(logDecodeThreadNum);
        retryableClient.init();
    }

    public void updateCurrentWeb3jWrapper() {
        retryableClient.updateCurrentWeb3jWrapper();
    }

    public Web3jWrapper getWeb3jWrapper() {
        return retryableClient.getWeb3jWrapper();
    }

    public ReceiptResult getReceiptResult(Long blockNumber) throws IOException, InterruptedException {
        ReceiptResult receiptResult = specialApi.getReceiptResult(retryableClient.getWeb3jWrapper(), BigInteger.valueOf(blockNumber));
        receiptResult.resolve(blockNumber, logDecodeExecutor);
        return receiptResult;
    }

    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(value = 3000L))
    public EconomicConfig getEconomicConfig() throws ConfigLoadingException {
        try {
            EconomicConfig ec = retryableClient.getWeb3jWrapper().getWeb3j().getEconomicConfig().send().getEconomicConfig();
            String msg = JSON.toJSONString(ec);
            log.info("On-chain configuration: {}", msg);
            return ec;
        } catch (Exception e) {
            retryableClient.updateCurrentWeb3jWrapper();
            log.error("Error in obtaining on-chain configuration ({}), will try again!", e.getMessage());
            throw new ConfigLoadingException(e.getMessage());
        }
    }

    public void updateContract() {
        retryableClient.updateContract();
    }

    public BigInteger getLatestBlockNumber() throws IOException {
        return retryableClient.getWeb3jWrapper().getWeb3j().bubbleBlockNumber().send().getBlockNumber();
    }

    public List<Node> getLatestValidators() {
        try {
            return getNodeContract().getValidatorList().send().getData();
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public List<Node> getLatestVerifiers() {
        try {
            return getNodeContract().getVerifierList().send().getData();
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public List<GovernParam> getGovernParamValue(String module) {
        try {
            return getProposalContract().getParamList(module).send().getData();
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public String getBubbleInfo(BigInteger bubbleId){
        try {
            return getBubbleContract().getBubbleInfo(bubbleId).send().getData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
