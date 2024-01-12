package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.GasEstimate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomGasEstimateMapper {
    int batchInsertOrUpdateSelective(@Param("list") List<GasEstimate> list, @Param("selective") GasEstimate.Column... selective);
}