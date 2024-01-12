package com.turn.browser.bean;

import java.util.List;

/**
 *Input information of [Contract Call PPOS] stored in blocks as storage units by special nodes
 *[
 * {
 * "TxHash":"Contract call hash",
 * "From":"caller address",
 * "To": "Built-in PPOS contract address",
 * "Input":[
 * "PPOS call input data"
 * ]
 * }
 * ]
 */
public class PPosInvokeContractInput {
    private List<TransData> transDatas;
    private String txHash;
    private String from;
    private String to;

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
    public String getTxHash() {
        return txHash;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<TransData> getTransDatas() {
        return transDatas;
    }

    public void setTransDatas(List<TransData> transDatas) {
        this.transDatas = transDatas;
    }
}