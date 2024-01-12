package com.turn.browser.request.staking;

import com.turn.browser.request.PageReq;

/**
 *  Lock the validator list request object
 */
public class LockedStakingListReq extends PageReq{
    private String key;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}