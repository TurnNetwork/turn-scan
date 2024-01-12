package com.turn.browser.response.microNode;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * Micro node statistical parameter return object
 */
public class MicroNodeStatisticResp {


    /**
     * Total number of pledges
     */
    private BigDecimal stakingValue;


    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingValue() {
        return stakingValue;
    }

    public void setStakingValue(BigDecimal stakingValue) {
        this.stakingValue = stakingValue;
    }

}
