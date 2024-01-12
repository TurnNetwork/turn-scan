package com.turn.browser.bean;

import lombok.Data;

@Data
public class CustomTokenDetail extends CustomToken {

    private String creator;

    private String txHash;

    private Integer txCount;

    private String binCode;

    /**
     * Whether the contract to which the token belongs has been destroyed: 0-no, 1-yes
     */
    private int isContractDestroy;

}