package com.turn.browser.analyzer.epoch;

import cn.hutool.json.JSONUtil;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.bean.CustomStaking.StatusEnum;
import com.turn.browser.bean.HistoryLowRateSlash;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.EpochBusinessMapper;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingExample;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.epoch.Election;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.service.ppos.StakeEpochService;
import com.turn.browser.utils.EpochUtil;
import com.turn.browser.utils.HexUtil;
import com.bubble.protocol.Web3j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * election
 */
@Slf4j
@Service
public class OnElectionAnalyzer {

    @Resource
    private EpochBusinessMapper epochBusinessMapper;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private SpecialApi specialApi;

    @Resource
    private TurnClient turnClient;

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private StakeEpochService stakeEpochService;

    public List<NodeOpt> analyze(CollectionEvent event, Block block) {
        long startTime = System.currentTimeMillis();
        // Operation log list
        List<NodeOpt> nodeOpts = new ArrayList<>();
        try {
            Web3j web3j = turnClient.getWeb3jWrapper().getWeb3j();
            List<HistoryLowRateSlash> slashList = specialApi.getHistoryLowRateSlashList(web3j, BigInteger.valueOf(block.getNum()));
            if (!slashList.isEmpty()) {
                List<String> slashNodeIdList = new ArrayList<>();
                // Unified node ID format: starting with 0x
                slashList.forEach(n -> slashNodeIdList.add(HexUtil.prefix(n.getNodeId())));
                log.info("Low block-producing nodes queried by special nodes:{}", slashNodeIdList);
                // Query node
                StakingExample stakingExample = new StakingExample();
                List<Integer> status = new ArrayList<>();
                status.add(StatusEnum.CANDIDATE.getCode());
                status.add(StatusEnum.EXITING.getCode());
                stakingExample.createCriteria().andStatusIn(status).andNodeIdIn(slashNodeIdList);
                List<Staking> slashStakingList = stakingMapper.selectByExample(stakingExample);
                if (slashStakingList.isEmpty() || slashStakingList.size() < slashNodeIdList.size()) {
                    log.warn("Nodes with low block production rates queried by special nodes[" + JSONUtil.toJsonStr(slashNodeIdList) + "]The corresponding candidate node data cannot be queried in the staking table.!");
                } else {
                    //Penalize nodes with low block production rate;
                    BigInteger curSettleEpoch = EpochUtil.getEpoch(BigInteger.valueOf(block.getNum()), chainConfig.getSettlePeriodBlockCount());
                    List<NodeOpt> exceptionNodeOpts = slash(event, block, curSettleEpoch.intValue(), slashStakingList);
                    nodeOpts.addAll(exceptionNodeOpts);
                    log.info("Block height[{}]Settlement period[{}]Consensus period[{}]The list of punished nodes is：{}",
                             block.getNum(),
                             event.getEpochMessage().getSettleEpochRound(),
                             event.getEpochMessage().getConsensusEpochRound(),
                             JSONUtil.toJsonStr(slashStakingList));
                }
            }
        } catch (Exception e) {
            log.error("OnElectionConverter error", e);
            throw new BusinessException(e.getMessage());
        }
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        return nodeOpts;
    }

    /**
     * Penalty node
     *
     * @param block
     * @param settleEpoch   Current billing cycle
     * @param slashNodeList List of punished nodes
     * @return
     */
    private List<NodeOpt> slash(CollectionEvent event, Block block, int settleEpoch, List<Staking> slashNodeList) {
        // Update the number of locked billing cycles
        BigInteger zeroProduceFreezeDuration = stakeEpochService.getZeroProduceFreeDuration();
        slashNodeList.forEach(staking -> {
            staking.setZeroProduceFreezeDuration(zeroProduceFreezeDuration.intValue());
        });
        // Penalty node
        Election election = Election.builder().settingEpoch(settleEpoch).zeroProduceFreezeEpoch(settleEpoch) // 记录零出块被惩罚时所在结算周期
                                    .zeroProduceFreezeDuration(zeroProduceFreezeDuration.intValue()) //记录此刻的零出块锁定周期数
                                    .build();

        // Node operation log
        BigInteger bNum = BigInteger.valueOf(block.getNum());

        List<NodeOpt> nodeOpts = new ArrayList<>();
        List<Staking> lockedNodes = new ArrayList<>();
        List<Staking> exitingNodes = new ArrayList<>();
        for (Staking staking : slashNodeList) {
            if (staking.getLowRateSlashCount() > 0) {
                // If you have been punished once for low block production, you will no longer be punished.
                continue;
            }
            CustomStaking customStaking = new CustomStaking();
            BeanUtils.copyProperties(staking, customStaking);
            StringBuffer desc = new StringBuffer("0|");
            /**
             * If the penalty for low block production is not equal to 0, the penalty amount needs to be configured.
             */
            BigDecimal slashAmount = event.getEpochMessage().getBlockReward().multiply(chainConfig.getSlashBlockRewardCount());
            customStaking.setSlashAmount(slashAmount);
            desc.append(chainConfig.getSlashBlockRewardCount().toString()).append("|").append(slashAmount.toString()).append("|1");
            NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
            nodeOpt.setNodeId(staking.getNodeId());
            nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.LOW_BLOCK_RATE.getCode()));
            nodeOpt.setBNum(bNum.longValue());
            nodeOpt.setTime(block.getTime());
            nodeOpt.setDesc(desc.toString());

