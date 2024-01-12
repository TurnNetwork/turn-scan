package com.turn.browser.request.newtransaction;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.turn.browser.request.PageReq;

/**
 * Address transaction list request object
 */
public class TransactionListByAddressRequest extends PageReq{
	@NotBlank(message = "{address not null}")
	@Size(min = 42,max = 42)
    private String address;
    private String txType;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address.toLowerCase();
	}
	public String getTxType() {
		return txType;
	}
	public void setTxType(String txType) {
		this.txType = txType;
	}
    
}