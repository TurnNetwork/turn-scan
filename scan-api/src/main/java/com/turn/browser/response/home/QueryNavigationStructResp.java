package com.turn.browser.response.home;

/**
 * Home page returns query object substructure object
 */
public class QueryNavigationStructResp {
	private Long number; // block height
	private String txHash; // transaction hash
	private String address; // address
	private String nodeId; // node address
	public Long getNumber() {
		return number;
	}
	public void setNumber(Long number) {
		this.number = number;
	}
	public String getTxHash() {
		return txHash;
	}
	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

}
