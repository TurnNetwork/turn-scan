package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
public class Report implements BusinessParam {
    //Report evidence
    private String slashData;
    //NodeId
    private String nodeId;
    //Transaction hash
    private String txHash;
    //time
    private Date time;
    //Round up by (block_number/number of blocks produced in each settlement cycle)
    private int settingEpoch;
    //The pledge exchange is at block height
    private BigInteger stakingBlockNum;
    //Double signing penalty ratio
    private BigDecimal slashRate;
    //The proportion of penalties allocated to whistleblowers
    private BigDecimal slash2ReportRate;
    //transaction sender
    private String benefitAddr;
    //The remaining pledge amount after the double-signing penalty. Because the node is set to exit after the double-signing penalty, all amounts will be moved to the pending redemption field.
    private BigDecimal codeRemainRedeemAmount;
    //amount of reward
    private BigDecimal codeRewardValue;
    //node status
    private int codeStatus;
    //Currently exiting
    private int codeStakingReductionEpoch;
    //The amount of punishment
    private BigDecimal codeSlashValue;
    //The number of settlement cycles required to release the pledge
    private int unStakeFreezeDuration;
    //Unlock the last block frozen by the pledge: the largest of the theoretical end block and the voting end block
    private BigInteger unStakeEndBlock;
    //Double-signed block
    private Long blockNum;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.REPORT;
    }
}
