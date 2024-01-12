package com.turn.browser.request.subchain;

import com.turn.browser.utils.HexUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 交易详情请求对象
 *  @file TransactionDetailsReq.java
 *  @description 
 *	@author zhangrj
 *  @data 2019年8月31日
 */
public class SubChainTransactionDetailsReq {
    @NotBlank(message = "{txHash not null}")
    @Size(min = 60,max = 66)
    private String txHash;

	@NotNull(message = "{bubbleId not null}")
	private Long bubbleId;

	public Long getBubbleId() {
		return bubbleId;
	}

	public void setBubbleId(Long bubbleId) {
		this.bubbleId = bubbleId;
	}

	public String getTxHash() {
		return txHash;
	}

	public void setTxHash(String txHash) {
		if(StringUtils.isBlank(txHash)) return;
		this.txHash = HexUtil.prefix(txHash.toLowerCase());
	}
    
}