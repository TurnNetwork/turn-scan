package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.StakingHistory;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface CustomStakingHistoryMapper {

	int batchInsertOrUpdateSelective ( @Param("list") Set <StakingHistory> list, @Param("selective") StakingHistory.Column... selective );



}
