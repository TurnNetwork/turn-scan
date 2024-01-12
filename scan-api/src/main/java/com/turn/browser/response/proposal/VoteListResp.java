package com.turn.browser.response.proposal;

/**
 *  Voting list return object
 */
public class VoteListResp {
	private String voterName; //Voting verifier name
	private String voter; //voting verifier
	private String option; //Vote selection 1: support; 2: oppose; 3 abstain
	private String txHash; //voting hash
	private Long timestamp; //voting time
	public String getVoterName() {
		return voterName;
	}
	public void setVoterName(String voterName) {
		this.voterName = voterName;
	}
	public String getVoter() {
		return voter;
	}
	public void setVoter(String voter) {
		this.voter = voter;
	}
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public String getTxHash() {
		return txHash;
	}
	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
}
