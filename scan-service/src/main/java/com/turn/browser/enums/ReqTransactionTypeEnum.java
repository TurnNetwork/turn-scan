package com.turn.browser.enums;

import com.turn.browser.elasticsearch.dto.Transaction.TypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction type request enumeration
 */
public enum ReqTransactionTypeEnum {

	/**
	 * 0: Transfer 1: Contract release 2: Contract call 5: MPC transaction
	 *6: ERC20 contract release (contract creation)
	 *7: ERC20 contract call (contract execution)
	 *8: ERC721 contract release (contract creation)
	 *9: ERC721 contract call (contract execution)
	 * 1000: Initiate pledge 1001: Modify pledge information 1002: Increase pledge 1003: Cancel pledge 1004: Initiate commission 1005: Reduce stake/cancel commission
	 * 2000: Submit text proposal 2001: Submit upgrade proposal 2002: Submit parameter proposal 2003: Vote for proposal 2004: Version statement
	 * 3000: Report multiple signatures
	 * 4000: Create a lock-up plan
	 */
	TRANSACTION_TRANSFER("transfer","0","transfer"),
	TRANSACTION_DELEGATE("delegate","1","delegate"),
	TRANSACTION_STAKING("staking","2","validator"),
	TRANSACTION_PROPOSAL("proposal","3","proposal");
	private ReqTransactionTypeEnum(String name,String code,String description) {
		this.code = code;
		this.name = name;
		this.description = description;
	}
	
	private String name;
	
	private String code;
	
	private String description;

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Convert different types of pages to display the data that needs to be displayed
	 * @method getTxType
	 * @param typeName
	 * @return
	 */
	public static List<Object> getTxType(String typeName){
		List<Object> list = new ArrayList<>();
		if(ReqTransactionTypeEnum.TRANSACTION_TRANSFER.getName().equals(typeName)) {
			list.add(String.valueOf(TypeEnum.TRANSFER.getCode()));
		}
		if(ReqTransactionTypeEnum.TRANSACTION_DELEGATE.getName().equals(typeName)) {
			list.add(String.valueOf(TypeEnum.DELEGATE_CREATE.getCode()));
			list.add(String.valueOf(TypeEnum.DELEGATE_EXIT.getCode()));
			list.add(String.valueOf(TypeEnum.CLAIM_REWARDS.getCode()));
		}
		if(ReqTransactionTypeEnum.TRANSACTION_STAKING.getName().equals(typeName)) {
			list.add(String.valueOf(TypeEnum.STAKE_CREATE.getCode()));
			list.add(String.valueOf(TypeEnum.STAKE_MODIFY.getCode()));
			list.add(String.valueOf(TypeEnum.STAKE_INCREASE.getCode()));
			list.add(String.valueOf(TypeEnum.STAKE_EXIT.getCode()));
			list.add(String.valueOf(TypeEnum.REPORT.getCode()));
		}
		if(ReqTransactionTypeEnum.TRANSACTION_PROPOSAL.getName().equals(typeName)) {
			list.add(String.valueOf(TypeEnum.PROPOSAL_TEXT.getCode()));
			list.add(String.valueOf(TypeEnum.PROPOSAL_UPGRADE.getCode()));
			list.add(String.valueOf(TypeEnum.PROPOSAL_PARAMETER.getCode()));
			list.add(String.valueOf(TypeEnum.PROPOSAL_CANCEL.getCode()));
			list.add(String.valueOf(TypeEnum.PROPOSAL_VOTE.getCode()));
			list.add(String.valueOf(TypeEnum.VERSION_DECLARE.getCode()));
		}
		return list;
	}
}
