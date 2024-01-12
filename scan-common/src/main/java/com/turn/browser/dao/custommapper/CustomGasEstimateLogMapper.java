package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.GasEstimateLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomGasEstimateLogMapper {
    int batchInsertOrUpdateSelective(@Param("list") List<GasEstimateLog> list, @Param("selective") GasEstimateLog.Column... selective);
}