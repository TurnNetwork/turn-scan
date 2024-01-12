package com.turn.browser.response.address;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *  Address details lock substructure return object
 */
public class DetailsRPPlanResp {
	private BigInteger epoch;         //Lock-up period
    private BigDecimal amount;      //Locked amount
    private String blockNumber;   //The lock-up period corresponds to the fast high end period * epoch
    private Long estimateTime;   //estimated time
	public BigInteger getEpoch() {
		return epoch;
	}
	public void setEpoch(BigInteger epoch) {
		this.epoch = epoch;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(String blockNumber) {
		this.blockNumber = blockNumber;
	}
	public Long getEstimateTime() {
		return estimateTime;
	}
	public void setEstimateTime(Long estimateTime) {
		this.estimateTime = estimateTime;
	}
	
    
}
