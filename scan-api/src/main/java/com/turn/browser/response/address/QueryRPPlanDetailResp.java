package com.turn.browser.response.address;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;
import java.util.List;

/**
 * The object returned by querying the address lock information
 */
public class QueryRPPlanDetailResp {

    /**
     * Locked balance (unit: ATP)
     */
    private BigDecimal restrictingBalance;

    /**
     * Lock-up pledge\commission (unit: ATP)
     */
    private BigDecimal stakingValue;

    /**
     * Under release (unit: ATP)
     */
    private BigDecimal underReleaseValue;

    /**
     * Lock-up plan
     */
    private List<DetailsRPPlanResp> rpPlans;

    private Long total;

    /**
     * Total locked Value
     */
    private BigDecimal totalValue;

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getRestrictingBalance() {
        return restrictingBalance;
    }

    public void setRestrictingBalance(BigDecimal restrictingBalance) {
        this.restrictingBalance = restrictingBalance;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingValue() {
        return stakingValue;
    }

    public void setStakingValue(BigDecimal stakingValue) {
        this.stakingValue = stakingValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getUnderReleaseValue() {
        return underReleaseValue;
    }

    public void setUnderReleaseValue(BigDecimal underReleaseValue) {
        this.underReleaseValue = underReleaseValue;
    }

    public List<DetailsRPPlanResp> getRpPlans() {
        return rpPlans;
    }

    public void setRpPlans(List<DetailsRPPlanResp> rpPlans) {
        this.rpPlans = rpPlans;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

}
