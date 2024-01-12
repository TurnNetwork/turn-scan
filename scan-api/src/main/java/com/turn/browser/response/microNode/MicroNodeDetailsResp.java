package com.turn.browser.response.microNode;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * Micro node details return object
 */
public class MicroNodeDetailsResp {

    private String nodeId;

    private String name;

    private String version;

    private BigDecimal totalValue;

    private Integer nodeStatus;

    private String operationAddr;

    private String beneficiary;


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public Integer getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(Integer nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public String getOperationAddr() {
        return operationAddr;
    }

    public void setOperationAddr(String operationAddr) {
        this.operationAddr = operationAddr;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
