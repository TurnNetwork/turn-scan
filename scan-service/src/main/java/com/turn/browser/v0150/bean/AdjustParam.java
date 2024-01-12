package com.turn.browser.v0150.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Pledge or entrusted account adjustment entries
 */
@Data
public class AdjustParam {
    //Adjust the block number at the time
    private BigInteger currBlockNum;
    //Number of blocks in settlement cycle
    private BigInteger settleBlockCount;
    // block time
    private Date blockTime;
    /*Adjustment parameters returned by special nodes*/
    private String optType;
    private String nodeId;
    private String stakingBlockNum;
    private String addr;
    private BigDecimal lock;
    private BigDecimal hes;
    private BigDecimal reward;

    /*Delegation and pledge adjustment general attributes*/
    private BigDecimal nodeTotalValue; //The total number of valid pledge delegations of the node

    /*Delegated adjustment of special attributes*/
    private int isHistory; // Delegate status: whether it is history: 1 yes, 2 no
    private BigDecimal delegateHes; // Unlocked delegate amount
    private BigDecimal delegateLocked; // The delegate amount has been locked
    private BigDecimal delegateReleased; // Amount to be withdrawn
    private BigDecimal nodeStatDelegateValue; // The effective delegation amount of the node
    private BigDecimal nodeStatDelegateReleased; // The commission amount to be withdrawn by the node
    private BigDecimal nodeHaveDeleReward; // All pledges have received delegation rewards
    private BigDecimal stakeStatDelegateHes; // Stake the unlocked delegation
    private BigDecimal stakeStatDelegateLocked; // Delegate for pledge locking
    private BigDecimal stakeStatDelegateReleased; // Delegate pledged to be withdrawn
    private BigDecimal stakeHaveDeleReward; // The current stake has received the delegation reward

    /*Special properties for pledge adjustment*/
    private int status; //Node status: 1 candidate, 2 exiting, 3 exited, 4 locked
    private int isConsensus; // Whether it is in the consensus cycle
    private int isSettle; // Whether it is in the settlement cycle
    private BigDecimal stakingHes; // Deposit during the hesitation period
    private BigDecimal stakingLocked; // Deposit during the locking period
    private BigDecimal stakingReduction; // Refunding the pledge deposit

    private int stakingReductionEpoch; // Return settlement cycle identifier
    private int unStakeFreezeDuration; // The number of settlement cycles theoretically locked for unstaking
    private Long unStakeEndBlock; // Unlock the last block frozen by pledge
    private Date leaveTime; //Exit time
}
