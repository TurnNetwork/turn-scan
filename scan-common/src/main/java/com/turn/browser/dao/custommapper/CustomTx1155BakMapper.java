package com.turn.browser.dao.custommapper;

import com.turn.browser.elasticsearch.dto.ErcTx;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface CustomTx1155BakMapper {

    int batchInsert(@Param("set") Set<ErcTx> set);

    long findMaxId();
}
