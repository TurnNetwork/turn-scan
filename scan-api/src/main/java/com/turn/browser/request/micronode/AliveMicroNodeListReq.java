package com.turn.browser.request.micronode;

import com.turn.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 *  Micronode list request object
 */
public class AliveMicroNodeListReq extends PageReq{
    private String key;
    @NotBlank(message = "{queryStatus not null}")
    @Pattern(regexp = "all|candidate|exited", message = "{queryStatus.illegal}")
    private String queryStatus;

	private Long bubbleId;

	public Long getBubbleId() {
		return bubbleId;
	}

	public void setBubbleId(Long bubbleId) {
		this.bubbleId = bubbleId;
	}

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