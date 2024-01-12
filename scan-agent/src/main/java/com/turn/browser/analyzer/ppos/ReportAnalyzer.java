package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.SlashBusinessMapper;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.entity.Slash;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.dao.mapper.SlashMapper;
import com.turn.browser.dao.param.ppos.Report;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.ReportParam;
import com.turn.browser.service.ppos.StakeEpochService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: Report validator business parameter converter
 **/
@Slf4j
@Service
public class ReportAnalyzer extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private SlashBusinessMapper slashBusinessMapper;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private StakeEpochService stakeEpochService;

    @Resource
    private SlashMapper slashMapper;

    /**
     * Report multi-signature (report verifier)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        // Report information
        ReportParam txParam = tx.getTxParam(ReportParam.class);
        if (null == txParam) return null;
        Node staking = nodeMapper.selectByPrimaryKey(txParam.getVerify());
        if (staking != null) {
            // Backfill the penalty reward information in the setting parameters
            //Penalty amount Assuming the locked amount is 0, obtain the amount to be redeemed
            BigDecimal stakingAmount = staking.getStakingLocked();
            if (stakingAmount.compareTo(BigDecimal.ZERO) == 0) {
                stakingAmount = staking.getStakingReduction();
            }
            //amount of reward
            BigDecimal codeRewardValue = stakingAmount.multiply(chainConfig.getDuplicateSignSlashRate()).multiply(chainConfig.getDuplicateSignRewardRate());
            txParam.setReward(codeRewardValue);
        }

        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) return null;

        long startTime = System.currentTimeMillis();

        // If the report is successful, first set the node as abnormal, and subsequent punishment operations will be executed when the consensus cycle switches.
        List<String> nodeIdList = new ArrayList<>();
        nodeIdList.add(txParam.getVerify());
        slashBusinessMapper.setException(txParam.getVerify(), txParam.getStakingBlockNum().longValue());

        // Update the number of settlement cycles required for unstaking to be credited to the account
        BigInteger unStakeFreezeDuration = stakeEpochService.getUnStakeFreeDuration();

        Long blockNum = event.getBlock().getNum() - (event.getBlock().getNum() % chainConfig.getConsensusPeriodBlockCount().longValue()) + chainConfig.getConsensusPeriodBlockCount().longValue();
        // Theoretical exit block number, the actual exit block number should be compared with the voting deadline block of the proposal with status in progress, whichever is the largest
        BigInteger unStakeEndBlock = stakeEpochService.getUnStakeEndBlock(txParam.getVerify(), event.getEpochMessage().getSettleEpochRound(), true);
        Report businessParam = Report.builder()
                                     .slashData(txParam.getData())
                                     .nodeId(txParam.getVerify())
                                     .txHash(tx.getHash())
                                     .time(tx.getTime())
                                     .stakingBlockNum(txParam.getStakingBlockNum())
                                     .slashRate(chainConfig.getDuplicateSignSlashRate())
                                     .benefitAddr(tx.getFrom())
                                     .slash2ReportRate(chainConfig.getDuplicateSignRewardRate())
                                     .settingEpoch(event.getEpochMessage().getSettleEpochRound().intValue())
                                     .unStakeFreezeDuration(unStakeFreezeDuration.intValue())
                                     .unStakeEndBlock(unStakeEndBlock)
                                     .blockNum(blockNum)
                                     .build();

        /**
         * The number of pledge lock cycles needs to be updated only when the first candidate is punished.
         */
        if (staking != null && staking.getStatus().intValue() == CustomStaking.StatusEnum.CANDIDATE.getCode()) {
            //Update the number of cycles it takes for the node to withdraw the pledge.
            slashBusinessMapper.updateUnStakeFreezeDuration(businessParam);
        }

        // Temporarily cache the reporting parameters and wait for processing when the consensus cycle switches.
        Slash slash = new Slash();
        slash.setNodeId(businessParam.getNodeId());
        slash.setTxHash(businessParam.getTxHash());
        slash.setTime(businessParam.getTime());
        slash.setSettingEpoch(businessParam.getSettingEpoch());
        slash.setStakingBlockNum(businessParam.getStakingBlockNum().longValue());
        slash.setSlashRate(businessParam.getSlashRate());
        slash.setSlashReportRate(businessParam.getSlash2ReportRate());
        slash.setBenefitAddress(businessParam.getBenefitAddr());
        slash.setUnStakeFreezeDuration(businessParam.getUnStakeFreezeDuration());
        slash.setUnStakeEndBlock(businessParam.getUnStakeEndBlock().longValue());
        slash.setBlockNum(businessParam.getBlockNum());
        slash.setIsHandle(false);
        slash.setSlashData(businessParam.getSlashData());
        slashMapper.insertSelective(slash);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return null;
    }

}
