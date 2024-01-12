package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Restricting <br/>
 * <pre>
insert into `rp_plan` 
	(`address`, 
	`epoch`, 
	`amount`, 
	`number`
	)
	values
	('address', 
	'epoch', 
	'amount', 
	'number'
	);

 * <pre/>
 * @author chendai
 */
@Data
@Builder
@Accessors(chain = true)
public class RestrictingCreate implements BusinessParam {
	
	List<RestrictingItem> itemList;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.RESTRICTING_CREATE;
    }
}
