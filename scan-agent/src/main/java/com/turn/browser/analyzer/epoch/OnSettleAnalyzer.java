package com.turn.browser.analyzer.epoch;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.*;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomGasEstimateLogMapper;
import com.turn.browser.dao.custommapper.EpochBusinessMapper;
import com.turn.browser.dao.entity.GasEstimate;
import com.turn.browser.dao.entity.GasEstimateLog;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingExample;
import com.turn.browser.dao.mapper.GasEstimateLogMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.epoch.Settle;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.utils.CalculateUtils;
import com.turn.browser.v0150.service.RestrictingMinimumReleaseParamService;
import com.bubble.contracts.dpos.dto.resp.Node;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
public class OnSettleAnalyzer {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private EpochBusinessMapper epochBusinessMapper;

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private CustomGasEstimateLogMapper customGasEstimateLogMapper;

    @Resource
    private GasEstimateLogMapper gasEstimateLogMapper;

    @Resource
    private RestrictingMinimumReleaseParamService restrictingMinimumReleaseParamService;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<NodeOpt> analyze(CollectionEvent event, Block block) {
        long startTime = System.currentTimeMillis();
        // Operation log list
        List<NodeOpt> nodeOpts = new ArrayList<>();
        if (block.getNum() == 1) {
            return nodeOpts;
        }

        log.debug("Block Number:{}", block.getNum());

        Map<String, Node> curVerifierMap = new HashMap<>();
        event.getEpochMessage().getCurVerifierList().forEach(v -> curVerifierMap.put(v.getNodeId(), v));
        Map<String, Node> preVerifierMap = new HashMap<>();
        event.getEpochMessage().getPreVerifierList().forEach(v -> preVerifierMap.put(v.getNodeId(), v));

        if (event.getEpochMessage().getPreVerifierList().isEmpty()) {
            throw new BusinessException("The validator list of the previous settlement cycle of the current cycle [" + event.getEpochMessage().getSettleEpochRound().intValue() + "]is empty!！");
        }

        Settle settle = Settle.builder()
                              .preVerifierSet(preVerifierMap.keySet())
                              .curVerifierSet(curVerifierMap.keySet())
                              .stakingReward(event.getEpochMessage().getStakeReward())
                              .settingEpoch(event.getEpochMessage().getSettleEpochRound().intValue())
                              .stakingLockEpoch(chainConfig.getUnStakeRefundSettlePeriodCount().intValue())
                              .build();

        List<Integer> statusList = new ArrayList<>();
        statusList.add(CustomStaking.StatusEnum.CANDIDATE.getCode());
        statusList.add(CustomStaking.StatusEnum.EXITING.getCode());
        statusList.add(CustomStaking.StatusEnum.LOCKED.getCode());
        StakingExample stakingExample = new StakingExample();
        stakingExample.createCriteria().andStatusIn(statusList);
        List<Staking> stakingList = stakingMapper.selectByExampleWithBLOBs(stakingExample);
        List<String> exitedNodeIds = new ArrayList<>();
        stakingList.forEach(staking -> {

            //The amount during the hesitation period becomes the locked amount
            staking.setStakingLocked(staking.getStakingLocked().add(staking.getStakingHes()));
            staking.setStakingHes(BigDecimal.ZERO);

            //Exiting record status setting (if the status is exiting and the specified number of settlement cycles has passed, set the status to exited)
            if (staking.getStatus() == CustomStaking.StatusEnum.EXITING.getCode() && // The node status is exiting
                    event.getBlock().getNum() >= staking.getUnStakeEndBlock() // And the current block number is greater than or equal to the actual exit block number expected by the pledge
            ) {
                staking.setStakingReduction(BigDecimal.ZERO);
                staking.setStatus(CustomStaking.StatusEnum.EXITED.getCode());
                staking.setLowRateSlashCount(0);
                exitedNodeIds.add(staking.getNodeId());
            }
            //Locked record status setting (if the status is locked and the specified number of settlement cycles has passed, the status is set to candidate)
            if (staking.getStatus() == CustomStaking.StatusEnum.LOCKED.getCode() && // The node status is locked
                    (staking.getZeroProduceFreezeEpoch() + staking.getZeroProduceFreezeDuration()) < settle.getSettingEpoch()
                // And the current block number is greater than or equal to the actual exit block number expected by the pledge
            ) {
                // The number of low block penalty times is set to 0
                staking.setLowRateSlashCount(0);
                // Abnormal state
                staking.setExceptionStatus(CustomStaking.ExceptionStatusEnum.NORMAL.getCode());
                // Revert from locked state to candidate state
                staking.setStatus(CustomStaking.StatusEnum.CANDIDATE.getCode());
                // 块高恢复
                log.info("Node [{}] reverts from exiting block height [{}] to candidate", staking.getNodeId(), staking.getLeaveNum());
                staking.setLeaveNum(0L);
                recoverLog(staking, settle.getSettingEpoch(), block, nodeOpts);
            }

            //The current pledger is the validator of the last settlement cycle, and the pledge rewards for this settlement cycle are issued. The reward amount is temporarily stored in the stakeReward variable.
            BigDecimal curSettleStakeReward = BigDecimal.ZERO;
            if (settle.getPreVerifierSet().contains(staking.getNodeId())) {
                curSettleStakeReward = settle.getStakingReward();
            }

            // The current pledge is the validator of the next settlement cycle
            if (settle.getCurVerifierSet().contains(staking.getNodeId())) {
                staking.setIsSettle(CustomStaking.YesNoEnum.YES.getCode());
            } else {
                staking.setIsSettle(CustomStaking.YesNoEnum.NO.getCode());
            }

            // Set the total commission reward of the current pledge. The total commission reward taken out from the node is the total commission reward obtained by the current pledge.
            Node node = preVerifierMap.get(staking.getNodeId());
            BigDecimal curTotalDelegateCost = BigDecimal.ZERO;
            if (node != null) {
                staking.setTotalDeleReward(new BigDecimal(node.getDelegateRewardTotal()));
                /**
                 * When the number of commissions queried from the bottom layer is 0, the cost uses the number of commissions in staking.
                 */
                if (BigInteger.ZERO.compareTo(node.getDelegateTotal()) == 0) {
                    curTotalDelegateCost = staking.getStatDelegateLocked().add(staking.getStatDelegateHes());
                } else {
                    curTotalDelegateCost = new BigDecimal(node.getDelegateTotal());
                }
            } else {
                /**
                 * When the number of commissions queried from the bottom layer is 0, the cost uses the number of commissions in staking.
                 */
                curTotalDelegateCost = staking.getStatDelegateLocked().add(staking.getStatDelegateHes());
            }

            // Calculate the annualized rate of node staking
            calcStakeAnnualizedRate(staking, curSettleStakeReward, settle);
            // Calculate the commission annualized rate
            calcDelegateAnnualizedRate(staking, curTotalDelegateCost, settle);
        });
        settle.setStakingList(stakingList);
        settle.setExitNodeList(exitedNodeIds);

        epochBusinessMapper.settle(settle);

        // Update node’s staking rewards
        updateStakingRewardValue(block.getNum() - 1,
                                 event.getEpochMessage().getSettleEpochRound().subtract(BigInteger.ONE),
                                 settle.getPreVerifierSet(),
                                 event.getEpochMessage().getStakeReward());

        List<GasEstimate> gasEstimates = new ArrayList<>();
        preVerifierMap.forEach((k, v) -> {
            GasEstimate ge = new GasEstimate();
            ge.setNodeId(v.getNodeId());
            ge.setSbn(v.getStakingBlockNum().longValue());
            gasEstimates.add(ge);
        });

        // 1、Pledge the node whose cycle number needs to be incremented by 1 first into the mysql database
        Long seq = block.getNum() * 10000;
        List<GasEstimateLog> gasEstimateLogs = new ArrayList<>();
        GasEstimateLog gasEstimateLog = new GasEstimateLog();
        gasEstimateLog.setSeq(seq);
        gasEstimateLog.setJson(JSON.toJSONString(gasEstimates));
        gasEstimateLogs.add(gasEstimateLog);
        customGasEstimateLogMapper.batchInsertOrUpdateSelective(gasEstimateLogs, GasEstimateLog.Column.values());

        if (CollUtil.isNotEmpty(gasEstimates)) {
            epochBusinessMapper.updateGasEstimate(gasEstimates);
        }
        gasEstimateLogMapper.deleteByPrimaryKey(seq);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        try {
            restrictingMinimumReleaseParamService.checkRestrictingMinimumReleaseParam(block);
        } catch (Exception e) {
            log.error("Error checking the effective version on the chain：", e);
        }

        return nodeOpts;
    }

