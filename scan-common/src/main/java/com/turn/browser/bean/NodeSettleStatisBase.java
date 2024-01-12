package com.turn.browser.bean;

import lombok.Data;

import java.math.BigInteger;

@Data
public class NodeSettleStatisBase {

    /**
     *Number of settlement cycle rounds
     */
    private BigInteger settleEpochRound;

    /**
     * Accumulated blocks corresponding to the number of rounds in the settlement cycle
     */
    private BigInteger blockNumGrandTotal;

    /**
     * The number of times a block node is elected
     */
    private BigInteger blockNumElected;

}
