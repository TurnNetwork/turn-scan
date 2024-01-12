package com.turn.browser.request.bubble;

import com.turn.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 *  bubble list request obj
 *  @file AliveStakingListReq.java
 *  @description 
 *	@author zhangrj
 *  @data 2019年8月31日
 */
public class BubbleListReq extends PageReq{
    @NotBlank(message = "{queryStatus not null}")
    @Pattern(regexp = "all|active|releasing|released", message = "{queryStatus.illegal}")
    private String queryStatus;

	private Long bubbleId;

	public Long getBubbleId() {
		return bubbleId;
	}

	public void setBubbleId(Long bubbleId) {
		this.bubbleId = bubbleId;
	}
	public String getQueryStatus() {
		return queryStatus;
	}
	public void setQueryStatus(String queryStatus) {
		this.queryStatus = queryStatus;
	}
    
}