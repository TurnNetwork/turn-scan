package com.turn.browser.bean.govern;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * @description: Modifiable lock-up configuration
 **/
@Builder
public class Restricting {
    private BigDecimal minimumRelease;

	public BigDecimal getMinimumRelease() {
		return minimumRelease;
	}

	public void setMinimumRelease(BigDecimal minimumRelease) {
		this.minimumRelease = minimumRelease;
	}
}
