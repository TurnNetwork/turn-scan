package com.turn.browser.request.address;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *  Query address details request object
 */
public class QueryDetailRequest {
    @NotBlank(message = "{address not null}")
    @Size(min = 42,max = 42)
    private String address;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address.toLowerCase();
	}
    
}