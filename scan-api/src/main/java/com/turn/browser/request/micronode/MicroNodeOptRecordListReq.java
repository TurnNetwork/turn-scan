package com.turn.browser.request.micronode;

import com.turn.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Micronode operation list request object
 */
public class MicroNodeOptRecordListReq extends PageReq{
	@NotBlank(message = "{nodeId not null}")
	@Size(min = 128,max = 128)
    private String nodeId;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
}