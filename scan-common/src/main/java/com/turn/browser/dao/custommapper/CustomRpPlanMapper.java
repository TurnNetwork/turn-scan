package com.turn.browser.dao.custommapper;

import com.turn.browser.bean.CustomRpPlan;
import com.turn.browser.dao.entity.RpPlan;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface CustomRpPlanMapper {

    List<CustomRpPlan> selectAll();

    int batchInsertOrUpdateSelective(@Param("list") Set<RpPlan> list, @Param("selective") RpPlan.Column... selective);

    /**
     *Total amount of inquiry
     */
    BigDecimal selectSumByAddress(String address);

    BigDecimal sumAmountByAddressAndBlockNumber(@Param("address") String address, @Param("blockNumber") Long blockNumber);

    /**
     * Lock the unexpired amount
     *
     * @param settlePeriodBlockCount: Total number of blocks in each settlement period
     * @param curBlockNumber: current block height
     * @return: java.math.BigDecimal
     */
    BigDecimal getRPNotExpiredValue(@Param("settlePeriodBlockCount") Long settlePeriodBlockCount, @Param("curBlockNumber") Long curBlockNumber);

}
