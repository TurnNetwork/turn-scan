package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.GasEstimate;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.param.BusinessParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EpochBusinessMapper {

    /**
     * New billing cycle
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void settle(BusinessParam param);

    /**
     * Find nodes that need to update staking rewards
     *
     * @param preVerifierSet:
     * @return: java.util.List<com.turn.browser.dao.entity.Staking>
     * @date: 2022/6/16
     */
    List<Staking> findStaking(@Param("list") List<String> preVerifierSet);

    /**
     * New settlement cycle--update staking rewards
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void settleForStakingValue(@Param("list") List<Staking> updateStakingList);

    /**
     * Update Gas estimation cycle number
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateGasEstimate(@Param("list") List<GasEstimate> estimateList);

    /**
     * Data changes in the new election cycle (the settlement & consensus cycle is pushed forward 20 blocks)
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void slashNode(BusinessParam param);

    /**
     * Data changes in the new consensus cycle
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void consensus(BusinessParam param);

    /**
     * Query the list of nodes to be punished
     *
     * @param preValidatorList
     * @return
     */
    List<Staking> querySlashNode(@Param("list") List<String> preValidatorList);

    /**
     * Mark the node as abnormal
     *
     * @return
     */
    void setException(@Param("list") List<String> nodeIdList);

    /**
     * Query nodes marked as abnormal
     *
     * @return
     */
    List<Staking> getException(@Param("list") List<String> nodeIdList);

}