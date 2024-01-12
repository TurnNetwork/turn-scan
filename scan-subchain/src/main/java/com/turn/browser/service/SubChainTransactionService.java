package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.turn.browser.bean.SubChainTx;
import com.turn.browser.dao.entity.MicroNodeOptBak;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.enums.RetEnum;
import com.turn.browser.kafka.TopicProperties;
import com.turn.browser.request.SubChainTxListReq;
import com.turn.browser.request.SubChainTxReq;
import com.turn.browser.response.BaseResp;
import com.turn.browser.service.elasticsearch.EsSubChainTxRepository;
import com.turn.browser.service.elasticsearch.EsSubChainTxService;
import com.turn.browser.service.elasticsearch.bean.ESResult;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilderConstructor;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SubChainTransactionService {

    @Resource
    private EsSubChainTxRepository esSubChainTxRepository;

    @Resource
    private KafkaTemplate<Object, Object> kafkaTemplateWithTransaction;

    @Value("${scan.kafka.topic.partition:3}")
    private Integer topicPartition;

    @Resource
    private TopicProperties topicProperties;

    public Boolean submitSubChainTransaction(SubChainTxListReq reqList) {
        if(CollUtil.isEmpty(reqList.getSubChainTxReqSet())){
            return false;
        }
        Long bubbleId = reqList.getBubbleId();
        SubChainTxReq req = reqList.getSubChainTxReqSet().get(0);
        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().term("hash", req.getHash()));
        constructor.must(new ESQueryBuilders().term("bubbleId", bubbleId));
        ESResult<SubChainTx> items;
        try {
            items = esSubChainTxRepository.search(constructor, SubChainTx.class, 1, 1);
        } catch (Exception e) {
            log.error("Error in obtaining sub-chain transaction record.", e);
            return false;
        }
        if(items.getTotal()>0){
            log.error("Duplicate sub-chain transaction records: bubbleId-{},hash{}", bubbleId,req.getHash());
            return false;
        }

        //Send message to kafka
        List<SubChainTxReq> subChainTxReqSet = reqList.getSubChainTxReqSet();
        subChainTxReqSet.forEach(item->item.setBubbleId(bubbleId));
        kafkaTemplateWithTransaction.executeInTransaction(operations -> {
            int partition = (int) (bubbleId % topicPartition);
            operations.send(topicProperties.getSubChainTx(),
                    partition,
                    reqList.getNodeId(),
                    JSONUtil.toJsonStr(subChainTxReqSet));
            return reqList.getNodeId();
        });
        return true;
    }
}
