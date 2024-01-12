package com.turn.browser.request.newblock;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.turn.browser.request.PageReq;
import com.turn.browser.utils.HexUtil;

/**
 *  Query node id request object
 */
public class BlockListByNodeIdReq extends PageReq{
	@NotBlank(message="{nodeId is not null}")
	@Size(min = 130,max = 130)
    private String nodeId;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = HexUtil.prefix(nodeId.toLowerCase());
	}
	
}