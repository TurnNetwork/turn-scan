package com.turn.browser.dao.custommapper;

import com.turn.browser.elasticsearch.dto.ErcTx;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface CustomTx721BakMapper {

    int batchInsert(@Param("list") List<ErcTx> list);

    long findMaxId();

}
