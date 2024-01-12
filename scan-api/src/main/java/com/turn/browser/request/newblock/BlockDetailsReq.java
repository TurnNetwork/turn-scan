package com.turn.browser.request.newblock;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Block details query request object
 */
public class BlockDetailsReq {
    @NotNull(message = "{number not null}")
    @Min(value = 0)
    private Integer number;

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
    
}