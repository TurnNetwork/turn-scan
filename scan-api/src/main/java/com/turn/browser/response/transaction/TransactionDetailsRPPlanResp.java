package com.turn.browser.response.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 *  交易详情锁仓子结构体返回对象
 */
public class TransactionDetailsRPPlanResp {
	private String epoch; //Lock-up period
	private BigDecimal amount; //Lock amount
	private String blockNumber; //The lock-up period corresponds to the fast high end period * epoch
	public String getEpoch() {
		return epoch;
	}
	public void setEpoch(String epoch) {
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
    
}
