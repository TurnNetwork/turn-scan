package com.turn.browser.dao.custommapper;

import com.turn.browser.elasticsearch.dto.NodeOpt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomNOptBakMapper {

    int batchInsertOrUpdateSelective(@Param("list") List<NodeOpt> list);

    /**
     * Get the latest serial number of node operation records
     *
     * @param :
     * @return: long
     */
    long getLastNodeOptSeq();

}