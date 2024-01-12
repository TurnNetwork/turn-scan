package com.turn.browser.bean;

import java.math.BigDecimal;

public class ConfigChange {
    private BigDecimal blockReward; // block reward
    private BigDecimal settleStakeReward; // Settlement cycle pledge reward
    private BigDecimal issueEpoch; // Current issuance cycle
    private BigDecimal yearStartNum; // The starting block number of the current issuance cycle
    private BigDecimal yearEndNum; // The end block number of the current issuance cycle
    private BigDecimal remainEpoch; // Number of settlement cycles remaining in the current issuance cycle
    private BigDecimal avgPackTime; // Average block time
    private BigDecimal stakeReward; // The pledge reward of each validator in the current settlement cycle
    private String issueRates; //Current issuance ratio

    public BigDecimal getBlockReward() {
        return blockReward;
    }

    public void setBlockReward(BigDecimal blockReward) {
        this.blockReward = blockReward;
    }

    public BigDecimal getSettleStakeReward() {
        return settleStakeReward;
    }

    public void setSettleStakeReward(BigDecimal settleStakeReward) {
        this.settleStakeReward = settleStakeReward;
    }

    public BigDecimal getIssueEpoch() {
        return issueEpoch;
    }

    public void setIssueEpoch(BigDecimal issueEpoch) {
        this.issueEpoch = issueEpoch;
    }

    public BigDecimal getYearStartNum() {
        return yearStartNum;
    }

    public void setYearStartNum(BigDecimal yearStartNum) {
        this.yearStartNum = yearStartNum;
    }

    public BigDecimal getYearEndNum() {
        return yearEndNum;
    }

    public void setYearEndNum(BigDecimal yearEndNum) {
        this.yearEndNum = yearEndNum;
    }

    public BigDecimal getRemainEpoch() {
        return remainEpoch;
    }

    public void setRemainEpoch(BigDecimal remainEpoch) {
        this.remainEpoch = remainEpoch;
    }

    public BigDecimal getAvgPackTime() {
        return avgPackTime;
    }

    public void setAvgPackTime(BigDecimal avgPackTime) {
        this.avgPackTime = avgPackTime;
    }

    public BigDecimal getStakeReward() {
        return stakeReward;
    }

    public void setStakeReward(BigDecimal stakeReward) {
        this.stakeReward = stakeReward;
    }

	public String getIssueRates() {
		return issueRates;
	}

	public void setIssueRates(String issueRates) {
		this.issueRates = issueRates;
	}
}
