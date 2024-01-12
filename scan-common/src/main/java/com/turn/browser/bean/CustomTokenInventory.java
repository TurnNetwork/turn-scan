package com.turn.browser.bean;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class CustomTokenInventory {

    private String tokenAddress;

    private BigInteger tokenId;

    private String owner;

    private String name;

    private String description;

    private String image;

    private Date createTime;

    private Date updateTime;

    private Integer tokenTxQty;

    private String tokenName;

    private String symbol;

}