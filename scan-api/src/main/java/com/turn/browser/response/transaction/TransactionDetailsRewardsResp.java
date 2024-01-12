package com.turn.browser.response.transaction;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

/**
 * Transaction details receive reward substructure return object
 */
public class TransactionDetailsRewardsResp {

	private String verify; //node id
	private String nodeName; //Node name
	private BigDecimal reward; //reward
	public String getVerify() {
		return verify;
	}
	public void setVerify(String verify) {
		this.verify = verify;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getReward() {
		return reward;
	}
	public void setReward(BigDecimal reward) {
		this.reward = reward;
	}
	
}
