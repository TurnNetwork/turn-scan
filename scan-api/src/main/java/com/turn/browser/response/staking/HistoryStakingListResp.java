package com.turn.browser.response.staking;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * Historical validator list return object
 */
public class HistoryStakingListResp {
	private String nodeId; //Block node address
	private String nodeName; //Verifier name
	private String stakingIcon; //Verifier icon
	private Integer status; //Status 4: Exiting 5: Exited
	private BigDecimal statDelegateReduction; //Delegation to be extracted
	private Integer slashLowQty; //Number of low block rate reports
	private Integer slashMultiQty; //Number of multi-sign reports
	private Long leaveTime; //Exit time
	private Long blockQty; //The number of blocks generated
	private Long unlockBlockNum; //Estimated unlock block height
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getStakingIcon() {
		return stakingIcon;
	}
	public void setStakingIcon(String stakingIcon) {
		this.stakingIcon = stakingIcon;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getStatDelegateReduction() {
		return statDelegateReduction;
	}
	public void setStatDelegateReduction(BigDecimal statDelegateReduction) {
		this.statDelegateReduction = statDelegateReduction;
	}
	public Integer getSlashLowQty() {
		return slashLowQty;
	}
	public void setSlashLowQty(Integer slashLowQty) {
		this.slashLowQty = slashLowQty;
	}
	public Integer getSlashMultiQty() {
		return slashMultiQty;
	}
	public void setSlashMultiQty(Integer slashMultiQty) {
		this.slashMultiQty = slashMultiQty;
	}
	public Long getLeaveTime() {
		return leaveTime;
	}
	public void setLeaveTime(Long leaveTime) {
		this.leaveTime = leaveTime;
	}
	public Long getBlockQty() {
		return blockQty;
	}
	public void setBlockQty(Long blockQty) {
		this.blockQty = blockQty;
	}

	public Long getUnlockBlockNum() {
		return unlockBlockNum;
	}

	public void setUnlockBlockNum(Long unlockBlockNum) {
		this.unlockBlockNum = unlockBlockNum;
	}
}
