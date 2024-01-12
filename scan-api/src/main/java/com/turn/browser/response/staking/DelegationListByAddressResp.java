package com.turn.browser.response.staking;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * Address lock list return object
 */
public class DelegationListByAddressResp {
	private String nodeId; //node id
	private String nodeName; //Node name
	private BigDecimal delegateValue; //Number of delegates
	private BigDecimal delegateHas; //unlocked delegate (ATP)
	private BigDecimal delegateLocked; //Locked delegate (ATP)
	private BigDecimal delegateUnlock; //Delegated (ATP)
	private BigDecimal delegateReleased; //Redeeming delegate (ATP)
	private BigDecimal delegateClaim; //Delegate to be extracted (ATP)
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
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getDelegateValue() {
		return delegateValue;
	}
	public void setDelegateValue(BigDecimal delegateValue) {
		this.delegateValue = delegateValue;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getDelegateHas() {
		return delegateHas;
	}
	public void setDelegateHas(BigDecimal delegateHas) {
		this.delegateHas = delegateHas;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getDelegateLocked() {
		return delegateLocked;
	}
	public void setDelegateLocked(BigDecimal delegateLocked) {
		this.delegateLocked = delegateLocked;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getDelegateUnlock() {
		return delegateUnlock;
	}
	public void setDelegateUnlock(BigDecimal delegateUnlock) {
		this.delegateUnlock = delegateUnlock;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getDelegateReleased() {
		return delegateReleased;
	}
	public void setDelegateReleased(BigDecimal delegateReleased) {
		this.delegateReleased = delegateReleased;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getDelegateClaim() {
		return delegateClaim;
	}
	public void setDelegateClaim(BigDecimal delegateClaim) {
		this.delegateClaim = delegateClaim;
	}
    
}
