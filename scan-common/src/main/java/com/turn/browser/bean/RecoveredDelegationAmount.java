package com.turn.browser.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveredDelegationAmount {

    /**
     * address
     */
    private String delegateAddr;
    /**
     * Pledge to get high quickly
     */
    private Long stakingBlockNum;
    /**
     * Node id
     */
    private String nodeId;
    /**
     * Recovered commission rewards
     */
    private BigDecimal recoveredDelegationAmount;

}
