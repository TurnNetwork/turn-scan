package com.turn.browser.request.token;

import com.turn.browser.request.PageReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Query the holder’s contract list
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryHolderTokenListReq extends PageReq {

    @NotBlank(message = "{address required}")
    @Size(min = 42, max = 42)
    private String address;

    /**
     * type取值:erc20 | erc721 | 为空
     */
    @NotBlank(message = "{type required}")
    private String type;

}
