package com.turn.browser.response.staking;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

/**
 * Validator statistical parameters return object
 */
public class StakingStatisticNewResp {

    private BigDecimal stakingDelegationValue; // Total number of staking delegations

    private BigDecimal stakingValue; // Total number of pledges

    private BigDecimal delegationValue; // Total number of pledge delegations

    private BigDecimal issueValue; // issuance amount

    private BigDecimal blockReward; // Current block reward

    private BigDecimal stakingReward; // Current staking reward

    private Long currentNumber; // Current block height

    private Long addIssueBegin; // The starting high of the current issuance cycle

    private Long addIssueEnd; // The end block height of the current issuance cycle

    private Long nextSetting; // Countdown to the next settlement cycle

    private BigDecimal availableStaking;// total available pledge

    /**
     * Denominator of pledge = total issuance - real-time incentive pool balance - real-time delegation reward pool contract balance
     */
    private BigDecimal stakingDenominator;

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingDelegationValue() {
        return stakingDelegationValue;
    }

    public void setStakingDelegationValue(BigDecimal stakingDelegationValue) {
        this.stakingDelegationValue = stakingDelegationValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getAvailableStaking() {
        return availableStaking;
    }

    public void setAvailableStaking(BigDecimal availableStaking) {
        this.availableStaking = availableStaking;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingValue() {
        return stakingValue;
    }

    public void setStakingValue(BigDecimal stakingValue) {
        this.stakingValue = stakingValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getIssueValue() {
        return issueValue;
    }

    public void setIssueValue(BigDecimal issueValue) {
        this.issueValue = issueValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getBlockReward() {
        return blockReward;
    }

    public void setBlockReward(BigDecimal blockReward) {
        this.blockReward = blockReward;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingReward() {
        return stakingReward;
    }

    public void setStakingReward(BigDecimal stakingReward) {
        this.stakingReward = stakingReward;
    }

    public Long getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(Long currentNumber) {
        this.currentNumber = currentNumber;
    }

    public Long getAddIssueBegin() {
        return addIssueBegin;
    }

    public void setAddIssueBegin(Long addIssueBegin) {
        this.addIssueBegin = addIssueBegin;
    }

    public Long getAddIssueEnd() {
        return addIssueEnd;
    }

    public void setAddIssueEnd(Long addIssueEnd) {
        this.addIssueEnd = addIssueEnd;
    }

    public Long getNextSetting() {
        return nextSetting;
    }

    public void setNextSetting(Long nextSetting) {
        this.nextSetting = nextSetting;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegationValue() {
        return delegationValue;
    }

    public void setDelegationValue(BigDecimal delegationValue) {
        this.delegationValue = delegationValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingDenominator() {
        return stakingDenominator;
    }

    public void setStakingDenominator(BigDecimal stakingDenominator) {
        this.stakingDenominator = stakingDenominator;
    }

}
