package com.turn.browser.dao.custommapper;

import com.github.pagehelper.Page;
import com.turn.browser.bean.CustomDelegation;
import com.turn.browser.bean.DelegationAddress;
import com.turn.browser.bean.DelegationStaking;

import com.turn.browser.bean.RecoveredDelegationAmount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomDelegationMapper {

    List<CustomDelegation> selectByNodeId(@Param("nodeId") String nodeId);

    List<CustomDelegation> selectByNodeIdList(@Param("nodeIds") List<String> nodeIds);

    Page<DelegationStaking> selectStakingByNodeId(@Param("nodeId") String nodeId);

    Page<DelegationAddress> selectAddressByAddr(@Param("delegateAddr") String delegateAddr);

}
