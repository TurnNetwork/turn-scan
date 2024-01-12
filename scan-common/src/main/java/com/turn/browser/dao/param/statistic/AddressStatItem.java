package com.turn.browser.dao.param.statistic;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@Accessors(chain = true)
public class AddressStatItem {
	/**
	 * address
	 */
	private String address;

	/**
	 * Address type: 1 account, 2 built-in contract, 3EVM contract, 4WASM contract
	 */
	private Integer type;

	/**
	 *Total number of transactions
	 */
	private Integer txQty;

	/**
	 * Number of erc20 transactions
	 */
	private Integer erc20TxQty;
	/**
	 * Number of erc721 transactions
	 */
	private Integer erc721TxQty;

	/**
	 *Total number of transfer transactions
	 */
	private Integer transferQty;

	/**
	 *Total number of entrusted transactions
	 */
	private Integer delegateQty;

	/**
	 *Total number of pledge transactions
	 */
	private Integer stakingQty;

	/**
	 *Total number of governance transactions
	 */
	private Integer proposalQty;

	/**
	 *Contract name
	 */
	private String contractName;

	/**
	 *Contract creator address
	 */
	private String contractCreate;

	/**
	 * Create the transaction Hash of the contract
	 */
	private String contractCreatehash;
	/**
	 * The destruction hash of the contract
	 */
	private String contractDestroyHash;
	/**
	 *Contract code data
	 */
	private String contractBin;

	/**
	 * Received the commission reward
	 */
	private BigDecimal haveReward;
}
