package com.turn.browser.param.sync;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * totalSupply update parameters
 */
@Data
public class TotalSupplyUpdateParam {


    private String address;

    private BigDecimal totalSupply;

    private Date updateTime;

}
