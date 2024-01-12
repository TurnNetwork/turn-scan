package com.turn.browser.dao.custommapper;

import com.turn.browser.elasticsearch.dto.Transaction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomTxBakMapper {

    int batchInsertOrUpdateSelective(@Param("list") List<Transaction> list);

    /**
     * Find the largest id
     *
     * @param :
     * @return: long
     * @date: 2022/1/24
     */
    long findMaxId();

}