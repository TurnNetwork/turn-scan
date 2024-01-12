package com.turn.browser.request;

import com.turn.browser.SubChainTopic;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class SubChainTxReq {

    private int type;
    private long seq;
    private String bHash;
    private long num;
    private int index;
    private String hash;
    private String from;
    private String to;
    private int fromType;
    private int toType;
    private long nonce;
    private String gasLimit;
    private String  gasPrice;
    private String gasUsed;
    private String cost;
    private String value;
    private int status;
    private Timestamp time;
    private String failReason;
    private String remark;
    private long bubbleId;
    List<SubChainTopic> subChainTopics;
}
