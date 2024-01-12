package com.turn.browser.response.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Erc20 holder corresponding token list information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QueryHolderTokenListResp {

    private String address;

    private String contract;

    private BigDecimal balance;

    private Integer decimal;

    private String symbol;

    private String name;

    private Integer txCount;

    private String tokenId;

    /**
     * Whether the contract to which the token belongs has been destroyed: 0-no, 1-yes
     */
    private int isContractDestroy;

    private Date createTime;

}
