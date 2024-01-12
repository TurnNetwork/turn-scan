package com.turn.browser.request;

import lombok.Data;

import java.util.List;

@Data
public class SubChainTxListReq {

    private List<SubChainTxReq> subChainTxReqSet;

    private Long bubbleId;

    private String nodeId;

    private String sign;
}
