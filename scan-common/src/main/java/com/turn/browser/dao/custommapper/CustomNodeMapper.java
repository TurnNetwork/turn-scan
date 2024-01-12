package com.turn.browser.dao.custommapper;

import com.github.pagehelper.Page;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.entity.NodeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface CustomNodeMapper {

    /**
     * Query nodeName based on nodeId
     *
     * @param nodeId
     * @return
     * @method findNameById
     */
    String findNameById(String nodeId);

    /**
     * Query list based on conditions
     *
     * @param example
     * @return
     * @method selectByExample
     */
    Page<Node> selectListByExample(NodeExample example);


    int selectCountByActive();

    /**
     * Query nodeName based on nodeIds
     *
     * @param nodeIds
     * @return java.util.List<java.lang.String>
     */
    List<Node> batchFindNodeNameByNodeId(@Param("nodeIds") Set<String> nodeIds);

    Page<Node> findAliveStakingList(Integer status1, Integer isSettle1, boolean isUnion, Integer status2, Integer isSettle2);

    /**
     * Update node block number statistics in batches
     *
     * @param nodeList
     * @return int
     */
    int updateNodeSettleStatis(@Param("list") List<Node> nodeList);

}