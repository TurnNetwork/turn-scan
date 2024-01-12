package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Date;

/**
 * @description: Exit Stake storage parameters
 **/
@Data
@Slf4j
@Builder
@Accessors(chain = true)
public class StakeExit
        implements BusinessParam {

    /**
     * Node id
     */
    private String nodeId;

    /**
     * Staking exchange at block height
     */
    private BigInteger stakingBlockNum;

    /**
     * time
     */
    private Date time;

    /**
     * Settlement cycle identifier (the number of settlement cycles in which the pledge transaction is cancelled)
     */
    private int stakingReductionEpoch;

    /**
     * The number of settlement cycles required to un-pledge
     */
    private int unStakeFreezeDuration;

    /**
     * Unlock the last block frozen by staking: the largest of the theoretical end block and the voting end block
     */
    private BigInteger unStakeEndBlock;

    private Integer status;

    /**
     * Block height when exiting
     */
    private Long leaveNum;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.STAKE_EXIT;
    }

}
