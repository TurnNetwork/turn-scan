package com.turn.browser.response.home;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

/**
 * Home page statistics return object
 */
public class ChainStatisticNewResp {

    private Long currentNumber; // Current block height

    private String nodeId; // Block producing node id

    private String nodeName; // Block node name

    private Integer txQty; //Total number of transactions

    private Integer currentTps; // current TPS

    private Integer maxTps; // Maximum transaction TPS

    private BigDecimal turnValue; // Current circulation

    private BigDecimal issueValue; // Current issue amount

    private BigDecimal stakingDelegationValue; // Current total number of pledges = valid pledges + delegation

    private Integer addressQty; // Number of addresses

    private Integer proposalQty; // Total number of proposals

    private Integer doingProposalQty; // Number of proposals in progress

    private Integer nodeNum; // number of nodes

    private BigDecimal availableStaking;// total available pledge

    private List<BlockListNewResp> blockList;

    /**
     * Denominator of pledge = total issuance - real-time incentive pool balance - real-time delegation reward pool contract balance
     */
    private BigDecimal stakingDenominator;

    private Long subChainTxQty; //Total number of sub chain transactions

    public Long getSubChainTxQty() {
        return subChainTxQty;
    }

    public void setSubChainTxQty(Long subChainTxQty) {
        this.subChainTxQty = subChainTxQty;
    }

    public Long getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(Long currentNumber) {
        this.currentNumber = currentNumber;
    }

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

    public Integer getTxQty() {
        return txQty;
    }

    public void setTxQty(Integer txQty) {
        this.txQty = txQty;
    }

    public Integer getCurrentTps() {
        return currentTps;
    }

    public void setCurrentTps(Integer currentTps) {
        this.currentTps = currentTps;
    }

    public Integer getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(Integer maxTps) {
        this.maxTps = maxTps;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getTurnValue() {
        return turnValue;
    }

    public void setTurnValue(BigDecimal turnValue) {
        this.turnValue = turnValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getIssueValue() {
        return issueValue;
    }

    public void setIssueValue(BigDecimal issueValue) {
        this.issueValue = issueValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingDelegationValue() {
        return stakingDelegationValue;
    }

    public void setStakingDelegationValue(BigDecimal stakingDelegationValue) {
        this.stakingDelegationValue = stakingDelegationValue;
    }

    public Integer getAddressQty() {
        return addressQty;
    }

    public void setAddressQty(Integer addressQty) {
        this.addressQty = addressQty;
    }

    public Integer getProposalQty() {
        return proposalQty;
    }

    public void setProposalQty(Integer proposalQty) {
        this.proposalQty = proposalQty;
    }

    public Integer getDoingProposalQty() {
        return doingProposalQty;
    }

    public void setDoingProposalQty(Integer doingProposalQty) {
        this.doingProposalQty = doingProposalQty;
    }

    public List<BlockListNewResp> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<BlockListNewResp> blockList) {
        this.blockList = blockList;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getAvailableStaking() {
        return availableStaking;
    }

    public void setAvailableStaking(BigDecimal availableStaking) {
        this.availableStaking = availableStaking;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingDenominator() {
        return stakingDenominator;
    }

    public void setStakingDenominator(BigDecimal stakingDenominator) {
        this.stakingDenominator = stakingDenominator;
    }

}
