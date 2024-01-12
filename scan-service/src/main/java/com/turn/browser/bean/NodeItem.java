package com.turn.browser.bean;

import lombok.Builder;
import lombok.experimental.Accessors;

import java.math.BigInteger;

@Builder
@Accessors(chain = true)
public class NodeItem {

    private String nodeId;

    private String nodeName;

    //The latest pledge block number, updated with the transaction created by the validator
    private BigInteger stakingBlockNum;

    /**
     * Block production statistics of node settlement cycle
     */
    private String nodeSettleStatisInfo;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public BigInteger getStakingBlockNum() {
        return stakingBlockNum;
    }

    public void setStakingBlockNum(BigInteger stakingBlockNum) {
        this.stakingBlockNum = stakingBlockNum;
    }

    public String getNodeSettleStatisInfo() {
        return nodeSettleStatisInfo;
    }

    public void setNodeSettleStatisInfo(String nodeSettleStatisInfo) {
        this.nodeSettleStatisInfo = nodeSettleStatisInfo;
    }

}
