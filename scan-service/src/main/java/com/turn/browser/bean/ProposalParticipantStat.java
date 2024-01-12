package com.turn.browser.bean;


public class ProposalParticipantStat {
	private Long voterCount; // Cumulative number of people who can vote
	private Long supportCount; // Number of votes in favor
	private Long opposeCount; // Number of opposing votes
	private Long abstainCount; // Number of abstention votes
	public Long getVoterCount() {
		return voterCount;
	}
	public void setVoterCount(Long voterCount) {
		this.voterCount = voterCount;
	}
	public Long getSupportCount() {
		return supportCount;
	}
	public void setSupportCount(Long supportCount) {
		this.supportCount = supportCount;
	}
	public Long getOpposeCount() {
		return opposeCount;
	}
	public void setOpposeCount(Long opposeCount) {
		this.opposeCount = opposeCount;
	}
	public Long getAbstainCount() {
		return abstainCount;
	}
	public void setAbstainCount(Long abstainCount) {
		this.abstainCount = abstainCount;
	}
}
