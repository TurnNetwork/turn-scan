package com.turn.browser.v0152.bean;

import lombok.Data;

@Data
public class ErcTxInfo {
    private String name; // Contract name
    private String symbol;
    private Integer decimal; // precision
    private String contract; //Contract address (also the transaction to address)
    private String from; // Transaction initiator (also the token deductor)
    private String to;
    private String tokenId; // tokenId
    private String value; // Transaction value
    private Integer toType; // receiver type
    private Integer fromType; // sender type
}
