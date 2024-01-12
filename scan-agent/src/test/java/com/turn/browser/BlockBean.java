package com.turn.browser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.bubble.protocol.core.methods.response.BubbleBlock;

import java.util.List;

/**
 * @description:
 **/
@Data
@Slf4j
public class BlockBean {
    private String number;
    private String hash;
    private String parentHash;
    private String nonce;
    private String sha3Uncles;
    private String logsBloom;
    private String transactionsRoot;
    private String stateRoot;
    private String receiptsRoot;
    private String author;
    private String miner;
    private String mixHash;
    private String difficulty;
    private String totalDifficulty;
    private String extraData;
    private String size;
    private String gasLimit;
    private String gasUsed;
    private String timestamp;
    private List<BubbleBlock.TransactionObject> transactions;
    private List<String> uncles;
    private List<String> sealFields;
}
