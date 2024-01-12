package com.turn.browser.analyzer.ppos;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.custommapper.CustomGasEstimateMapper;
import com.turn.browser.dao.custommapper.DelegateBusinessMapper;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.GasEstimate;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.param.ppos.DelegateRewardClaim;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.DelegateRewardClaimParam;
import com.turn.browser.param.claim.Reward;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: Receive entrustment reward business parameter converter
 **/
@Slf4j
@Service
public class DelegateRewardClaimAnalyzer extends PPOSAnalyzer<DelegationReward> {

    @Resource
    private DelegateBusinessMapper delegateBusinessMapper;

    @Resource
    private AddressCache addressCache;

    @Resource
    private CustomGasEstimateMapper customGasEstimateMapper;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private CustomAddressMapper customAddressMapper;

    /**
     * Receive award
     */
    @Override
    public DelegationReward analyze(CollectionEvent event, Transaction tx) {
        // Initiate a commission
        DelegateRewardClaimParam txParam = tx.getTxParam(DelegateRewardClaimParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        DelegateRewardClaim businessParam = DelegateRewardClaim.builder().address(tx.getFrom()) // 领取者地址
                                                               .rewardList(txParam.getRewardList()) // 领取的奖励列表
                                                               .build();

        delegateBusinessMapper.claim(businessParam);

        // Cumulative total commission rewards
        BigDecimal txTotalReward = BigDecimal.ZERO;
        List<DelegationReward.Extra> extraList = new ArrayList<>();
        List<DelegationReward.Extra> extraCleanList = new ArrayList<>();

        // 1. Receive entrustment rewards and estimate the uncalculated period of gas entrustment. epoch = 0: Directly store into the mysql database.
        List<GasEstimate> estimates = new ArrayList<>();
        for (Reward reward : businessParam.getRewardList()) {
            DelegationReward.Extra extra = new DelegationReward.Extra();
            extra.setNodeId(reward.getNodeId());
            extra.setNodeName(reward.getNodeName());
            extra.setReward(reward.getReward().toString());
            extraList.add(extra);
            if (extra.decimalReward().compareTo(BigDecimal.ZERO) > 0) {
                extraCleanList.add(extra);
            }
            txTotalReward = txTotalReward.add(reward.getReward());

            GasEstimate estimate = new GasEstimate();
            estimate.setNodeId(reward.getNodeId());
            estimate.setSbn(reward.getStakingNum().longValue());
            estimate.setAddr(tx.getFrom());
            estimate.setEpoch(0L);
            estimates.add(estimate);
        }

        DelegationReward delegationReward = null;
        if (txTotalReward.compareTo(BigDecimal.ZERO) > 0) {
            // If the total reward is greater than zero, record the receipt details
            delegationReward = new DelegationReward();
            delegationReward.setHash(tx.getHash());
            delegationReward.setBn(tx.getNum());
            delegationReward.setAddr(tx.getFrom());
            delegationReward.setTime(tx.getTime());
            delegationReward.setCreTime(new Date());
            delegationReward.setUpdTime(new Date());
            delegationReward.setExtra(JSON.toJSONString(extraList));
            delegationReward.setExtraClean(JSON.toJSONString(extraCleanList));
        }

        updateAddressHaveReward(businessParam);

        // Directly into the mysql database
        customGasEstimateMapper.batchInsertOrUpdateSelective(estimates, GasEstimate.Column.values());

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        return delegationReward;
    }

    /**
     * Updated address and received commission reward
     */
    private void updateAddressHaveReward(DelegateRewardClaim businessParam) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Reward reward : businessParam.getRewardList()) {
            totalAmount = totalAmount.add(reward.getReward());
        }
        Address addressInfo = addressMapper.selectByPrimaryKey(businessParam.getAddress());
        if (ObjectUtil.isNull(addressInfo)) {
            // If the db does not exist, create a new address in the cache and update the received commission rewards.
            Address address = addressCache.createDefaultAddress(businessParam.getAddress());
            address.setHaveReward(totalAmount);
            addressMapper.insertSelective(address);
        } else {
            customAddressMapper.updateAddressHaveReward(businessParam.getAddress(), totalAmount);
        }
    }

}
