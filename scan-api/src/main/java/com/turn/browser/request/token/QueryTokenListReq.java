package com.turn.browser.request.token;

import com.turn.browser.request.PageReq;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Query contract list request parameters
 */
@Data
public class QueryTokenListReq extends PageReq {

    @NotBlank(message = "{type required}")
    @Size(min = 0, max = 10)
    private String type;// erc20,erc721

}
