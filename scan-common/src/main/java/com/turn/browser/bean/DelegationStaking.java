package com.turn.browser.bean;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Address delegation returns data
 */
@Data
public class DelegationStaking {

    private String delegateAddr;

    private BigDecimal delegateHes;

    private BigDecimal delegateLocked;

    private BigDecimal delegateReleased;

    private Integer delegateAddrType;

}
