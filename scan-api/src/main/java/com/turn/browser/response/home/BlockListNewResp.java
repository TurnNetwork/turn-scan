package com.turn.browser.response.home;

/**
 * Home page block list return object
 */
public class BlockListNewResp {
	private Boolean isRefresh; // Whether to update
	private Long number; // block height
	private Long timestamp; // block time
	private Long serverTime; // server time
	private String nodeId; // Block producing node id
	private String nodeName; // Block node name
	private Integer statTxQty; // Number of transactions
	public Boolean getIsRefresh() {
		return isRefresh;
	}
	public void setIsRefresh(Boolean isRefresh) {
		this.isRefresh = isRefresh;
	}
	public Long getNumber() {
		return number;
	}
	public void setNumber(Long number) {
		this.number = number;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getServerTime() {
		return serverTime;
	}
	public void setServerTime(Long serverTime) {
		this.serverTime = serverTime;
	}
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
	public Integer getStatTxQty() {
		return statTxQty;
	}
	public void setStatTxQty(Integer statTxQty) {
		this.statTxQty = statTxQty;
	}

	
}
