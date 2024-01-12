package com.turn.browser.bean;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class NodeAprBase {

    /**
     * Billing cycle
     */
    private Integer epochNum;

    /**
     * Annualized rate of return on entrustment
     */
    private String deleAnnualizedRate;

}
