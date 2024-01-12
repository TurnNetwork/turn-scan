package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.dao.custommapper.StakeBusinessMapper;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingKey;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.ppos.StakeModify;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.exception.NoSuchBeanException;
import com.turn.browser.param.StakeModifyParam;
import com.turn.browser.utils.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: Modify validator business parameter converter
 **/
@Slf4j
@Service
public class StakeModifyAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private StakeBusinessMapper stakeBusinessMapper;

    @Resource
    private StakingMapper stakingMapper;

    /**
     * Modify pledge information (edit validator)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) throws NoSuchBeanException {

        StakeModifyParam txParam = tx.getTxParam(StakeModifyParam.class);

        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus())
            return null;

        long startTime = System.currentTimeMillis();

        StakeModify businessParam = StakeModify.builder()
                                               .nodeId(txParam.getNodeId())
                                               .nodeName(txParam.getNodeName())
                                               .externalId(txParam.getExternalId())
                                               .benefitAddr(txParam.getBenefitAddress())
                                               .webSite(txParam.getWebsite())
                                               .details(txParam.getDetails())
                                               .isInit(isInit(txParam.getBenefitAddress()))
                                               .stakingBlockNum(nodeCache.getNode(txParam.getNodeId()).getStakingBlockNum())
                                               .nextRewardPer(txParam.getDelegateRewardPer())
                                               .settleEpoch(event.getEpochMessage().getSettleEpochRound().intValue())
                                               .build();


        StakingKey stakingKey = new StakingKey();
        stakingKey.setNodeId(txParam.getNodeId());
        stakingKey.setStakingBlockNum(businessParam.getStakingBlockNum().longValue());
        Staking staking = stakingMapper.selectByPrimaryKey(stakingKey);
        String preDelegateRewardRate = "0";
        if (staking != null)
            preDelegateRewardRate = staking.getRewardPer().toString();
        /**
         * If it is an incentive pool contract, the income address will not be modified.
         */
        if (staking != null && InnerContractAddrEnum.INCENTIVE_POOL_CONTRACT.getAddress().equals(staking.getBenefitAddr())) {
            businessParam.setBenefitAddr(InnerContractAddrEnum.INCENTIVE_POOL_CONTRACT.getAddress());
        }

        stakeBusinessMapper.modify(businessParam);
        // Update node cache
        updateNodeCache(HexUtil.prefix(txParam.getNodeId()), txParam.getNodeName());


        String desc = "";
        /**
         * Set desc when the parameter has a value and is not equal to the initial value
         */
        if (txParam.getDelegateRewardPer() != null && !String.valueOf(businessParam.getNextRewardPer()).equals(preDelegateRewardRate)) {
            desc = NodeOpt.TypeEnum.MODIFY.getTpl()
                                          .replace("BEFORERATE", preDelegateRewardRate)
                                          .replace("AFTERRATE", String.valueOf(businessParam.getNextRewardPer()));
        }

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(txParam.getNodeId());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.MODIFY.getCode()));
        nodeOpt.setTxHash(tx.getHash());
        nodeOpt.setBNum(tx.getNum());
        nodeOpt.setTime(tx.getTime());
        nodeOpt.setDesc(desc);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return nodeOpt;
    }

}
