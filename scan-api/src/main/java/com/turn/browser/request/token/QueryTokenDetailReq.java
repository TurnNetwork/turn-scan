package com.turn.browser.request.token;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Query contract details request parameters
 */
public class QueryTokenDetailReq {

    @NotBlank(message = "{address required}")
    @Size(min = 42, max = 42)
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
