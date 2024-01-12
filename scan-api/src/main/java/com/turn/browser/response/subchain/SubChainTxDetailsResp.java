package com.turn.browser.response.subchain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.SubChainTopic;
import com.turn.browser.config.json.CustomLatSerializer;
import com.turn.browser.config.json.CustomVersionSerializer;
import com.turn.browser.response.transaction.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易详情返回对象
 *
 * @author zhangrj
 * @file TransactionDetailsResp.java
 * @description
 * @data 2019年8月31日
 */
public class SubChainTxDetailsResp {

    private String txHash; // 交易hash

    private String from; // 发送方地址

    private String to; // 接收方地址（操作地址）

    private Long timestamp; // 交易时间

    private Long serverTime; // 服务器时间

    private String confirmNum; // 区块确认数

    private Long blockNumber; // 交易所在区块高度

    private String gasLimit; // 能量限制

    private String gasUsed; // 能量消耗

    private BigDecimal gasPrice; // 能量价格

    private BigDecimal value; // 金额(单位:von)

    private BigDecimal actualTxCost; // 交易费用(单位:von)

    private String txType; // 交易类型

    private String input; // 附加输入数据

    private String txInfo; // 附加输入数据解析后的结构

    private String failReason; // 失败原因

    private String method; // 合约调用函数

    private String contractName; // 合约名称

    private Integer txReceiptStatus; // 交易状态

    private BigDecimal txAmount; // 交易费用

    List<SubChainTopic> subChainTopics; // topic信息

    public List<SubChainTopic> getSubChainTopics() {
        return subChainTopics;
    }

    public void setSubChainTopics(List<SubChainTopic> subChainTopics) {
        this.subChainTopics = subChainTopics;
    }

    public String getTxHash() {
        return this.txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getServerTime() {
        return this.serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    public String getConfirmNum() {
        return this.confirmNum;
    }

    public void setConfirmNum(String confirmNum) {
        this.confirmNum = confirmNum;
    }

    public Long getBlockNumber() {
        return this.blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getGasLimit() {
        return this.gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasUsed() {
        return this.gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getGasPrice() {
        return this.gasPrice;
    }

    public void setGasPrice(BigDecimal gasPrice) {
        this.gasPrice = gasPrice;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getActualTxCost() {
        return this.actualTxCost;
    }

    public void setActualTxCost(BigDecimal actualTxCost) {
        this.actualTxCost = actualTxCost;
    }

    public String getTxType() {
        return this.txType;
    }

    public void setTxType(String txType) {
        this.txType = txType;
    }

    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getTxInfo() {
        return this.txInfo;
    }

    public void setTxInfo(String txInfo) {
        this.txInfo = txInfo;
    }

    public String getFailReason() {
        return this.failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public Integer getTxReceiptStatus() {
        return this.txReceiptStatus;
    }

    public void setTxReceiptStatus(Integer txReceiptStatus) {
        this.txReceiptStatus = txReceiptStatus;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getTxAmount() {
        return this.txAmount;
    }

    public void setTxAmount(BigDecimal txAmount) {
        this.txAmount = txAmount;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContractName() {
        return this.contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

}
