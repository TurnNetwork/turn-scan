package com.turn.browser.response.microNode;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLowLatSerializer;

import java.math.BigDecimal;

/**
 * Active micronode list return object
 *
 */
public class AliveMicroNodeListResp {


    private Integer ranking;

    private String nodeId;

    private String amount;

    private String beneficiary;

    private String name;

    private String details;

    private String electronUri;

    private String p2pUri;

    private String version;

    private Integer isOperator;

    private Integer nodeStatus;

    private Long bubbleId;

    private String bubbleCreator;

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @JsonSerialize(using = CustomLowLatSerializer.class)
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getElectronUri() {
        return electronUri;
    }

    public void setElectronUri(String electronUri) {
        this.electronUri = electronUri;
    }

    public String getP2pUri() {
        return p2pUri;
    }

    public void setP2pUri(String p2pUri) {
        this.p2pUri = p2pUri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getIsOperator() {
        return isOperator;
    }

    public void setIsOperator(Integer isOperator) {
        this.isOperator = isOperator;
    }

    public Integer getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(Integer nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public Long getBubbleId() {
        return bubbleId;
    }

    public void setBubbleId(Long bubbleId) {
        this.bubbleId = bubbleId;
    }

    public String getBubbleCreator() {
        return bubbleCreator;
    }

    public void setBubbleCreator(String bubbleCreator) {
        this.bubbleCreator = bubbleCreator;
    }
}
