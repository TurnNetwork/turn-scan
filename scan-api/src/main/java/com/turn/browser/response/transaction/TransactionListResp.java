package com.turn.browser.response.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * 交易列表返回对象
 */
public class TransactionListResp {
	private String txHash; //Transaction hash
	private String from; //Sender address (operation address)
	private String to; //Receiver address
	private Long seq; //sort number
	private BigDecimal value; //Amount (unit: AAA)
	private BigDecimal actualTxCost; //Transaction fee (unit: AAA)
	private String txType; //Transaction type 0: Transfer 1: Contract release 2: Contract call 5: MPC transaction
	private Long serverTime; //server time
	private Long timestamp;//block time
	private Long blockNumber; //Exchange at block height
	private String failReason; //Failure reason
	private String receiveType; //This field represents the account type stored in the to field: account-wallet address, contract-contract address,
	//When the front-end page clicks on the recipient's address, it will use this field to decide whether to jump to the account details or contract details.
	private Integer txReceiptStatus; //Transaction status
	public String getTxHash() {
		return txHash;
	}
	public void setTxHash(String txHash) {
		this.txHash = txHash;
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
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	@JsonSerialize(using = CustomLatSerializer.class)
	public BigDecimal getActualTxCost() {
		return actualTxCost;
	}
	public void setActualTxCost(BigDecimal actualTxCost) {
		this.actualTxCost = actualTxCost;
	}
	public String getTxType() {
		return txType;
	}
	public void setTxType(String txType) {
		this.txType = txType;
	}
	public Long getServerTime() {
		return serverTime;
	}
	public void setServerTime(Long serverTime) {
		this.serverTime = serverTime;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(Long blockNumber) {
		this.blockNumber = blockNumber;
	}
	public String getFailReason() {
		return failReason;
	}
	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	public String getReceiveType() {
		return receiveType;
	}
	public void setReceiveType(String receiveType) {
		this.receiveType = receiveType;
	}
	public Integer getTxReceiptStatus() {
		return txReceiptStatus;
	}
	public void setTxReceiptStatus(Integer txReceiptStatus) {
		this.txReceiptStatus = txReceiptStatus;
	}

	public Long getSeq() {
		return seq;
	}

	public void setSeq(Long seq) {
		this.seq = seq;
	}
}
