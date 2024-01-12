package com.turn.browser.response.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * Home page validator list returns substructure object
 */
public class StakingListResp {
	private String nodeId; // Block producing node Id
	private String nodeName; // Block node name
	private String stakingIcon; // Validator picture
	private Integer ranking; // Node ranking
	private String expectedIncome; // Expected annual income rate (calculated from the time the validator joins)
	private Boolean isInit; // Whether it is the initialized verifier, if it is expectedIncome, no value will be displayed.
	private BigDecimal totalValue; //
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
	public Integer getRanking() {
		return ranking;
	}
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}
	public String getExpectedIncome() {
		return expectedIncome;
	}
	public void setExpectedIncome(String expectedIncome) {
		this.expectedIncome = expectedIncome;
	}
	public Boolean getIsInit() {
		return isInit;
	}
	public void setIsInit(Boolean isInit) {
		this.isInit = isInit;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getTotalValue() {
		return totalValue;
	}
	public void setTotalValue(BigDecimal totalValue) {
		this.totalValue = totalValue;
	}
	
}
