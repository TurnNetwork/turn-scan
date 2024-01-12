package com.turn.browser.bean;

import com.turn.browser.dao.entity.Address;
import lombok.Data;

/**
 * Contract address details
 */
@Data
public class CustomAddressDetail extends Address {

    /**
     * Contract type: erc20 | erc721 | erc1155
     */
    private String tokenType;

    /**
     *Contract symbol
     */
    private String tokenSymbol;

    /**
     *Contract name
     */
    private String tokenName;

    /**
     *The number of transactions corresponding to the token
     */
    private Integer tokenTxQty;

}
