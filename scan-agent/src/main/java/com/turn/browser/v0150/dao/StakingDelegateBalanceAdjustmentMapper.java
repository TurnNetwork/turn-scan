package com.turn.browser.v0150.dao;

import com.turn.browser.v0150.bean.AdjustParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

@Mapper
public interface StakingDelegateBalanceAdjustmentMapper {
    /**
     * Pledge related table adjustment
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void adjustStakingData(@Param("adjustParam") AdjustParam adjustParam);

    /**
     * Entrust relevant statements to adjust accounts
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void adjustDelegateData(@Param("adjustParam") AdjustParam adjustParam);
}