package com.turn.browser.v0160.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.turn.browser.bean.RecoveredDelegationAmount;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingKey;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.v0160.bean.FixIssue1583;
import com.turn.browser.v0160.bean.RecoveredDelegation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;


@Slf4j
@Service
public class DelegateBalanceAdjustmentService {

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private CustomAddressMapper customAddressMapper;

    @Resource
    private AddressCache addressCache;

    /**
     * Adjust accounts
     *
     * @return:
     */
    public void adjust() throws Exception {
        List<FixIssue1583> list = FixIssue1583.getRecoveredDelegationInfo();
        List<RecoveredDelegationAmount> recoveredDelegationAmountList = CollUtil.newArrayList();
        for (FixIssue1583 node : list) {
            // Total commission rewards claimed
            BigInteger totalDeleReward = BigInteger.valueOf(0);
            // total Delegation
            BigInteger totalDelegationAmount = node.getTotalDelegationAmount();
            // total Delegation Reward
            BigInteger totalDelegationRewardAmount = node.getTotalDelegationRewardAmount();
            for (RecoveredDelegation recoveredDelegation : node.getRecoveredDelegationList()) {
                // Calculate the commission reward that the user can retrieve (the algorithm is consistent with the bottom layer, note: the result must be the same as the bottom layer)
                BigInteger recoveredDelegationAmount = recoveredDelegation.getDelegationAmount().multiply(totalDelegationRewardAmount).divide(totalDelegationAmount);
                totalDeleReward = totalDeleReward.add(recoveredDelegationAmount);
                RecoveredDelegationAmount recovered = new RecoveredDelegationAmount();
                recovered.setDelegateAddr(recoveredDelegation.getAddress());
                recovered.setStakingBlockNum(node.getStakingBlockNumber());
                recovered.setNodeId(node.getNodeId());
                recovered.setRecoveredDelegationAmount(new BigDecimal(recoveredDelegationAmount));
                recoveredDelegationAmountList.add(recovered);
            }
            updateNode(node.getNodeId(), totalDeleReward);
            StakingKey stakingKey = new Staking();
            stakingKey.setNodeId(node.getNodeId());
            stakingKey.setStakingBlockNum(node.getStakingBlockNumber());
            updateStaking(stakingKey, totalDeleReward);
        }
        if (CollUtil.isNotEmpty(recoveredDelegationAmountList)) {
            updateAddress(recoveredDelegationAmountList);
        }
    }

    /**
     * Update node table
     *
     * @param nodeId
     * @param totalDeleReward Total commission rewards claimed
     * @return: void
     */
    private void updateNode(String nodeId, BigInteger totalDeleReward) throws Exception {
        Node nodeInfo = nodeMapper.selectByPrimaryKey(nodeId);
        if (ObjectUtil.isNull(nodeInfo)) {
            throw new Exception(StrUtil.format("The corresponding node information cannot be found,nodeId:[{}]", nodeId));
        } else {
            Node newNode = new Node();
            newNode.setNodeId(nodeInfo.getNodeId());
            // have_dele_reward All pledges have received delegation rewards
            BigDecimal oldHaveDeleReward = nodeInfo.getHaveDeleReward();
            BigDecimal newHaveDeleReward = oldHaveDeleReward.add(new BigDecimal(totalDeleReward));
            newNode.setHaveDeleReward(newHaveDeleReward);
            int res = nodeMapper.updateByPrimaryKeySelective(newNode);
            if (res > 0) {
                log.error("issue1583 Account adjustment: Update node table successfully, nodeId: [{}]: New value of entrustment rewards received by all pledges [{}] = old value of entrustment rewards received by all pledges [{}] + total entrustment rewards received [{ }];", nodeInfo.getNodeId(), newHaveDeleReward, oldHaveDeleReward, totalDeleReward);
            } else {
                log.error("issue1583 Account adjustment: Failed to update node table,nodeId:[{}]", nodeInfo.getNodeId());
            }
        }
    }

