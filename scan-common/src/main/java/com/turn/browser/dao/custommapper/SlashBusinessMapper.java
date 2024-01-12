package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.Slash;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.param.BusinessParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SlashBusinessMapper {

    /**
     * Double signature report
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void slashNode(Slash param);

    /**
     * The number of cycles required for the new election cycle to update the node to withdraw the pledge
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateUnStakeFreezeDuration(BusinessParam param);

    /**
     * Mark the node as a double signature exception
     *
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void setException(@Param("nodeId") String nodeId, @Param("stakingBlockNum") long blockNum);

    /**
     * Query nodes marked as double-signature exceptions
     *
     * @return
     */
    List<Staking> getException(@Param("list") List<String> list);

}