    /**
     * Update node’s staking rewards
     *
     * @param blockNum
     * @param epoch
     * @param preVerifierSet
     * @param stakingRewardValue
     * @return: void
     * @date: 2022/3/8
     */
    private void updateStakingRewardValue(long blockNum, BigInteger epoch, Set<String> preVerifierSet, BigDecimal stakingRewardValue) {
        if (CollUtil.isNotEmpty(preVerifierSet)) {
            List<Staking> stakings = epochBusinessMapper.findStaking(new ArrayList<>(preVerifierSet));
            List<Staking> updateStakingList = new ArrayList<>();
            for (Staking staking : stakings) {
                Staking updateStaking = new Staking();
                updateStaking.setNodeId(staking.getNodeId());
                updateStaking.setStakingBlockNum(staking.getStakingBlockNum());
                updateStaking.setStakingRewardValue(stakingRewardValue);
                updateStakingList.add(updateStaking);
                // MySQL database will round up
                log.info("The settlement period corresponding to the block height [{}] is [{}]--the staking reward of node [{}] is cumulative [{}] = base [{}] + increment [{}]",
                         blockNum,
                         epoch,
                         staking.getNodeId(),
                         staking.getStakingRewardValue()
                                .add(updateStaking.getStakingRewardValue())
                                .setScale(0, BigDecimal.ROUND_HALF_UP)
                                .toPlainString(),
                         staking.getStakingRewardValue().toPlainString(),
                         updateStaking.getStakingRewardValue().toPlainString());
            }
            epochBusinessMapper.settleForStakingValue(updateStakingList);
        }
    }