    /**
     * Update staking table
     *
     * @param stakingKey
     * @param totalDeleReward Total commission rewards claimed
     * @return: void
     * @date: 2021/6/25
     */
    private void updateStaking(StakingKey stakingKey, BigInteger totalDeleReward) throws Exception {
        Staking staking = stakingMapper.selectByPrimaryKey(stakingKey);
        if (ObjectUtil.isNull(staking)) {
            throw new Exception(StrUtil.format("The corresponding pledge information cannot be found,stakingKey:[{}]", JSONUtil.toJsonStr(stakingKey)));
        } else {
            Staking newStaking = new Staking();
            newStaking.setNodeId(staking.getNodeId());
            newStaking.setStakingBlockNum(staking.getStakingBlockNum());
            // have_dele_reward The node currently pledges and has received the delegation reward
            BigDecimal oldHaveDeleReward = staking.getHaveDeleReward();
            BigDecimal newHaveDeleReward = staking.getHaveDeleReward().add(new BigDecimal(totalDeleReward));
            newStaking.setHaveDeleReward(newHaveDeleReward);
            int res = stakingMapper.updateByPrimaryKeySelective(newStaking);
            if (res > 0) {
                log.error("issue1583 Adjustment: Successfully updated the staking table, stakingKey: [{}]: The node’s current staking has received the new delegation reward value [{}] = The node’s current staking has received the delegation reward’s old value [{}] + the total number of commission rewards received [{}];",
                          JSONUtil.toJsonStr(stakingKey),
                          newHaveDeleReward,
                          oldHaveDeleReward,
                          totalDeleReward);
            } else {
                log.error("issue1583 Account adjustment: Failed to update staking table,stakingKey:[{}]", JSONUtil.toJsonStr(stakingKey));
            }
        }
    }

    /**
     * Update address table
     *
     * @param list
     * @return: void
     */
    private void updateAddress(List<RecoveredDelegationAmount> list) {
        try {
            List<RecoveredDelegationAmount> updateDBlist = updateAddressCache(list);
            if (CollUtil.isNotEmpty(updateDBlist)) {
                customAddressMapper.batchUpdateByAddress(updateDBlist);
            }
            log.error("issue1583 Account adjustment: The address table was updated successfully, the data is: [{}]", JSONUtil.toJsonStr(list));
        } catch (Exception e) {
            log.error("issue1583 Account adjustment: Failed to update the address table, the data is: [{}]", JSONUtil.toJsonStr(list));
            throw e;
        }
    }

    /**
     * Update the claimed delegation rewards in the cache address and filter out addresses that do not exist in the cache
     *
     * @param list
     * @return: java.util.List<com.turn.browser.bean.RecoveredDelegationAmount>
     */
    private List<RecoveredDelegationAmount> updateAddressCache(List<RecoveredDelegationAmount> list) {
        List<RecoveredDelegationAmount> updateDBlist = CollUtil.newArrayList();
        for (RecoveredDelegationAmount recoveredDelegationAmount : list) {
            Address addressInfo = addressMapper.selectByPrimaryKey(recoveredDelegationAmount.getDelegateAddr());
            if (ObjectUtil.isNull(addressInfo)) {
                // If the db does not exist, create a new address in the cache and update the received commission rewards.
                Address address = addressCache.createDefaultAddress(recoveredDelegationAmount.getDelegateAddr());
                address.setHaveReward(recoveredDelegationAmount.getRecoveredDelegationAmount());
                addressMapper.insertSelective(address);
            } else {
                // If db exists, it is stored in updateDBlist and uses the update method of db.
                updateDBlist.add(recoveredDelegationAmount);
                BigDecimal newHaveReward = addressInfo.getHaveReward().add(recoveredDelegationAmount.getRecoveredDelegationAmount());
                log.info("Adjustment---Updating the address table successfully: the new value of the entrustment reward field that the address has received [{}] = the old value of the entrustment reward field that has been claimed [{}] + the entrustment reward that has been claimed [{}]", newHaveReward, addressInfo.getHaveReward(), recoveredDelegationAmount.getRecoveredDelegationAmount());
            }
        }
        return updateDBlist;
    }

}
