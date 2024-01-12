package com.turn.browser.request.staking;

import com.turn.browser.request.PageReq;

/**
 * Historical verifier query verification object
 */
public class HistoryStakingListReq extends PageReq{
    private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
    
}