    /**
     * Calculate the annualized rate of node staking
     *
     * @param staking
     * @param curSettleStakeReward
     * @param settle
     */
    private void calcStakeAnnualizedRate(Staking staking, BigDecimal curSettleStakeReward, Settle settle) {
        // Set the amount after staking rewards are issued, used for annualized rate calculation
        staking.setStakingRewardValue(staking.getStakingRewardValue().add(curSettleStakeReward));
        // Parse Annualized Rate Information Object
        String ariString = staking.getAnnualizedRateInfo();
        AnnualizedRateInfo ari = StringUtils.isNotBlank(ariString) ? JSON.parseObject(ariString, AnnualizedRateInfo.class) : new AnnualizedRateInfo();
        if (ari.getStakeProfit() == null) {
            ari.setStakeProfit(new ArrayList<>());
        }
        if (ari.getStakeCost() == null) {
            ari.setStakeCost(new ArrayList<>());
        }
        if (ari.getSlash() == null) {
            ari.setSlash(new ArrayList<>());
        }

        // By default, the current node is not a validator in the next settlement cycle, and its pledge cost in the next settlement cycle is 0
        BigDecimal curSettleCost = BigDecimal.ZERO;
//        if(settle.getCurVerifierSet().contains(staking.getNodeId())){
        // If the current node is still a validator in the next settlement cycle, record the pledge cost of the next settlement cycle.
        // Calculate the current pledge cost. The cost does not require delegation for the time being.
        curSettleCost = staking.getStakingLocked() // Locked deposit
                               .add(staking.getStakingHes()); // Deposit during the hesitation period

//        }
        // Rotate cost information for next billing cycle
        CalculateUtils.rotateCost(ari.getStakeCost(), curSettleCost, BigInteger.valueOf(settle.getSettingEpoch()), chainConfig);

        // Calculate the annualized rate of current staking START ******************************
        // Lay the foundation START -- so that the total income has a subtractive basis
        layFoundation(ari.getStakeProfit(), settle.getSettingEpoch());

        if (ari.getSlash() == null) {
            ari.setSlash(new ArrayList<>());
        }
        // Lay the foundation END

        // The default node’s income in the previous cycle is zero
        BigDecimal curSettleStakeProfit = BigDecimal.ZERO;
        if (settle.getPreVerifierSet().contains(staking.getNodeId())) {
            // If the current node is in the previous settlement cycle, calculate the real income
            curSettleStakeProfit = staking.getStakingRewardValue() // Staking rewards
                                          .add(staking.getBlockRewardValue()) // + Block reward
                                          .add(staking.getFeeRewardValue()) // + Handling fee reward
                                          .subtract(staking.getTotalDeleReward()); // - Total commission rewards for the current settlement period
        }
        // Rotate the pledge income information and put the income of the current node in the previous cycle into the rotation information.
        CalculateUtils.rotateProfit(ari.getStakeProfit(), curSettleStakeProfit, BigInteger.valueOf(settle.getSettingEpoch() - 1L), chainConfig);
        // Calculate annualized rate
        BigDecimal annualizedRate = CalculateUtils.calculateAnnualizedRate(ari.getStakeProfit(), ari.getStakeCost(), chainConfig);
        // Set annualized rate
        staking.setAnnualizedRate(annualizedRate.doubleValue());
        // Calculate the annualized rate of the current pledge END ******************************

        // Update the original information for annualized rate calculation
        staking.setAnnualizedRateInfo(ari.toJSONString());

        // Set the value of the stakingRewardValue of the current staking to the staking reward value of the current settlement cycle, and the accumulation operation is completed by the SQL statement in mapper xmm
        // staking table: [`staking_reward_value` = `staking_reward_value` + #{staking.stakingRewardValue}]
        // node table: [`stat_staking_reward_value` = `stat_staking_reward_value` + #{staking.stakingRewardValue}]
        staking.setStakingRewardValue(curSettleStakeReward);
    }

