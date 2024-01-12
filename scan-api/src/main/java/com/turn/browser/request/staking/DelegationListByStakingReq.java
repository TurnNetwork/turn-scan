package com.turn.browser.request.staking;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.turn.browser.request.PageReq;

/**
 * Validator delegation list request object
 */
public class DelegationListByStakingReq extends PageReq{
	@NotBlank(message = "{nodeId not null}")
	@Size(min = 130,max = 130)
    private String nodeId;
    private String stakingBlockNum;
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getStakingBlockNum() {
		return stakingBlockNum;
	}
	public void setStakingBlockNum(String stakingBlockNum) {
		this.stakingBlockNum = stakingBlockNum;
	}
}