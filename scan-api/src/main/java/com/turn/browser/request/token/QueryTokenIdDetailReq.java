package com.turn.browser.request.token;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Query toknid list request parameters
 */
@Data
public class QueryTokenIdDetailReq {

    @NotBlank(message = "Contract cannot be empty")
    private String contract;

    @NotBlank(message = "tokenID cannot be empty")
    private String tokenId;

}
