package com.turn.browser.param.sync;

import lombok.Data;

/**
 * erc20_token table tx_count update parameters
 */
@Data
public class Erc20TokenTxCountUpdateParam {
    private String address;
    private Integer txCount;
}
