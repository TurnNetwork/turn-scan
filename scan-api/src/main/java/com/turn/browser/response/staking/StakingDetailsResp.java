package com.turn.browser.response.staking;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * Verifier details return object
 */
public class StakingDetailsResp {

    private String nodeName; //Verifier name

    private String stakingIcon; //Verifier icon

    private Integer status; //Status 1: Candidate 2: Active 3: Block producing 4: Exiting 5: Exited

    private BigDecimal totalValue; //Total number of pledges = valid pledges + delegation

    private BigDecimal delegateValue; //Total number of delegates

    private BigDecimal stakingValue; //Total number of pledges

    private Integer delegateQty; //Number of delegates

    private Integer slashLowQty; //Number of low block rate reports

    private Integer slashMultiQty; //Number of multi-sign reports

    private Long blockQty; //The number of blocks generated

    private Long expectBlockQty; //The expected number of blocks

    private String expectedIncome; //Estimated annual income rate (calculated from the time the validator joins)

    private Long joinTime; //Join time

    private Integer verifierTime; //Enter the number of consensus verification rounds

    private BigDecimal rewardValue; //Accumulated revenue

    private String nodeId; //node id

    private String stakingAddr; //Account address to initiate staking

    /**
     * Type of account address that initiates pledge
     */
    private Integer stakingAddrType;

    private String denefitAddr; //Income address

    /**
     * Revenue address type
     */
    private Integer denefitAddrType;

    private String website; //The third-party homepage of the node

    private String details; //Node description

    private String externalId; //ID card id

    private String externalUrl; //ID card id connection

    private Long stakingBlockNum; //The latest staking transaction block height

    private BigDecimal statDelegateReduction;//The delegate to be extracted

    private Long leaveTime; //Exit time

    private Boolean isInit; //Whether it is the initial node

    private String deleAnnualizedRate; //Estimated commission annual income rate (calculated from the start of commission)

    private String rewardPer; //Delegation reward ratio

    private String nextRewardPer; // Commission reward ratio in the next settlement cycle

    private BigDecimal haveDeleReward; //The node’s current pledge has received the delegation reward

    private BigDecimal totalDeleReward; //Node’s current pledge cumulative delegation reward

    private BigDecimal deleRewardRed; //The node is currently staking to receive the delegation reward

    /**
     *Block rate
     */
    private String genBlocksRate;

    /**
     * Version
     */
    private String Version;

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

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateValue() {
        return delegateValue;
    }

    public void setDelegateValue(BigDecimal delegateValue) {
        this.delegateValue = delegateValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingValue() {
        return stakingValue;
    }

    public void setStakingValue(BigDecimal stakingValue) {
        this.stakingValue = stakingValue;
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

    public Long getExpectBlockQty() {
        return expectBlockQty;
    }

    public void setExpectBlockQty(Long expectBlockQty) {
        this.expectBlockQty = expectBlockQty;
    }

    public String getExpectedIncome() {
        return expectedIncome;
    }

    public void setExpectedIncome(String expectedIncome) {
        this.expectedIncome = expectedIncome;
    }

    public Long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    public Integer getVerifierTime() {
        return verifierTime;
    }

    public void setVerifierTime(Integer verifierTime) {
        this.verifierTime = verifierTime;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getRewardValue() {
        return rewardValue;
    }

    public void setRewardValue(BigDecimal rewardValue) {
        this.rewardValue = rewardValue;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStakingAddr() {
        return stakingAddr;
    }

    public void setStakingAddr(String stakingAddr) {
        this.stakingAddr = stakingAddr;
    }

    public String getDenefitAddr() {
        return denefitAddr;
    }

    public void setDenefitAddr(String denefitAddr) {
        this.denefitAddr = denefitAddr;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getStakingBlockNum() {
        return stakingBlockNum;
    }

    public void setStakingBlockNum(Long stakingBlockNum) {
        this.stakingBlockNum = stakingBlockNum;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStatDelegateReduction() {
        return statDelegateReduction;
    }

    public void setStatDelegateReduction(BigDecimal statDelegateReduction) {
        this.statDelegateReduction = statDelegateReduction;
    }

    public Long getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(Long leaveTime) {
        this.leaveTime = leaveTime;
    }

    public Boolean getIsInit() {
        return isInit;
    }

    public void setIsInit(Boolean isInit) {
        this.isInit = isInit;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getDeleAnnualizedRate() {
        return deleAnnualizedRate;
    }

    public void setDeleAnnualizedRate(String deleAnnualizedRate) {
        this.deleAnnualizedRate = deleAnnualizedRate;
    }

    public String getRewardPer() {
        return rewardPer;
    }

    public void setRewardPer(String rewardPer) {
        this.rewardPer = rewardPer;
    }

    public String getNextRewardPer() {
        return nextRewardPer;
    }

    public void setNextRewardPer(String nextRewardPer) {
        this.nextRewardPer = nextRewardPer;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getHaveDeleReward() {
        return haveDeleReward;
    }

    public void setHaveDeleReward(BigDecimal haveDeleReward) {
        this.haveDeleReward = haveDeleReward;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getTotalDeleReward() {
        return totalDeleReward;
    }

    public void setTotalDeleReward(BigDecimal totalDeleReward) {
        this.totalDeleReward = totalDeleReward;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDeleRewardRed() {
        return deleRewardRed;
    }

    public void setDeleRewardRed(BigDecimal deleRewardRed) {
        this.deleRewardRed = deleRewardRed;
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

    public Integer getStakingAddrType() {
        return stakingAddrType;
    }

    public void setStakingAddrType(Integer stakingAddrType) {
        this.stakingAddrType = stakingAddrType;
    }

    public Integer getDenefitAddrType() {
        return denefitAddrType;
    }

    public void setDenefitAddrType(Integer denefitAddrType) {
        this.denefitAddrType = denefitAddrType;
    }

}
