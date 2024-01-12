package com.turn.browser.response.staking;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

/**
 * Validator delegate list return object
 */
public class DelegationListByStakingResp {

    private String delegateAddr; // Principal address

    private BigDecimal delegateValue; // delegate amount

    private BigDecimal delegateTotalValue;//The total amount delegated by the validator

    private BigDecimal delegateLocked; //The locked delegate (ATP) is displayed normally if the associated validator status is normal, and zero if otherwise (delegation)

    private BigDecimal delegateReleased; //The current validator’s delegate is to be redeemed

    private Integer delegateAddrType;

    public String getDelegateAddr() {
        return delegateAddr;
    }

    public void setDelegateAddr(String delegateAddr) {
        this.delegateAddr = delegateAddr;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateValue() {
        return delegateValue;
    }

    public void setDelegateValue(BigDecimal delegateValue) {
        this.delegateValue = delegateValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateTotalValue() {
        return delegateTotalValue;
    }

    public void setDelegateTotalValue(BigDecimal delegateTotalValue) {
        this.delegateTotalValue = delegateTotalValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateLocked() {
        return delegateLocked;
    }

    public void setDelegateLocked(BigDecimal delegateLocked) {
        this.delegateLocked = delegateLocked;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateReleased() {
        return delegateReleased;
    }

    public void setDelegateReleased(BigDecimal delegateReleased) {
        this.delegateReleased = delegateReleased;
    }

    public Integer getDelegateAddrType() {
        return delegateAddrType;
    }

    public void setDelegateAddrType(Integer delegateAddrType) {
        this.delegateAddrType = delegateAddrType;
    }

}
