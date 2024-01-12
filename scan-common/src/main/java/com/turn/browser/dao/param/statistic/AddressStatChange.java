package com.turn.browser.dao.param.statistic;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Address statistics <br/>
 * <pre>
insert into `address` 
	(`address`, 
	`type`, 
	`tx_qty`, 
	`transfer_qty`, 
	`delegate_qty`, 
	`staking_qty`, 
	`proposal_qty`, 
	`contract_name`, 
	`contract_create`, 
	`contract_createHash`
	)
	values
	('address', 
	'type', 
	'tx_qty', 
	'transfer_qty', 
	'delegate_qty', 
	'staking_qty', 
	'proposal_qty', 
	'contract_name', 
	'contract_create', 
	'contract_createHash'
	)
	on duplicate key update
	`tx_qty` = `tx_qty` + @, 
	`transfer_qty` = `transfer_qty` + @, 
	`delegate_qty` = `delegate_qty` + @, 
	`staking_qty` = `staking_qty` + @, 
	`proposal_qty` = `proposal_qty` + @;
 * <pre/>
 * @author chendai
 */
@Data
@Builder
@Accessors(chain = true)
public class AddressStatChange implements BusinessParam {
	
	List<AddressStatItem> addressStatItemList;

    @Override
	public BusinessType getBusinessType() {
		return BusinessType.ADDRESS_STATISTIC;
	}    
}
