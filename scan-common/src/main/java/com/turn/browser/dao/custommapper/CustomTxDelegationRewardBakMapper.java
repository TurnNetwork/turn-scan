package com.turn.browser.dao.custommapper;

import com.turn.browser.elasticsearch.dto.DelegationReward;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomTxDelegationRewardBakMapper {

    int batchInsert(@Param("list") List<DelegationReward> list);

    long findMaxId();
}
