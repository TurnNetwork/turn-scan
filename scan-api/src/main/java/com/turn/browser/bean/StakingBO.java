package com.turn.browser.bean;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StakingBO {

    /**
     * totalStaking
     */
    private BigDecimal totalStakingValue;

    /**
     * Denominator of staking rate
     */
    private BigDecimal stakingDenominator;

}
