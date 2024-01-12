package com.turn.browser.bean;

import lombok.Data;

@Data
public class TokenHolderCount {

    /**
     *Contract address
     */
    private String tokenAddress;

    /**
     *The number of holders corresponding to the token
     */
    private Integer tokenHolderCount;

}
