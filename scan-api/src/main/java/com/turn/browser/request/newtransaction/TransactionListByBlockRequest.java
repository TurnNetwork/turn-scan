package com.turn.browser.request.newtransaction;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.turn.browser.request.PageReq;

/**
 * Block transaction request object
 */
public class TransactionListByBlockRequest extends PageReq{
	@NotNull(message = "{blockNumber not null}")
	@Min(value = 0)
    private Integer blockNumber;
    private String txType;
	public Integer getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(Integer blockNumber) {
		this.blockNumber = blockNumber;
	}
	public String getTxType() {
		return txType;
	}
	public void setTxType(String txType) {
		this.txType = txType;
	}
    
}