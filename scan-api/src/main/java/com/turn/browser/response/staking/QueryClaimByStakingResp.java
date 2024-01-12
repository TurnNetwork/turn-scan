package com.turn.browser.response.staking;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

/**
 * Receive rewards based on node query
 */
public class QueryClaimByStakingResp {

    private String hash;

    private String addr;

    private Integer addrType;

    private Long time;

    private BigDecimal reward;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }

    public Integer getAddrType() {
        return addrType;
    }

    public void setAddrType(Integer addrType) {
        this.addrType = addrType;
    }

}
