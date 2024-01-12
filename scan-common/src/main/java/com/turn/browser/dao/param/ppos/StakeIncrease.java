package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Increase and Stake storage parameters
 **/
@Data
@Slf4j
@Builder
@Accessors(chain = true)
public class StakeIncrease implements BusinessParam {
    //NodeId
    private String nodeId;
    //Amount of increased holdings
    private BigDecimal amount;
    //The pledge exchange is at block height
    private BigInteger stakingBlockNum;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.STAKE_INCREASE;
    }
}
