package com.turn.browser.request.newtransaction;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 *  Transaction details request object
 */
public class TransactionDetailNavigateReq {
    @NotBlank(message = "{transaction.hash.notnull}")
    private String txHash;
    @NotBlank(message = "{navigate.direction.notnull}")
    @Pattern(regexp = "prev|next", message = "{direction.illegal}")
    private String direction;
	public String getTxHash() {
		return txHash;
	}
	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
}
