package com.turn.browser.service.epoch;

import com.turn.browser.bean.CommonConstant;
import com.turn.browser.bean.ConfigChange;
import com.turn.browser.bean.EpochInfo;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.constant.Browser;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.exception.CandidateException;
import com.turn.browser.utils.EpochUtil;
import com.turn.browser.utils.HexUtil;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.resp.Node;
import com.bubble.protocol.Web3j;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * This type of non-thread safety
 * Reward calculation service
 * 1. Calculate cycle switching related values based on block number:
 * Name/Meaning Variable name
 * The balance of the incentive pool at the beginning of the current issuance cycle IB inciteBalance
 * The balance of the incentive pool at the beginning of the current issuance cycle is divided into the block reward part BR=IB*block reward ratio inciteAmount4Block
 * The reward value of each block in the current additional issuance cycle is BR/the total number of blocks in the additional issuance cycle blockReward
 * The balance of the incentive pool at the beginning of the current issuance cycle is divided into the pledge reward part SR=IB* pledge reward ratio inciteAmount4Stake
 * The pledge reward value of each settlement cycle of the current additional issuance cycle SSR=SR/the number of settlement cycles included in an additional issuance cycle settleStakeReward
 * The pledge reward value of each node in the current settlement cycle PerNodeSR=SSR/the actual number of verifiers in the current settlement cycle stakeReward
 * Current consensus cycle validators curValidators
 * Validator of the current settlement cycle                                                          curVerifiers
 */
@Slf4j
@Service
public class EpochRetryService {

    private final Queue<ConfigChange> epochChanges = new LinkedList<>();

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private TurnClient turnClient;

    @Resource
    private SpecialApi specialApi;

    // Note: All the following attributes are unchanged within the period to which they belong. Their values will only be updated when their respective periods change.
    // ******* Properties related to additional issuance cycle START *******
    @Getter
    private BigDecimal blockReward = BigDecimal.ZERO; // The reward value of each block in the current additional issuance cycle is BR/the total number of blocks in the additional issuance cycle.

    @Getter
    private BigDecimal settleStakeReward = BigDecimal.ZERO;  // The pledge reward value of each settlement cycle of the current additional issuance cycle SSR=SR/the number of settlement cycles included in an additional issuance cycle

    // ******* Properties related to additional issuance cycle END *******
    // ******* Consensus cycle related attributes START *******
    @Getter
    private final List<Node> preValidators = new ArrayList<>(); // List of validators of the previous consensus cycle

    @Getter
    private final List<Node> curValidators = new ArrayList<>(); // Current consensus cycle validator list

    @Getter
    private Long expectBlockCount = 0L; // Current expected number of blocks

    // ******* Consensus cycle related attributes END *******
    // ******* Settlement cycle related attributes START *******
    @Getter
    private final List<Node> preVerifiers = new ArrayList<>(); // List of validators of the previous settlement cycle

    @Getter
    private final List<Node> curVerifiers = new ArrayList<>(); // List of validators for the current settlement cycle

    @Getter
    private BigDecimal stakeReward = BigDecimal.ZERO; // The pledge reward value of each node in the current settlement period is PerNodeSR=SSR/the actual number of verifiers in the current settlement period.

    @Getter
    private BigDecimal preStakeReward = BigDecimal.ZERO; // 前一结算周期每个节点的质押奖励值 PerNodeSR=SSR/当前结算周期实际验证人数
    // ******* 结算周期相关属性 END *******

    @Resource
    private NetworkStatCache networkStatCache;

