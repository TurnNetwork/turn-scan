package com.turn.browser.analyzer.epoch;

import cn.hutool.core.bean.BeanUtil;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.bean.CusSlash;
import com.turn.browser.bean.CustomStaking.StatusEnum;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.EpochBusinessMapper;
import com.turn.browser.dao.custommapper.SlashBusinessMapper;
import com.turn.browser.dao.entity.Slash;
import com.turn.browser.dao.entity.SlashExample;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingKey;
import com.turn.browser.dao.mapper.SlashMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.epoch.Consensus;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.service.proposal.ProposalParameterService;
import com.turn.browser.service.statistic.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OnConsensusAnalyzer {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private EpochBusinessMapper epochBusinessMapper;

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private SlashBusinessMapper slashBusinessMapper;

    @Resource
    private ProposalParameterService proposalParameterService;

    @Resource
    private StatisticService statisticService;

    @Resource
    private SlashMapper slashMapper;

    public Optional<List<NodeOpt>> analyze(CollectionEvent event, Block block) {
        long startTime = System.currentTimeMillis();

        log.debug("Block Number:{}", block.getNum());

        // Get the node ID list for the next consensus cycle
        List<String> nextConsNodeIdList = new ArrayList<>();
        event.getEpochMessage().getCurValidatorList().forEach(v -> nextConsNodeIdList.add(v.getNodeId()));
        // 每个共识周期的期望出块数
        BigInteger expectBlockNum = chainConfig.getConsensusPeriodBlockCount().divide(BigInteger.valueOf(nextConsNodeIdList.size()));
        Consensus consensus = Consensus.builder()
                                       .expectBlockNum(expectBlockNum)
                                       .validatorList(nextConsNodeIdList) // In sql, if the node is in the next consensus cycle list, the consensus status is set to 1, otherwise it is 2
                                       .build();
        epochBusinessMapper.consensus(consensus);

        // Get the list of all reported node IDs in the double signature parameter cache
        SlashExample slashExample = new SlashExample();
        slashExample.createCriteria().andIsHandleEqualTo(false);
        List<Slash> reportedNodeIdList = slashMapper.selectByExampleWithBLOBs(slashExample);
        // If the reported node is not in the next consensus cycle, safe punishment operations can be performed on it
        List<String> notInNextConsNodeIdList = new ArrayList<>();
        reportedNodeIdList.forEach(slash -> {
            if (!nextConsNodeIdList.contains(slash.getNodeId())) notInNextConsNodeIdList.add(slash.getNodeId());
        });

        List<NodeOpt> nodeOpts = new ArrayList<>();
        if (!notInNextConsNodeIdList.isEmpty()) {
            // Punishment will be based on the data in the database.
            List<Staking> slashList = slashBusinessMapper.getException(notInNextConsNodeIdList);
            if (!slashList.isEmpty()) {
                slashList.forEach(slashNode -> {
                    SlashExample slashNodeIdExample = new SlashExample();
                    slashNodeIdExample.createCriteria().andNodeIdEqualTo(slashNode.getNodeId()).andIsHandleEqualTo(false);
                    List<Slash> reportList = slashMapper.selectByExampleWithBLOBs(slashNodeIdExample);
                    reportList.forEach(report -> {
                        // Penalty node
                        NodeOpt nodeOpt = slashNode(report, block);
                        nodeOpts.add(nodeOpt);
                    });
                    //Penalize proposal data
                    proposalParameterService.setSlashParameters(slashNode.getNodeId());
                });
            }
        }

        statisticService.nodeSettleStatisElected(event);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return Optional.ofNullable(nodeOpts);
    }

    /**
     * Penalty node
     *
     * @param copyBusinessParam:
     * @param block:
     * @return: com.turn.browser.elasticsearch.dto.NodeOpt
     */
    private NodeOpt slashNode(Slash copyBusinessParam, Block block) {
        CusSlash businessParam = new CusSlash();
        BeanUtil.copyProperties(copyBusinessParam, businessParam);
        /**
         * Handle double-signing penalties
         * important! ! ! ! ! ! : Once a node is punished for double signing, all the amount of the node will become pending redemption.
         * The locked amount will be set to 0
         * */
        // Query qualified pledge records based on node ID and pledge block number
        StakingKey stakingKey = new StakingKey();
        stakingKey.setNodeId(businessParam.getNodeId());
        stakingKey.setStakingBlockNum(businessParam.getStakingBlockNum());
        Staking staking = stakingMapper.selectByPrimaryKey(stakingKey);
        // Only one of the locked amount and the amount to be redeemed will have value, so the locked or redeemed amount is used as the basis for calculating the penalty.
        BigDecimal baseAmount = staking.getStakingLocked();
        if (baseAmount.compareTo(BigDecimal.ZERO) == 0) {
            baseAmount = staking.getStakingReduction();
        }
        //Amount of penalty = base x penalty ratio
        BigDecimal codeSlashValue = baseAmount.multiply(businessParam.getSlashRate());
        //Amount of reward = penalty x reward ratio
        BigDecimal codeRewardValue = codeSlashValue.multiply(businessParam.getSlashReportRate());
        // Calculate the amount remaining to be redeemed after deducting the penalty amount
        BigDecimal codeRemainRedeemAmount = BigDecimal.ZERO;
        if (staking.getStakingLocked().compareTo(BigDecimal.ZERO) > 0) {
            codeRemainRedeemAmount = staking.getStakingLocked().subtract(codeSlashValue);
        }
        /**
         * If the node status is exiting, reduction is required.
         * Because all the money of the node in the exiting state is in the redemption state
         */
        if (staking.getStatus().intValue() == StatusEnum.EXITING.getCode()) {
            codeRemainRedeemAmount = staking.getStakingReduction().subtract(codeSlashValue);
        }
        if (codeRemainRedeemAmount.compareTo(BigDecimal.ZERO) >= 0) {
            // Set the node status to exiting
            businessParam.setCodeStatus(2);
            // Set the period for the exit operation
            businessParam.setCodeStakingReductionEpoch(businessParam.getSettingEpoch());
            businessParam.setLeaveNum(block.getNum());
        } else {
            // The node status is set to exited
            businessParam.setCodeStatus(3);
            businessParam.setCodeStakingReductionEpoch(0);
            // Set to 0 if the result of the deduction is less than 0
            codeRemainRedeemAmount = BigDecimal.ZERO;
        }
        businessParam.setCodeRewardValue(codeRewardValue);
        businessParam.setCodeRemainRedeemAmount(codeRemainRedeemAmount);
        businessParam.setCodeSlashValue(codeSlashValue);
        slashBusinessMapper.slashNode(businessParam);
        //Operation description: 6【PERCENT|AMOUNT】
        String desc = NodeOpt.TypeEnum.MULTI_SIGN.getTpl()
                                                 .replace("PERCENT", chainConfig.getDuplicateSignSlashRate().toString())
                                                 .replace("AMOUNT", codeSlashValue.toString());
        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(businessParam.getNodeId());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.MULTI_SIGN.getCode()));
        nodeOpt.setDesc(desc);
        nodeOpt.setTxHash(businessParam.getTxHash());
        nodeOpt.setBNum(businessParam.getBlockNum());
        nodeOpt.setTime(block.getTime());
        return nodeOpt;
    }

}
