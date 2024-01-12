package com.turn.browser.bean.govern;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * @description: Modifiable block parameter configuration
 **/
@Builder
public class Block {
    private BigDecimal maxBlockGasLimit;

	public BigDecimal getMaxBlockGasLimit() {
		return maxBlockGasLimit;
	}

	public void setMaxBlockGasLimit(BigDecimal maxBlockGasLimit) {
		this.maxBlockGasLimit = maxBlockGasLimit;
	}
    
}
