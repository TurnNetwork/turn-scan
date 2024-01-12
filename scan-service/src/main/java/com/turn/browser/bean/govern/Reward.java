package com.turn.browser.bean.govern;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * @description: Modifiable income configuration
 **/
@Builder
public class Reward {
    private BigDecimal increaseIssuanceRatio;

	public BigDecimal getIncreaseIssuanceRatio() {
		return increaseIssuanceRatio;
	}

	public void setIncreaseIssuanceRatio(BigDecimal increaseIssuanceRatio) {
		this.increaseIssuanceRatio = increaseIssuanceRatio;
	}
    
}
