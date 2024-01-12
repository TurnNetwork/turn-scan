package com.turn.browser.response.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Return object for reward collection
 */
public class QueryClaimByAddressResp {
	private String txHash; //Transaction hash
	private Long timestamp;//Extract time and time
	private BigDecimal allRewards; //Total revenue
	private List<TransactionDetailsRewardsResp> rewardsDetails; //Transaction substructure
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
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getAllRewards() {
		return allRewards;
	}
	public void setAllRewards(BigDecimal allRewards) {
		this.allRewards = allRewards;
	}
	public List<TransactionDetailsRewardsResp> getRewardsDetails() {
		return rewardsDetails;
	}
	public void setRewardsDetails(List<TransactionDetailsRewardsResp> rewardsDetails) {
		this.rewardsDetails = rewardsDetails;
	}

	
}
