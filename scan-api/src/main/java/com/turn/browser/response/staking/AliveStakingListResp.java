package com.turn.browser.response.staking;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLowLatSerializer;

/**
 * Active validator list return object
 */
public class AliveStakingListResp {


    private Integer ranking;

    private String nodeId;

    private String nodeName;

    private String stakingIcon;

    /**
     * 1: Candidate 2: Active 3: Block produced
     */
    private Integer status;

    /**
     * Total number of pledges = valid pledges + delegation
     */
    private String totalValue;

    /**
     *Total number of orders
     */
    private String delegateValue;

    /**
     *Number of clients
     */
    private Integer delegateQty;

    /**
     *Number of low block rate reports
     */
    private Integer slashLowQty;

    /**
     * Number of multi-sign reports
     */
    private Integer slashMultiQty;

    /**
     *Number of blocks generated
     */
    private Long blockQty;

    /**
     * Estimated annual revenue rate (calculated from the time when the validator joins)
     */
    private String expectedIncome;

    /**
     * Is it officially recommended?
     */
    private Boolean isRecommend;

    /**
     * Whether it is the initial node
     */
    private Boolean isInit;

    /**
     * Estimated delegation annual rate (calculated from the moment the validator joins)
     */
    private String deleAnnualizedRate;

    /**
     * The estimated annual delegation rate of the previous day (calculated from the time when the validator joins)
     */
    private String preDeleAnnualizedRate;

    /**
     * Commission reward ratio
     */
    private String delegatedRewardRatio;

    /**
     *Block rate
     */
    private String genBlocksRate;

    /**
     * Version
     */
    private String Version;

    /**
     * The node’s normal operating time ratio in the last 24 hours (8 settlement cycles)
     */
    private String upTime;

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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getStakingIcon() {
        return stakingIcon;
    }

    public void setStakingIcon(String stakingIcon) {
        this.stakingIcon = stakingIcon;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @JsonSerialize(using = CustomLowLatSerializer.class)
    public String getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(String totalValue) {
        this.totalValue = totalValue;
    }

    @JsonSerialize(using = CustomLowLatSerializer.class)
    public String getDelegateValue() {
        return delegateValue;
    }

    public void setDelegateValue(String delegateValue) {
        this.delegateValue = delegateValue;
    }

    public Integer getDelegateQty() {
        return delegateQty;
    }

    public void setDelegateQty(Integer delegateQty) {
        this.delegateQty = delegateQty;
    }

    public Integer getSlashLowQty() {
        return slashLowQty;
    }

    public void setSlashLowQty(Integer slashLowQty) {
        this.slashLowQty = slashLowQty;
    }

    public Integer getSlashMultiQty() {
        return slashMultiQty;
    }

    public void setSlashMultiQty(Integer slashMultiQty) {
        this.slashMultiQty = slashMultiQty;
    }

    public Long getBlockQty() {
        return blockQty;
    }

    public void setBlockQty(Long blockQty) {
        this.blockQty = blockQty;
    }

    public String getExpectedIncome() {
        return expectedIncome;
    }

    public void setExpectedIncome(String expectedIncome) {
        this.expectedIncome = expectedIncome;
    }

    public Boolean getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(Boolean isRecommend) {
        this.isRecommend = isRecommend;
    }

    public Boolean getIsInit() {
        return isInit;
    }

    public void setIsInit(Boolean isInit) {
        this.isInit = isInit;
    }

    public String getDeleAnnualizedRate() {
        return deleAnnualizedRate;
    }

    public void setDeleAnnualizedRate(String deleAnnualizedRate) {
        this.deleAnnualizedRate = deleAnnualizedRate;
    }

    public String getDelegatedRewardRatio() {
        return delegatedRewardRatio;
    }

    public void setDelegatedRewardRatio(String delegatedRewardRatio) {
        this.delegatedRewardRatio = delegatedRewardRatio;
    }

    public String getGenBlocksRate() {
        return genBlocksRate;
    }

    public void setGenBlocksRate(String genBlocksRate) {
        this.genBlocksRate = genBlocksRate;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public String getPreDeleAnnualizedRate() {
        return preDeleAnnualizedRate;
    }

    public void setPreDeleAnnualizedRate(String preDeleAnnualizedRate) {
        this.preDeleAnnualizedRate = preDeleAnnualizedRate;
    }

    public String getUpTime() {
        return upTime;
    }

    public void setUpTime(String upTime) {
        this.upTime = upTime;
    }

}
