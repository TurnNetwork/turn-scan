package com.turn.browser.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.turn.browser.bean.SubChainTx;
import com.turn.browser.service.elasticsearch.EsSubChainTxService;
import com.turn.browser.utils.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SubChainTxConsumer {

    @Resource
    private EsSubChainTxService esSubChainTxService;

    @KafkaListener(topics = {"${scan.kafka.topic.sub-chain-tx}"}, clientIdPrefix = "SubChainTx")
    public void handle(ConsumerRecord<?, ?> consumer, Acknowledgment ack) {
        TraceIdUtil.putTraceId(consumer.key().toString());
        log.info("topic name: {}, key: {}, partition location: {}, subscript: {}, thread ID: {}",
                consumer.topic(),
                consumer.key(),
                consumer.partition(),
                consumer.offset(),
                Thread.currentThread().getId());
        try {
            List<SubChainTx> list = JSONUtil.toList((String) consumer.value(), SubChainTx.class);

            Set<SubChainTx> subChainTxSet = new HashSet<>(list.size());
            subChainTxSet.addAll(list);

            try {
                esSubChainTxService.save(subChainTxSet);
            } catch (IOException e) {
                log.error("subChainTx add error:{}",e);
            }
            ack.acknowledge();
        } catch (Exception e) {
            throw e;
        }


    }
}
