package com.turn.browser.analyzer.ppos;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.bean.DelegateExitResult;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.custommapper.CustomGasEstimateMapper;
import com.turn.browser.dao.custommapper.DelegateBusinessMapper;
import com.turn.browser.dao.entity.*;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.mapper.DelegationMapper;
import com.turn.browser.dao.mapper.GasEstimateMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.dao.param.ppos.DelegateExit;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.exception.NoSuchBeanException;
import com.turn.browser.param.DelegateExitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: Undo delegate business parameter converter
 **/
@Slf4j
@Service
public class DelegateExitAnalyzer extends PPOSAnalyzer<DelegateExitResult> {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private DelegateBusinessMapper delegateBusinessMapper;

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private DelegationMapper delegationMapper;

    @Resource
    private AddressCache addressCache;

    @Resource
    private CustomGasEstimateMapper customGasEstimateMapper;

    @Resource
    private GasEstimateMapper gasEstimateMapper;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private CustomAddressMapper customAddressMapper;

    /**
     * Reduce holdings/cancel entrustment (redemption entrustment)
     */
    @Override
    public DelegateExitResult analyze(CollectionEvent event, Transaction tx) {
        DelegateExitResult der = DelegateExitResult.builder().build();
        // Exit delegation
        DelegateExitParam txParam = tx.getTxParam(DelegateExitParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) {
            return der;
        }

        long startTime = System.currentTimeMillis();

        // Query the entrustment information corresponding to the entrustment cancellation transaction
        DelegationKey delegationKey = new DelegationKey();
        delegationKey.setDelegateAddr(tx.getFrom());
        delegationKey.setNodeId(txParam.getNodeId());
        delegationKey.setStakingBlockNum(txParam.getStakingBlockNum().longValue());
        Delegation delegation = delegationMapper.selectByPrimaryKey(delegationKey);

        if (delegation == null) {
            return der;
        }
        // Query the corresponding node information
        StakingExample stakingExample = new StakingExample();
        stakingExample.createCriteria().andNodeIdEqualTo(delegation.getNodeId()).andStakingBlockNumEqualTo(delegation.getStakingBlockNum());
        List<Staking> stakings = stakingMapper.selectByExample(stakingExample);

        if (stakings.isEmpty()) {
            throw new BusinessException("Delegator:" + tx.getFrom() + " pledge node:" + txParam.getNodeId() + " does not exist");
        }

        Staking staking = stakings.get(0);

        DelegateExit businessParam = DelegateExit.builder()
                                                 .nodeId(txParam.getNodeId())
                                                 .blockNumber(BigInteger.valueOf(tx.getNum()))
                                                 .txFrom(tx.getFrom())
                                                 .stakingBlockNumber(txParam.getStakingBlockNum())
                                                 .minimumThreshold(chainConfig.getDelegateThreshold())
                                                 .delegateReward(txParam.getReward() == null ? BigDecimal.ZERO : txParam.getReward())
                                                 .build();

        /**
         * 1. For nodes that have exited & are exiting, all the hesitation and lock-up period amounts recorded by them have been moved to the waiting field.
         * Make a redemption request at this time
         * For delegate table & node table records, deduct the actual redemption amount from the field to be claimed stat_delegate_released
         * For the pledge table, deduct stat_delegate_released from the field to be received.
         *
         * 2. When selecting candidates, buckle first and hesitate, then buckle and lock if insufficient.
         * Deduction for hesitation: delegation and pledge tables are deducted from stat_delegate_hes, node tables are deducted from stat_delegate_value
         * Deductions for insufficient hesitation: Deductions and pledges are deducted from stat_delegate_hes and stat_delegate_locked, and node tables are deducted from stat_delegate_value.
         */
        boolean isCandidate = true; // Whether the corresponding node is a candidate

        if (staking.getStatus() == CustomStaking.StatusEnum.EXITING.getCode() || staking.getStatus() == CustomStaking.StatusEnum.EXITED.getCode()) {
            // The node is [Exiting | Exited]
            businessParam.setCodeNodeIsLeave(true);
            isCandidate = false;
        }

        boolean isRefundAll = delegation.getDelegateHes() // Hesitation period amount
                                        .add(delegation.getDelegateLocked()) // +Lock-up period amount
                                        .add(delegation.getDelegateReleased()) // +Amount to be withdrawn
                                        .subtract(txParam.getAmount()) // -Apply for refund amount
                                        .compareTo(chainConfig.getDelegateThreshold()) < 0; // Less than the commission threshold
        // Calculate the true refund amount
        BigDecimal realRefundAmount = txParam.getAmount();
        if (isRefundAll) {
            // All are returned and the order is set to history.
            realRefundAmount = delegation.getDelegateHes() // +Hesitation period amount
                                         .add(delegation.getDelegateLocked()) // +Lock-up period amount
                                         .add(delegation.getDelegateReleased()); // +Amount to be withdrawn
            businessParam.setCodeIsHistory(BusinessParam.YesNoEnum.YES.getCode()); // Set the commission status to history
        } else {
            // Partially returned, the order is set to non-history
            businessParam.setCodeIsHistory(BusinessParam.YesNoEnum.NO.getCode()); // The commission status is set to non-historical
        }

        if (isCandidate) {
            // Candidate nodes
            if (delegation.getDelegateHes().compareTo(realRefundAmount) >= 0) {
                // Due to enough deduction: the delegation and pledge tables are deducted from stat_delegate_hes, and the node table is deducted from stat_delegate_value
                BigDecimal remainDelegateHes = delegation.getDelegateHes().subtract(realRefundAmount);
                // Changes in the amount of the commission record itself
                businessParam.getBalance().setDelegateHes(remainDelegateHes).setDelegateLocked(delegation.getDelegateLocked()) // The locked order amount remains unchanged
                             .setDelegateReleased(delegation.getDelegateReleased()); //The amount to be received remains unchanged
                // The amount that should be reduced by entrusting the corresponding node or staking
                businessParam.getDecrease().setDelegateHes(realRefundAmount) // -True amount of deduction
                             .setDelegateLocked(BigDecimal.ZERO) // -0
                             .setDelegateReleased(BigDecimal.ZERO); // -0
            } else {
                // Due to insufficient deduction: delegation and pledge are deducted from stat_delegate_hes and stat_delegate_locked, and the node table is deducted from stat_delegate_value.
                BigDecimal remainDelegateLocked = delegation.getDelegateLocked() // +Lock the commission amount
                                                            .add(delegation.getDelegateHes()) // +Amount of commission during the hesitation period
                                                            .subtract(realRefundAmount); // -True amount of deduction
                if (remainDelegateLocked.compareTo(BigDecimal.ZERO) < 0) {
                    remainDelegateLocked = BigDecimal.ZERO;
                }
                // Changes in the amount of the commission record itself
                businessParam.getBalance().setDelegateHes(BigDecimal.ZERO) // The amount of the hesitation period is set to 0
                             .setDelegateLocked(remainDelegateLocked) // Locked amount + hesitation amount – real deduction amount
                             .setDelegateReleased(delegation.getDelegateReleased()); //The amount to be received remains unchanged
                // The amount that should be reduced by entrusting the corresponding node or staking
                businessParam.getDecrease().setDelegateHes(BigDecimal.ZERO) // -0
                             .setDelegateLocked(realRefundAmount) // -True amount of deduction
                             .setDelegateReleased(delegation.getDelegateReleased()); //The amount to be received remains unchanged
            }
        } else {
            //Exiting or exiting nodes
            // For the delegation table & node table records, deduct the actual redemption amount from the field to be received stat_delegate_released
            // For the pledge table, deduct stat_delegate_released from the field to be received
            BigDecimal delegateReleasedBalance = delegation.getDelegateReleased().subtract(realRefundAmount);
            if (delegateReleasedBalance.compareTo(BigDecimal.ZERO) < 0) {
                delegateReleasedBalance = BigDecimal.ZERO;
            }
            businessParam.getBalance().setDelegateReleased(delegateReleasedBalance);
            businessParam.getBalance().setDelegateHes(BigDecimal.ZERO);
            businessParam.getBalance().setDelegateLocked(BigDecimal.ZERO);

            businessParam.getDecrease().setDelegateReleased(realRefundAmount);
            businessParam.getDecrease().setDelegateHes(BigDecimal.ZERO);
            businessParam.getDecrease().setDelegateLocked(BigDecimal.ZERO);
        }

        // Supplement the true refund amount
        txParam.setRealAmount(realRefundAmount);
        tx.setInfo(txParam.toJSONString());

        businessParam.setRealRefundAmount(realRefundAmount);
        delegateBusinessMapper.exit(businessParam);

        der.setDelegateExit(businessParam);

        if (txParam.getReward().compareTo(BigDecimal.ZERO) > 0) {
            // If the commission reward is 0, there is no need to record the claim record
            DelegationReward delegationReward = new DelegationReward();
            delegationReward.setHash(tx.getHash());
            delegationReward.setBn(tx.getNum());
            delegationReward.setAddr(tx.getFrom());
            delegationReward.setTime(tx.getTime());
            delegationReward.setCreTime(new Date());
            delegationReward.setUpdTime(new Date());

            List<DelegationReward.Extra> extraList = new ArrayList<>();
            DelegationReward.Extra extra = new DelegationReward.Extra();
            extra.setNodeId(businessParam.getNodeId());
            String nodeName = "Unknown";
            try {
                nodeName = nodeCache.getNode(businessParam.getNodeId()).getNodeName();
            } catch (NoSuchBeanException e) {
                log.error("{}", e.getMessage());
            }
            extra.setNodeName(nodeName);
            extra.setReward(txParam.getReward().toString());
            extraList.add(extra);
            delegationReward.setExtra(JSON.toJSONString(extraList));

            List<DelegationReward.Extra> extraCleanList = new ArrayList<>();
            if (extra.decimalReward().compareTo(BigDecimal.ZERO) > 0) {
                extraCleanList.add(extra);
            }
            delegationReward.setExtraClean(JSON.toJSONString(extraCleanList));

            der.setDelegationReward(delegationReward);
        }

        updateAddressHaveReward(businessParam);

        if (isRefundAll) {
            // 1. Redeem all: Delete corresponding records
            GasEstimateKey gek = new GasEstimateKey();
            gek.setNodeId(txParam.getNodeId());
            gek.setAddr(tx.getFrom());
            gek.setSbn(txParam.getStakingBlockNum().longValue());
            gasEstimateMapper.deleteByPrimaryKey(gek);
        } else {
            // 2. Partial redemption: 1. Add a new commission uncalculated cycle record, epoch = 0
            List<GasEstimate> estimates = new ArrayList<>();
            GasEstimate estimate = new GasEstimate();
            estimate.setNodeId(txParam.getNodeId());
            estimate.setSbn(txParam.getStakingBlockNum().longValue());
            estimate.setAddr(tx.getFrom());
            estimate.setEpoch(0L);
            estimates.add(estimate);
            customGasEstimateMapper.batchInsertOrUpdateSelective(estimates, GasEstimate.Column.values());
        }

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        return der;
    }

    /**
     * Updated address and received commission reward
     */
    private void updateAddressHaveReward(DelegateExit businessParam) {
        Address addressInfo = addressMapper.selectByPrimaryKey(businessParam.getTxFrom());
        if (ObjectUtil.isNull(addressInfo)) {
            // If the db does not exist, create a new address in the cache and update the received commission rewards.
            Address address = addressCache.createDefaultAddress(businessParam.getTxFrom());
            address.setHaveReward(businessParam.getDelegateReward());
            addressMapper.insertSelective(address);
        } else {
            customAddressMapper.updateAddressHaveReward(businessParam.getTxFrom(), businessParam.getDelegateReward());
        }
    }

}
