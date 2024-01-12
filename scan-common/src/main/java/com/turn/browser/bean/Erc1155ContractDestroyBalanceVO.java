package com.turn.browser.bean;

import lombok.Data;

@Data
public class Erc1155ContractDestroyBalanceVO {

    /**
     *Contract
     */
    private String tokenAddress;

    /**
     * Holder
     */
    private String owner;

    /**
     * quantity
     */
    private Integer num;

}
