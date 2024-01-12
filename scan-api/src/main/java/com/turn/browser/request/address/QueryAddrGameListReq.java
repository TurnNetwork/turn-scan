package com.turn.browser.request.address;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *  Address to join the game list request object
 */
public class QueryAddrGameListReq {
	@NotBlank(message = "{address not null}")
	@Size(min = 42,max = 42)
	private String address;

	private Long roundId;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address.toLowerCase();
	}

	public Long getTableId() {
		return roundId;
	}

	public void setTableId(Long tableId) {
		this.roundId = tableId;
	}
}