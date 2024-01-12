package com.turn.browser.dao.param.epoch;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @Description: New block update input parameters
 */
@Data
@Builder
@Accessors(chain = true)
public class NewBlock implements BusinessParam {
    //Block reward (transaction fee)
    private BigDecimal feeRewardValue;
    //Block reward (incentive pool)
    private BigDecimal blockRewardValue;
    //The pledge rewards expected to be obtained during this settlement cycle
    private BigDecimal predictStakingReward;
    //NodeId
    private String nodeId;
    //The block number where the pledge is located
    private BigInteger stakingBlockNum;

    @Override
    public BusinessType getBusinessType () {
        return BusinessType.NEW_BLOCK;
    }
}
