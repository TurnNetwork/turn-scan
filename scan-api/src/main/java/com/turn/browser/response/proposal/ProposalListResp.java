package com.turn.browser.response.proposal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomVersionSerializer;


/**
 *Proposal list return object
 */
public class ProposalListResp {
	private String pipNum; //pip num
	private String proposalHash; // Proposal internal identifier
	private String topic; //proposal title
	private String description; //Proposal description
	private String url; // github address https://github.com/ethereum/EIPs/blob/master/EIPS/eip-100.md PIP number
	//eip-100
	private String type; // Proposal type 1: text proposal; 2: upgrade proposal; 3 parameter proposal.
	private String status; // Status 1: Voting 2: Passed 3: Failed 4: Pre-upgrade 5: Upgrade completed Passed=2 or 4 or 5
	private String curBlock; // Current block height
	private String endVotingBlock; // Voting settlement speed
	private String newVersion; // The version to be upgraded by the upgrade proposal
	private String paramName; // parameter name
	private Long timestamp; // Proposal time
	private Long inBlock; // The block where the proposal is located
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
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
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getInBlock() {
		return inBlock;
	}
	public void setInBlock(Long inBlock) {
		this.inBlock = inBlock;
	}
	
}
