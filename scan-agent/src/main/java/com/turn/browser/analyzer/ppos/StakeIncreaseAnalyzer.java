package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.dao.custommapper.StakeBusinessMapper;
import com.turn.browser.dao.param.ppos.StakeIncrease;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.StakeIncreaseParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: Increased stake business parameter converter
 **/
@Slf4j
@Service
public class StakeIncreaseAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private StakeBusinessMapper stakeBusinessMapper;

    /**
     * Increase pledge (increase own pledge)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {

        StakeIncreaseParam txParam = tx.getTxParam(StakeIncreaseParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus())
            return null;

        long startTime = System.currentTimeMillis();


        StakeIncrease businessParam = StakeIncrease.builder()
                                                   .nodeId(txParam.getNodeId())
                                                   .amount(txParam.getAmount())
                                                   .stakingBlockNum(txParam.getStakingBlockNum())
                                                   .build();

        stakeBusinessMapper.increase(businessParam);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(txParam.getNodeId());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.INCREASE.getCode()));
        nodeOpt.setTxHash(tx.getHash());
        nodeOpt.setBNum(tx.getNum());
        nodeOpt.setTime(tx.getTime());


        return nodeOpt;
    }

}
