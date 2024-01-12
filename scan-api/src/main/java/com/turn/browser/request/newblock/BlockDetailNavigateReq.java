package com.turn.browser.request.newblock;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Block details switching object
 */
public class BlockDetailNavigateReq {
    @NotBlank(message = "{navigate.direction.notnull}")
    @Pattern(regexp = "prev|next", message = "{direction.illegal}")
    private String direction;
    @NotNull(message = "{block.number.notnull}")
    private Long number;
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public Long getNumber() {
		return number;
	}
	public void setNumber(Long number) {
		this.number = number;
	}
}
