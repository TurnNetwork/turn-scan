package com.turn.browser.response.transaction;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Arc1155Param {

    /**
     * Contract address (also the transaction to address)
     */
    private String contract;


    /**
     *Identifier of erc1155
     */
    private String tokenId;

    /**
     *Contract name
     */
    private String name;

    /**
     * Accuracy
     */
    private Integer decimal;

    /**
     * picture
     */
    private String image;

    private String from;

    /**
     * Sender type
     */
    private Integer fromType;

    private String to;

    /**
     * Receiver type
     */
    private Integer toType;

    /**
     * Transaction value
     */
    private String value;

}
