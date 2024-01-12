package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * @Description: Create commission storage parameters
 */
@Data
@Builder
@Accessors(chain = true)
public class DelegateCreate implements BusinessParam {
    //node id
    private String nodeId;
    //Amount of commission
    private BigDecimal amount;
    // Commissioned transaction block height
    private BigInteger blockNumber;
    //transaction sender
    private String txFrom;
    //Transaction serial number
    private BigInteger sequence;
    //Node staking is fast and high
    private BigInteger stakingBlockNumber;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.DELEGATE_CREATE;
    }
}
