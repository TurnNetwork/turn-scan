package com.turn.browser.request.staking;

import com.turn.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 *  Active validator list request object
 */
public class AliveStakingListReq extends PageReq{
    private String key;
    @NotBlank(message = "{queryStatus not null}")
    @Pattern(regexp = "all|active|candidate", message = "{queryStatus.illegal}")
    private String queryStatus;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getQueryStatus() {
		return queryStatus;
	}
	public void setQueryStatus(String queryStatus) {
		this.queryStatus = queryStatus;
	}
    
}