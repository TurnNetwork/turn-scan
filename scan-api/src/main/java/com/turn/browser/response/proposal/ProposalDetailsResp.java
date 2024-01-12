package com.turn.browser.response.proposal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomRateSerializer;
import com.turn.browser.config.json.CustomVersionSerializer;

/**
 *Proposal details return object
 */
public class ProposalDetailsResp {
    private String pipNum;
	private String proposalHash; //proposal internal identifier
	private String topic; //proposal title
	private String description; //Proposal description
	private String url; //github address https://github.com/ethereum/EIPs/blob/master/EIPS/eip-100.md PIP number eip-100
	private Integer type; //Proposal type 1: text proposal; 2: upgrade proposal; 3 parameter proposal.
	private Integer status; //Status 1: Voting 2: Passed 3: Failed 4: Pre-upgrade 5: Upgrade completed Passed=2 or 4 or 5
	private String curBlock; //Current block height
	private String endVotingBlock; //voting settlement speed
	private Long timestamp; //Proposal time
	private String activeBlock; // (if the vote passes) the effective block is high
	private String newVersion; //The version to be upgraded by the upgrade proposal
	private String paramName; //Parameter name
	private String currentValue; //current value of parameter
	private String newValue; //New value of parameter
	private String nodeName; //The name of the node that initiated the proposal
	private String nodeId; //The node ID that initiated the proposal
	private Integer yeas; //Those who agree
	private Integer nays; //those who object
	private Integer abstentions; //Those who abstain
	private String accuVerifiers; //Total number of people
	private Long activeBlockTime; //Estimated effective block time (activeBlock-curBlock)*period
	private Long endVotingBlockTime; //Voting block high time (endVotingBlock-curBlock)*period
	private String supportRateThreshold; //Pass condition rate
	private String yesRateThreshold; //pass rate
	private String opposeRateThreshold; //opposition rate
	private String abstainRateThreshold; //abstention rate
	private Long inBlock; //The height of the block
	private String canceledPipId;//The proposal id corresponding to the canceled proposal
	private String canceledTopic;//The proposal title corresponding to the canceled proposal
	private String participationRate;//pass condition rate
	public String getPipNum() {
		return pipNum;
	}
	public void setPipNum(String pipNum) {
		this.pipNum = pipNum;
	}
	public String getProposalHash() {
		return proposalHash;
	}
	public void setProposalHash(String proposalHash) {
		this.proposalHash = proposalHash;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getCurBlock() {
		return curBlock;
	}
	public void setCurBlock(String curBlock) {
		this.curBlock = curBlock;
	}
	public String getEndVotingBlock() {
		return endVotingBlock;
	}
	public void setEndVotingBlock(String endVotingBlock) {
		this.endVotingBlock = endVotingBlock;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public String getActiveBlock() {
		return activeBlock;
	}
	public void setActiveBlock(String activeBlock) {
		this.activeBlock = activeBlock;
	}
	@JsonSerialize(using = CustomVersionSerializer.class)
	public String getNewVersion() {
		return newVersion;
	}
	public void setNewVersion(String newVersion) {
		this.newVersion = newVersion;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public String getCurrentValue() {
		return currentValue;
	}
	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public Integer getYeas() {
		return yeas;
	}
	public void setYeas(Integer yeas) {
		this.yeas = yeas;
	}
	public Integer getNays() {
		return nays;
	}
	public void setNays(Integer nays) {
		this.nays = nays;
	}
	public Integer getAbstentions() {
		return abstentions;
	}
	public void setAbstentions(Integer abstentions) {
		this.abstentions = abstentions;
	}
	public String getAccuVerifiers() {
		return accuVerifiers;
	}
	public void setAccuVerifiers(String accuVerifiers) {
		this.accuVerifiers = accuVerifiers;
	}
	public Long getActiveBlockTime() {
		return activeBlockTime;
	}
	public void setActiveBlockTime(Long activeBlockTime) {
		this.activeBlockTime = activeBlockTime;
	}
	public Long getEndVotingBlockTime() {
		return endVotingBlockTime;
	}
	public void setEndVotingBlockTime(Long endVotingBlockTime) {
		this.endVotingBlockTime = endVotingBlockTime;
	}
	@JsonSerialize(using = CustomRateSerializer.class)
	public String getSupportRateThreshold() {
		return supportRateThreshold;
	}
	public void setSupportRateThreshold(String supportRateThreshold) {
		this.supportRateThreshold = supportRateThreshold;
	}
	public String getOpposeRateThreshold() {
		return opposeRateThreshold;
	}
	public void setOpposeRateThreshold(String opposeRateThreshold) {
		this.opposeRateThreshold = opposeRateThreshold;
	}
	public String getAbstainRateThreshold() {
		return abstainRateThreshold;
	}
	public void setAbstainRateThreshold(String abstainRateThreshold) {
		this.abstainRateThreshold = abstainRateThreshold;
	}
	public Long getInBlock() {
		return inBlock;
	}
	public void setInBlock(Long inBlock) {
		this.inBlock = inBlock;
	}
	public String getCanceledPipId() {
		return canceledPipId;
	}
	public void setCanceledPipId(String canceledPipId) {
		this.canceledPipId = canceledPipId;
	}
	public String getCanceledTopic() {
		return canceledTopic;
	}
	public void setCanceledTopic(String canceledTopic) {
		this.canceledTopic = canceledTopic;
	}
	@JsonSerialize(using = CustomRateSerializer.class)
	public String getParticipationRate() {
		return participationRate;
	}
	public void setParticipationRate(String participationRate) {
		this.participationRate = participationRate;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getYesRateThreshold() {
		return yesRateThreshold;
	}
	public void setYesRateThreshold(String yesRateThreshold) {
		this.yesRateThreshold = yesRateThreshold;
	}
    
}
