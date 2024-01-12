package com.turn.browser.request.subchain;

import com.turn.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 子链交易列表请求对象
 */
public class SubChainRecordListReq extends PageReq{
	@NotBlank(message = "{nodeId not null}")
	@Size(min = 42,max = 42)
    private String address;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}