package com.turn.browser.bean;

import lombok.Data;

import java.math.BigInteger;

@Data
public class LockDelegate {

    /**
     * Unlock block height
     */
    private BigInteger blockNum;

    /**
     * Unlock time
     */
    private long date;

    /**
     * frozen quantity（AAA）
     */
    private String lock;

}
