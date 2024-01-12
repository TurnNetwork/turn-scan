package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.StakeBusinessMapper;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingKey;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.ppos.StakeExit;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.param.StakeExitParam;
import com.turn.browser.service.ppos.StakeEpochService;
import com.turn.browser.utils.EpochUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Exit stake business parameter converter
 **/
@Slf4j
@Service
public class StakeExitAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private StakeBusinessMapper stakeBusinessMapper;

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private StakeEpochService stakeEpochService;

    /**
     * Revoke pledge (exit validator)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) throws BlockNumberException {
        // Exit Stake
        StakeExitParam txParam = tx.getTxParam(StakeExitParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);

        BigInteger curEpoch;
        try {
            // Calculate the current period
            curEpoch = EpochUtil.getEpoch(BigInteger.valueOf(tx.getNum()), chainConfig.getSettlePeriodBlockCount());
            // Calculate the block number for the pledge return = the number of blocks in each settlement cycle x (the number of settlement cycle rounds in which the pledge is withdrawn + the number of settlement cycle rounds that need to pass)
            BigInteger withdrawBlockNum = chainConfig.getSettlePeriodBlockCount()
                                                     .multiply(curEpoch.add(chainConfig.getUnStakeRefundSettlePeriodCount()));
            txParam.setWithdrawBlockNum(withdrawBlockNum);
        } catch (BlockNumberException e) {
            log.error("", e);
            throw new BusinessException("Period calculation error!");
        }

        // Failed transactions do not analyze business data，增加info信息
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) {
            tx.setInfo(txParam.toJSONString());
            return null;
        }

        StakingKey stakingKey = new StakingKey();
        stakingKey.setNodeId(txParam.getNodeId());
        stakingKey.setStakingBlockNum(txParam.getStakingBlockNum().longValue());
        Staking staking = stakingMapper.selectByPrimaryKey(stakingKey);
        if (staking == null) {
            throw new BusinessException("The node ID is[" + txParam.getNodeId() + "],The stake block number is[" + txParam.getStakingBlockNum() + "]  stake record does not exist！");
        }

        long startTime = System.currentTimeMillis();

        // Update the number of settlement cycles required for unstaking to be credited to the account
        BigInteger unStakeFreezeDuration = stakeEpochService.getUnStakeFreeDuration();
        // Theoretical exit block number, the actual exit block number should be compared with the voting deadline block of the proposal with status in progress, whichever is the largest
        BigInteger unStakeEndBlock = stakeEpochService.getUnStakeEndBlock(txParam.getNodeId(), event.getEpochMessage().getSettleEpochRound(), true);

        StakeExit businessParam = StakeExit.builder()
                                           .nodeId(txParam.getNodeId())
                                           .stakingBlockNum(txParam.getStakingBlockNum())
                                           .time(tx.getTime())
                                           .leaveNum(tx.getNum())
                                           .stakingReductionEpoch(event.getEpochMessage().getSettleEpochRound().intValue())
                                           .unStakeFreezeDuration(unStakeFreezeDuration.intValue())
                                           .unStakeEndBlock(unStakeEndBlock)
                                           .status(CustomStaking.StatusEnum.EXITING.getCode())
                                           .build();

        // Determine whether the current pledge withdrawal operation is in the same settlement cycle as the pledge creation operation. If the same cycle is entered, the node is set to have exited.
        BigInteger stakeEpoch = EpochUtil.getEpoch(BigInteger.valueOf(staking.getStakingBlockNum()), chainConfig.getSettlePeriodBlockCount());
        if (stakeEpoch.compareTo(curEpoch) < 0) {
            // After the pledge is locked, cancel the pledge
            stakeBusinessMapper.lockedExit(businessParam);
        } else {
            // Create pledge and release pledge in the same settlement cycle, exit immediately
            stakeBusinessMapper.unlockExit(businessParam);
        }

        // Query the pledge amount
        BigDecimal stakingValue = staking.getStakingHes().add(staking.getStakingLocked());


        // Supplement txInfo
        txParam.setAmount(stakingValue);
        tx.setInfo(txParam.toJSONString());

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(txParam.getNodeId());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.QUIT.getCode()));
        nodeOpt.setTxHash(tx.getHash());
        nodeOpt.setBNum(tx.getNum());
        nodeOpt.setTime(tx.getTime());
        return nodeOpt;
    }

}
