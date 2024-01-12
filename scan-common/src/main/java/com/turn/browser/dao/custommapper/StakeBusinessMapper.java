package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.param.BusinessParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface StakeBusinessMapper {
    /**
     * Initiate pledge
     * @param param
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void create(BusinessParam param);
    /**
     * Increase pledge
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void increase(BusinessParam param);
    /**
     * Modify pledge information
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void modify(BusinessParam param);

    /**
     * Exit the pledge when the pledge deposit is locked
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void lockedExit(BusinessParam param);

    /**
     * Exit the pledge when the pledge deposit is not locked
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void unlockExit(BusinessParam param);

    /**
     * Update node information: keybase information, program version number information
     * @param updateNodeList
     * @return
     */
	int updateNodeForTask(@Param("list") List<Node> updateNodeList);
}