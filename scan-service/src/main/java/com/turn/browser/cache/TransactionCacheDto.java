package com.turn.browser.cache;

import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.response.RespPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction cache dto
 */
public class TransactionCacheDto {

	public TransactionCacheDto() {
		this.transactionList = new ArrayList<>();
	}
	/**
	 * Transaction construction initial method
	 * @param page
	 */
	public TransactionCacheDto(List<Transaction> transactionList, RespPage page) {
		this.transactionList = transactionList;
		this.page = page;
	}
	private List<Transaction> transactionList;
	
	private RespPage page;

	public List<Transaction> getTransactionList() {
		return transactionList;
	}

	public void setTransactionList(List<Transaction> transactionList) {
		this.transactionList = transactionList;
	}

	public RespPage getPage() {
		return page;
	}

	public void setPage(RespPage page) {
		this.page = page;
	}
	
	
	
}