            // Update each field of the node instance according to the different status of the node.
            if (StatusEnum.EXITING == StatusEnum.getEnum(staking.getStatus())) {
                // If the node was in exit status before, all its money has been redeemed, so the penalty amount will be deducted from the redemption.
                //The total pledge + commission statistics fields must also be updated
                election.setUnStakeFreezeDuration(customStaking.getUnStakeFreezeDuration());
                election.setUnStakeEndBlock(BigInteger.valueOf(customStaking.getUnStakeEndBlock()));
                exitingNodes.add(customStaking);
            }
            if (StatusEnum.CANDIDATE == StatusEnum.getEnum(staking.getStatus())) {
                // If the node is in the candidate, the penalty amount is deducted from the pledge in [hesitate + lock]
                BigDecimal remainStakingAmount = staking.getStakingHes().add(staking.getStakingLocked()).subtract(slashAmount);
                // If the [hesitate + lock] deposit after deducting the penalty amount is less than the pledge threshold, the node will be set to exit.
                if (remainStakingAmount.compareTo(chainConfig.getStakeThreshold()) < 0) {
                    // Update the number of settlement cycles required for unStaking to be credited to the account
                    BigInteger unStakeFreezeDuration = stakeEpochService.getUnStakeFreeDuration();
                    // Low block generation does not need to pay attention to the validity period of the comparison proposal.
                    BigInteger unStakeEndBlock = stakeEpochService.getUnStakeEndBlock(staking.getNodeId(),
                                                                                      event.getEpochMessage().getSettleEpochRound(),
                                                                                      false);
                    election.setUnStakeFreezeDuration(unStakeFreezeDuration.intValue());
                    election.setUnStakeEndBlock(unStakeEndBlock);
                    customStaking.setLeaveNum(block.getNum());
                    exitingNodes.add(customStaking);
                    log.info("Block height [{}] settlement cycle [{}] consensus cycle [{}], node [{}] after deducting the penalty amount [hesitate + lock] the pledge deposit is less than the pledge threshold, the node is set to exit",
                             event.getBlock().getNum(),
                             event.getEpochMessage().getSettleEpochRound(),
                             event.getEpochMessage().getConsensusEpochRound(),
                             customStaking.getNodeId());
                } else {
                    customStaking.setLeaveNum(block.getNum());
                    customStaking.setStatus(StatusEnum.LOCKED.getCode());
                    // Lock node
                    if (customStaking.getStakingHes().compareTo(slashAmount) >= 0) {
                        // Because it's enough
                        BigDecimal remainStakingHes = customStaking.getStakingHes().subtract(slashAmount);
                        customStaking.setStakingHes(remainStakingHes);
                    } else {
                        // Since there are not enough deductions, the remaining amount will be deducted from the locked pledge
                        // The amount that needs to be subtracted from the pledge during the lock-up period
                        BigDecimal diffAmount = slashAmount.subtract(customStaking.getStakingHes());
                        customStaking.setStakingHes(BigDecimal.ZERO);
                        // Remaining pledge during lock-up period
                        BigDecimal lockedAmount = customStaking.getStakingLocked().subtract(diffAmount);
                        customStaking.setStakingLocked(lockedAmount);
                    }
                    lockedNodes.add(customStaking);
                }
            }
            // Set the time to leave the validator list
            customStaking.setLeaveTime(block.getTime());
            // Penalty times for low block production +1
            customStaking.setLowRateSlashCount(staking.getLowRateSlashCount() + 1);
            nodeOpts.add(nodeOpt);
        }
        election.setLockedNodeList(lockedNodes);
        election.setExitingNodeList(exitingNodes);
        epochBusinessMapper.slashNode(election);
        return nodeOpts;
    }

}
