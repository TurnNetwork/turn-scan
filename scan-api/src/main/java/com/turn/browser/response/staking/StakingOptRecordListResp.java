package com.turn.browser.response.staking;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * Validator operation list return object
 */
public class StakingOptRecordListResp {

    private Long timestamp; //Creation time

    private String desc; //operation description

    private String txHash; //Transaction to which it belongs

    private Long blockNumber; //Block to which it belongs

    private String type; //type

    private String id; //proposal id

    private String title; //proposal title

    private String option; //Vote selection 1: support; 2: oppose; 3 abstain

    private String percent; //Penalty percentage

    private BigDecimal amount; //Penalty amount

    private Integer isFire; //Whether to kick out the list 0-no, 1-yes

    private String version; //version number

    private String proposalType; //Proposal type 1: text proposal; 2: upgrade proposal; 3 parameter proposal.

    private String beforeRate; //original commission reward ratio

    private String afterRate; //Delegation reward ratio after modification

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getIsFire() {
        return isFire;
    }

    public void setIsFire(Integer isFire) {
        this.isFire = isFire;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProposalType() {
        return proposalType;
    }

    public void setProposalType(String proposalType) {
        this.proposalType = proposalType;
    }

    public String getBeforeRate() {
        return beforeRate;
    }

    public void setBeforeRate(String beforeRate) {
        this.beforeRate = beforeRate;
    }

    public String getAfterRate() {
        return afterRate;
    }

    public void setAfterRate(String afterRate) {
        this.afterRate = afterRate;
    }

}
