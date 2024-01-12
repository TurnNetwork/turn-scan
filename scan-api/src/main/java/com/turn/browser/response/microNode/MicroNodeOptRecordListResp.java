package com.turn.browser.response.microNode;


/**
 * Micronode operation list return object
 */
public class MicroNodeOptRecordListResp {

    /**
     * 创建时间
     */
    private Long timestamp;

    /**
     * 所属交易
     */
    private String txHash;

    /**
     * 所属区块
     */
    private Long blockNumber;

    /**
     * 类型
     */
    private String type;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
