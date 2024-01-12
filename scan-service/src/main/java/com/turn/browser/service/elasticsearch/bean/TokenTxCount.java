package com.turn.browser.service.elasticsearch.bean;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Token transaction number information
 */
@Data
public class TokenTxCount {
    //The number of transactions in all addresses corresponding to this token
    private Long tokenTxCount = 0L;
    // The number of transactions for the address related to the current token
    private Map<String,Long> tokenTxCountMap = new HashMap<>();
}