    /**
     * Changes in additional issuance cycle:
     * Inevitably accompanied by changes in the settlement cycle and consensus cycle
     *
     * @param currentBlockNumber Any block within the issuance period
     */
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum)
    public void issueChange(BigInteger currentBlockNumber) {
        log.debug("Changes in issuance cycle:{}({})", Thread.currentThread().getStackTrace()[1].getMethodName(), currentBlockNumber);
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back.
     *
     * @param e:
     * @return: void
     */
    @Recover
    public void recover(Exception e) {
        log.error("If the retry is completed or the service fails, please contact the administrator for processing.");
    }

    /**
     * Consensus cycle changes
     * 1. Update the validator of the current cycle
     * 2. Update the validator of the previous cycle
     * 3. Update the validator’s expected number of blocks
     *
     * @param currentBlockNumber any block within the consensus cycle
     */
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum)
    public void consensusChange(BigInteger currentBlockNumber) {
        log.debug("Consensus cycle changes:{}({})", Thread.currentThread().getStackTrace()[1].getMethodName(), currentBlockNumber);
        try {
            // The consensus cycle of the current block
            BigInteger currentEpoch = EpochUtil.getEpoch(currentBlockNumber, chainConfig.getConsensusPeriodBlockCount());
            // The consensus cycle of the latest block on the chain
            Web3j web3j = turnClient.getWeb3jWrapper().getWeb3j();
            BigInteger latestBlockNumber = turnClient.getLatestBlockNumber();
            BigInteger latestEpoch = EpochUtil.getEpoch(latestBlockNumber, chainConfig.getConsensusPeriodBlockCount());
            // The last block number of the previous cycle
            BigInteger preEpochLastBlockNumber = EpochUtil.getPreEpochLastBlockNumber(currentBlockNumber, chainConfig.getConsensusPeriodBlockCount());

            // Validator from the previous cycle
            List<Node> preNodes = specialApi.getHistoryValidatorList(web3j, preEpochLastBlockNumber);
            preNodes.forEach(n -> n.setNodeId(HexUtil.prefix(n.getNodeId())));
            preValidators.clear();
            preValidators.addAll(preNodes);

            // Validator of the current cycle
            List<Node> curNodes = Collections.emptyList();
            if (latestEpoch.compareTo(currentEpoch) > 0) {
                // >>>>If the cycle of the latest block on the chain > the cycle of the current block, query the special node history interface
                // If the last block of the previous cycle is 0, the validator at the 0th block will be used as the current validator.
                BigInteger targetBlockNumber = preEpochLastBlockNumber.compareTo(BigInteger.ZERO) == 0 ? BigInteger.ZERO : preEpochLastBlockNumber.add(BigInteger.ONE);
                curNodes = specialApi.getHistoryValidatorList(web3j, targetBlockNumber);
            }
            if (latestEpoch.compareTo(currentEpoch) == 0) {
                // >>>>If the period of the latest block on the chain == the period of the current block, query the real-time interface
                curNodes = turnClient.getLatestValidators();
            }
            curNodes.forEach(n -> n.setNodeId(HexUtil.prefix(n.getNodeId())));
            curValidators.clear();
            curValidators.addAll(curNodes);

            // Update the expected number of blocks: Expected number of blocks = Number of blocks in the consensus cycle/Number of actual participating consensus nodes
            expectBlockCount = chainConfig.getConsensusPeriodBlockCount().divide(BigInteger.valueOf(curValidators.size())).longValue();
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            log.error("", e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Changes in billing cycle
     * 1. Update the validator of the current cycle
     * 2. Update the validator of the previous cycle
     * 3. Update the current cycle block reward
     * 4. Update the staking rewards in the current cycle
     *
     * @param currentBlockNumber any block within the settlement cycle
     */
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum)
    public void settlementChange(BigInteger currentBlockNumber) {
        log.debug("settlement cycle changes:{}({})", Thread.currentThread().getStackTrace()[1].getMethodName(), currentBlockNumber);
        try {
            // The period of the current block
            BigInteger currentEpoch = EpochUtil.getEpoch(currentBlockNumber, chainConfig.getSettlePeriodBlockCount());
            // The period of the latest block on the chain
            Web3j web3j = turnClient.getWeb3jWrapper().getWeb3j();
            BigInteger latestBlockNumber = turnClient.getLatestBlockNumber();
            BigInteger latestEpoch = EpochUtil.getEpoch(latestBlockNumber, chainConfig.getSettlePeriodBlockCount());
            // The last block number of the previous cycle
            BigInteger preEpochLastBlockNumber = EpochUtil.getPreEpochLastBlockNumber(currentBlockNumber, chainConfig.getSettlePeriodBlockCount());

            // Validator from the previous cycle
            List<Node> preNodes = specialApi.getHistoryVerifierList(web3j, preEpochLastBlockNumber);
            preNodes.forEach(n -> n.setNodeId(HexUtil.prefix(n.getNodeId())));
            preVerifiers.clear();
            preVerifiers.addAll(preNodes);

            // Validator of the current cycle
            List<Node> curNodes = Collections.emptyList();
            if (latestEpoch.compareTo(currentEpoch) > 0) {
                // >>>>If the cycle of the latest block on the chain > the cycle of the current block, query the special node history interface
                // If the last block of the previous cycle is 0, the validator at the 0th block will be used as the current validator.
                BigInteger targetBlockNumber = preEpochLastBlockNumber.compareTo(BigInteger.ZERO) == 0 ? BigInteger.ZERO : preEpochLastBlockNumber.add(BigInteger.ONE);
                curNodes = specialApi.getHistoryVerifierList(web3j, targetBlockNumber);
            }
            if (latestEpoch.compareTo(currentEpoch) == 0) {
                // >>>>If the period of the latest block on the chain == the period of the current block, query the real-time interface
                curNodes = turnClient.getLatestVerifiers();
            }
            curNodes.forEach(n -> n.setNodeId(HexUtil.prefix(n.getNodeId())));
            curVerifiers.clear();
            curVerifiers.addAll(curNodes);

            // The last block number of the previous settlement cycle
            BigInteger preSettleEpochLastBlockNumber = EpochUtil.getPreEpochLastBlockNumber(currentBlockNumber, chainConfig.getSettlePeriodBlockCount());
            // Get from special interface
            EpochInfo epochInfo = specialApi.getEpochInfo(turnClient.getWeb3jWrapper().getWeb3j(), preSettleEpochLastBlockNumber);

            blockReward = epochInfo.getPackageReward();
            // Pledge rewards for each settlement cycle in the current issuance cycle
            settleStakeReward = epochInfo.getStakingReward();
            // Rotation of pledge rewards in the previous settlement cycle
            preStakeReward = stakeReward;
            // Calculate the staking reward for each validator in the current settlement cycle
            stakeReward = handleStakeReward(preSettleEpochLastBlockNumber, currentEpoch.subtract(BigInteger.ONE), epochInfo.getCurStakingReward());
            ConfigChange configChange = new ConfigChange();
            configChange.setAvgPackTime(epochInfo.getAvgPackTime());
            configChange.setBlockReward(epochInfo.getNextPackageReward());
            configChange.setIssueEpoch(epochInfo.getYearNum());
            configChange.setYearStartNum(epochInfo.getYearStartNum());
            configChange.setYearEndNum(epochInfo.getYearEndNum());
            configChange.setRemainEpoch(epochInfo.getRemainEpoch());
            configChange.setSettleStakeReward(epochInfo.getNextStakingReward());
            configChange.setStakeReward(stakeReward);
            epochChanges.offer(configChange);
            applyConfigChange();
        } catch (Exception e) {
            log.error("The settlement cycle change is abnormal and will be retried soon.", e);
            turnClient.updateCurrentWeb3jWrapper();
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Calculate the staking reward at n*10750 block height at n*10750+1 block height
     *
     * @param preSettleEpochLastBlockNumber The last block number of the previous settlement cycle
     * @param preEpoch Previous billing cycle
     * @param preStakingReward The total staking reward in the previous settlement cycle
     * @return: java.math.BigDecimal
     */
    private BigDecimal handleStakeReward(BigInteger preSettleEpochLastBlockNumber, BigInteger preEpoch, BigDecimal preStakingReward) throws Exception {
        List<Node> lastNodes = specialApi.getHistoryVerifierList(turnClient.getWeb3jWrapper().getWeb3j(), preSettleEpochLastBlockNumber);
        BigDecimal stakeReward = preStakingReward.divide(BigDecimal.valueOf(lastNodes.size()), 0, BigDecimal.ROUND_DOWN);
        log.info("Block height [{}]th settlement period [{}], pledge reward [{}] = total pledge reward [{}]/number of validators [{}]", preSettleEpochLastBlockNumber, preEpoch, stakeReward.toPlainString(), preStakingReward.toPlainString(), lastNodes.size());
        return stakeReward;
    }

    /**
     * Get a real-time candidate list
     *
     * @return
     * @throws Exception
     */
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum)
    public List<Node> getCandidates() throws CandidateException {
        log.debug("Get a real-time candidate list:{}()", Thread.currentThread().getStackTrace()[1].getMethodName());
        try {
            CallResponse<List<Node>> br = turnClient.getNodeContract().getCandidateList().send();
            if (!br.isStatusOk()) {
                throw new CandidateException(br.getErrMsg());
            }
            List<Node> candidates = br.getData();
            if (candidates == null) {
                throw new CandidateException("The real-time candidate node list is empty!");
            }
            candidates.forEach(v -> v.setNodeId(HexUtil.prefix(v.getNodeId())));
            return candidates;
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            log.error("", e);
            throw new CandidateException(e.getMessage());
        }
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back.
     *
     * @param e:
     * @return: void
     */
    @Recover
    public List<Node> recoverCandidates(Exception e) {
        log.error("If the retry is completed or the service fails, please contact the administrator for processing.");
        List<Node> candidates = new ArrayList<>();
        return candidates;
    }

    /**
     * Apply configuration changes
     * 1. Update related configuration items involved in BlockChainConfig
     * 2. Update relevant data items involved in the network statistics cache
     */
    public void applyConfigChange() {
        ConfigChange summary = new ConfigChange();
        while (epochChanges.peek() != null) {
            ConfigChange configChange = epochChanges.poll();

            if (configChange.getIssueEpoch() != null) {
                /**
                 * When the year is different, the network proportion needs to be updated.
                 */
                if (chainConfig.getIssueEpochRound() != null && chainConfig.getIssueEpochRound().compareTo(configChange.getIssueEpoch()) != 0) {
                    NetworkStat networkStat = networkStatCache.getNetworkStat();
                    summary.setIssueRates(networkStat.getIssueRates() + Browser.HTTP_SPILT + chainConfig.getAddIssueRate().toPlainString());
                }
                // Update the issuance cycle number
                chainConfig.setIssueEpochRound(configChange.getIssueEpoch());
                summary.setIssueEpoch(configChange.getIssueEpoch());
            }
            if (configChange.getYearStartNum() != null) {
                // Update the starting block number of the issuance cycle
                chainConfig.setIssueEpochStartBlockNumber(configChange.getYearStartNum());
                summary.setYearStartNum(configChange.getYearStartNum());
            }
            if (configChange.getYearEndNum() != null) {
                // Update the end block number of the issuance cycle
                chainConfig.setIssueEpochEndBlockNumber(configChange.getYearEndNum());
                summary.setYearEndNum(configChange.getYearEndNum());
            }

            if (configChange.getYearStartNum() != null && configChange.getYearEndNum() != null) {
                // Update the number of blocks in the issuance cycle
                BigInteger blockCountPerIssue = configChange.getYearEndNum().subtract(configChange.getYearStartNum()).add(BigDecimal.ONE).toBigInteger();
                chainConfig.setAddIssuePeriodBlockCount(blockCountPerIssue);
                // Update the number of settlement cycles for each additional issuance cycle
                chainConfig.setSettlePeriodCountPerIssue(chainConfig.getAddIssuePeriodBlockCount().divide(chainConfig.getSettlePeriodBlockCount()));
            }

            if (configChange.getSettleStakeReward() != null) {
                summary.setSettleStakeReward(configChange.getSettleStakeReward());
            }
            if (configChange.getBlockReward() != null) {
                summary.setBlockReward(configChange.getBlockReward());
            }
            if (configChange.getStakeReward() != null) {
                summary.setStakeReward(configChange.getStakeReward());
            }
            if (configChange.getAvgPackTime() != null) {
                summary.setAvgPackTime(configChange.getAvgPackTime());
            }
        }
        networkStatCache.updateByEpochChange(summary);
    }

}
