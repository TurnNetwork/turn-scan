package com.turn.browser.bean.govern;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * @description: Modifiable pledge configuration
 **/
@Builder
public class Staking {

    private BigDecimal stakeThreshold;

    private BigDecimal operatingThreshold;

    private BigDecimal maxValidators;

    private BigDecimal unStakeFreezeDuration;

    private BigDecimal unDelegateFreezeDuration;

    private Integer rewardPerMaxChangeRange;

    private Integer rewardPerChangeInterval;

    public BigDecimal getStakeThreshold() {
        return stakeThreshold;
    }

    public void setStakeThreshold(BigDecimal stakeThreshold) {
        this.stakeThreshold = stakeThreshold;
    }

    public BigDecimal getOperatingThreshold() {
        return operatingThreshold;
    }

    public void setOperatingThreshold(BigDecimal operatingThreshold) {
        this.operatingThreshold = operatingThreshold;
    }

    public BigDecimal getMaxValidators() {
        return maxValidators;
    }

    public void setMaxValidators(BigDecimal maxValidators) {
        this.maxValidators = maxValidators;
    }

    public BigDecimal getUnStakeFreezeDuration() {
        return unStakeFreezeDuration;
    }

    public void setUnStakeFreezeDuration(BigDecimal unStakeFreezeDuration) {
        this.unStakeFreezeDuration = unStakeFreezeDuration;
    }

    public Integer getRewardPerMaxChangeRange() {
        return rewardPerMaxChangeRange;
    }

    public void setRewardPerMaxChangeRange(Integer rewardPerMaxChangeRange) {
        this.rewardPerMaxChangeRange = rewardPerMaxChangeRange;
    }

    public Integer getRewardPerChangeInterval() {
        return rewardPerChangeInterval;
    }

    public void setRewardPerChangeInterval(Integer rewardPerChangeInterval) {
        this.rewardPerChangeInterval = rewardPerChangeInterval;
    }

    public BigDecimal getUnDelegateFreezeDuration() {
        return unDelegateFreezeDuration;
    }

    public void setUnDelegateFreezeDuration(BigDecimal unDelegateFreezeDuration) {
        this.unDelegateFreezeDuration = unDelegateFreezeDuration;
    }

}
