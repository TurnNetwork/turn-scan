package com.turn.browser.v0150.context;

import com.turn.browser.bean.CustomStaking;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.utils.EpochUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Pledge adjustment context
 *Required data:
 * 1. Pledge information
 * 2. Node information
 */
@Slf4j
@Data
public class StakingAdjustContext extends AbstractAdjustContext {
    /**
     * Verify whether the relevant amount of pledge meets the adjustment requirements
     */
    @Override
    void validateAmount(){
        // Before verifying whether the amount is correct, check whether the necessary data exists
        if (!errors.isEmpty()) return;
        if(
                adjustParam.getStatus()==CustomStaking.StatusEnum.EXITING.getCode()
                        ||adjustParam.getStatus()==CustomStaking.StatusEnum.EXITED.getCode()
        ){
            // For nodes that are exiting or have exited, determine whether the stakingReduction amount is sufficient.
            if(adjustParam.getStakingReduction().compareTo(adjustParam.getHes().add(adjustParam.getLock()))<0){
                errors.add("[Error]: The pledge record [refunded amount ["+adjustParam.getStakingReduction()+"]] is less than the adjustment parameter [hesitation period ["+adjustParam.getHes()+"] + locking period [" +adjustParam.getLock()+"】]Amount【"+adjustParam.getHes().add(adjustParam.getLock())+"】!");
            }
        }else{
            // Candidate or locked nodes, determine whether the hesitation period and locking period amount is enough to deduct
            if(adjustParam.getStakingHes().compareTo(adjustParam.getHes())<0){
                errors.add("[Error]: The pledge record [the amount during the hesitation period ["+adjustParam.getStakingHes()+"]] is less than the adjustment parameter [the amount during the hesitation period ["+adjustParam.getHes()+"]]!") ;
            }
            if(adjustParam.getStakingLocked().compareTo(adjustParam.getLock())<0){
                errors.add("[Error]: The pledge record [locking period amount ["+adjustParam.getStakingLocked()+"]] is less than the adjustment parameter [locking period amount ["+adjustParam.getLock()+"]]!") ;
            }
        }
    }

    @Override
    void calculateAmountAndStatus() throws BlockNumberException {
        if(
            adjustParam.getStatus()==CustomStaking.StatusEnum.EXITING.getCode()
            ||adjustParam.getStatus()==CustomStaking.StatusEnum.EXITED.getCode()
        ){
            // For nodes that are exiting or have exited, subtract hes and lock from stakingReduction
            adjustParam.setStakingReduction(
                    adjustParam.getStakingReduction()
                            .subtract(adjustParam.getHes())
                            .subtract(adjustParam.getLock())
            );
        }else{
            // Candidate or locked nodes will be deducted from their respective hesitation or locking period amounts.
            adjustParam.setStakingHes(adjustParam.getStakingHes().subtract(adjustParam.getHes()));
            adjustParam.setStakingLocked(adjustParam.getStakingLocked().subtract(adjustParam.getLock()));
            // Subtract hes and lock from the node's total effective pledge and delegation totalValue
            adjustParam.setNodeTotalValue(
                adjustParam.getNodeTotalValue()
                .subtract(adjustParam.getHes())
                .subtract(adjustParam.getLock())
            );

            // After deducting the staking-related amount, if (stakingHes+stakingLocked) < staking threshold, the node status is set to exiting, the locking period is set to 1, and the unlocking block number is set to the last block number of this cycle
            BigDecimal stakingHes = adjustParam.getStakingHes();
            BigDecimal stakingLocked = adjustParam.getStakingLocked();
            if(stakingHes.add(stakingLocked).compareTo(chainConfig.getStakeThreshold())<0){
                adjustParam.setStatus(CustomStaking.StatusEnum.EXITING.getCode());
                adjustParam.setIsConsensus(CustomStaking.YesNoEnum.NO.getCode());
                adjustParam.setIsSettle(CustomStaking.YesNoEnum.NO.getCode());
                //Move the lock-in period amount to the returning field
                adjustParam.setStakingReduction(adjustParam.getStakingReduction().add(stakingLocked));
                // Set the hesitation period and lock-in period amounts to 0
                adjustParam.setStakingHes(BigDecimal.ZERO);
                adjustParam.setStakingLocked(BigDecimal.ZERO);
                // Set the settlement period when exiting
                BigInteger epoch = EpochUtil.getEpoch(adjustParam.getCurrBlockNum(),adjustParam.getSettleBlockCount());
                adjustParam.setStakingReductionEpoch(epoch.intValue());
                //Unlock block number = the last block number of the next settlement cycle
                BigInteger futureBlockNum = adjustParam.getCurrBlockNum().add(adjustParam.getSettleBlockCount());
                BigInteger unStakeEndBlock = EpochUtil.getCurEpochLastBlockNumber(futureBlockNum,adjustParam.getSettleBlockCount());
                adjustParam.setUnStakeEndBlock(unStakeEndBlock.longValue());
                //Set the exit time to the current block time and the freezing period number to 1
                adjustParam.setLeaveTime(adjustParam.getBlockTime());
                adjustParam.setUnStakeFreezeDuration(1);
            }
        }
    }

    @Override
    String extraContextInfo() {
        return null;
    }
}