    /**
     * Calculate the commission annualized rate
     *
     * @param staking              Current pledge record
     * @param curTotalDelegateCost The total commission amount of the node in the current settlement cycle
     * @param settle               Periodic switching of service parameters
     */
    private void calcDelegateAnnualizedRate(Staking staking, BigDecimal curTotalDelegateCost, Settle settle) {
        //Calculate the commission annualized rate
        // Parse the annual rate information object
        String ariString = staking.getAnnualizedRateInfo();
        AnnualizedRateInfo ari = StringUtils.isNotBlank(ariString) ? JSON.parseObject(ariString, AnnualizedRateInfo.class) : new AnnualizedRateInfo();
        if (ari.getDelegateProfit() == null) {
            ari.setDelegateProfit(new ArrayList<>());
        }
        if (ari.getDelegateCost() == null) {
            ari.setDelegateCost(new ArrayList<>());
        }

        // By default, the current node is not a validator in the next settlement cycle, and its commission cost in the next settlement cycle is 0
        BigDecimal curDelegateCost;

        // If the current node is still a validator in the next settlement cycle, the commission cost of the next settlement cycle will be recorded. The commission cost is based on the curTotalDelegateCost passed in as a parameter.
        curDelegateCost = curTotalDelegateCost;

        // Rotate cost information for next billing cycle
        CalculateUtils.rotateCost(ari.getDelegateCost(), curDelegateCost, BigInteger.valueOf(settle.getSettingEpoch()), chainConfig);

        // Calculate the annualized rate of the current order START ******************************
        layFoundation(ari.getDelegateProfit(), settle.getSettingEpoch());

        // The default node’s commission income in the previous cycle is zero.
        BigDecimal curSettleDelegateProfit = BigDecimal.ZERO;
        if (settle.getPreVerifierSet().contains(staking.getNodeId())) {
            curSettleDelegateProfit = staking.getTotalDeleReward();
        }
        // Rotate entrustment income information, put the entrustment income of the current node in the previous cycle into the rotation information
        CalculateUtils.rotateProfit(ari.getDelegateProfit(), curSettleDelegateProfit, BigInteger.valueOf(settle.getSettingEpoch() - 1L), chainConfig);
        // Calculate annualized rate
        BigDecimal annualizedRate = CalculateUtils.calculateAnnualizedRate(ari.getDelegateProfit(), ari.getDelegateCost(), chainConfig);
        // Set the annualized rate of delegation rewards in the previous cycle to the preDeleAnnualizedRate field
        staking.setPreDeleAnnualizedRate(staking.getDeleAnnualizedRate());
        // Set the annual rate of delegation rewards for the current pledge record
        staking.setDeleAnnualizedRate(annualizedRate.doubleValue());
        // Calculate the annualized rate of the current pledge END ******************************
        NodeApr nodeApr = NodeApr.build(settle.getSettingEpoch(), staking.getNodeId(), annualizedRate, staking.getNodeApr());
        staking.setNodeApr(JSONUtil.toJsonStr(nodeApr));
        // Update the original information for annualized rate calculation
        staking.setAnnualizedRateInfo(ari.toJSONString());
    }

    // lay foundation
    private void layFoundation(List<PeriodValueElement> pves, int settleEpoch) {
        // lay foundation START -- In this way, the total income has a basis for subtraction
        if (pves.isEmpty()) {
            // Set 0 returns as the subtrahend of the calculation period returns
            PeriodValueElement pv = new PeriodValueElement();
            // For example, it is the 6th period. To get the profit sum of the 5th and 6th periods, you need to record the profit at the end of the 4th period, so that you can use [Profit at the end of the 6th period]-[4th period] Profit at the end of the period] Calculate the sum of the profits of the two periods 5 and 6
            pv.setPeriod(settleEpoch - 2L);
            pv.setValue(BigDecimal.ZERO);
            pves.add(pv);
        }
        // lay foundation END
    }

    /**
     * Node recovery log
     *
     * @param staking
     * @param settingEpoch
     * @param block
     * @param nodeOpts
     */
    private void recoverLog(Staking staking, int settingEpoch, Block block, List<NodeOpt> nodeOpts) {
        String desc = NodeOpt.TypeEnum.UNLOCKED.getTpl()
                                               .replace("LOCKED_EPOCH", staking.getZeroProduceFreezeEpoch().toString())
                                               .replace("UNLOCKED_EPOCH", String.valueOf(settingEpoch))
                                               .replace("FREEZE_DURATION", staking.getZeroProduceFreezeDuration().toString());
        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(staking.getNodeId());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.UNLOCKED.getCode()));
        nodeOpt.setBNum(block.getNum());
        nodeOpt.setTime(block.getTime());
        nodeOpt.setDesc(desc);
        nodeOpts.add(nodeOpt);
    }

}
