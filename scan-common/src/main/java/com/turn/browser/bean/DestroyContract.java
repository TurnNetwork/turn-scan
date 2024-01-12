package com.turn.browser.bean;

import lombok.Data;

@Data
public class DestroyContract {

    /**
     *Contract address
     */
    private String tokenAddress;

    /**
     * Destroyed block height
     */
    private long contractDestroyBlock;

    /**
     * Account address
     */
    private String account;

}
