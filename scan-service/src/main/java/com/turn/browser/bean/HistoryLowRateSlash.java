package com.turn.browser.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.math.BigInteger;

/**
 * @description:
 **/
public class HistoryLowRateSlash {
    // Punished node ID
    @JSONField(name = "NodeId")
    private String nodeId;
    // Penalty amount
    @JSONField(name = "Amount")
    private BigInteger amount;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }
}
