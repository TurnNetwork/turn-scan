package com.turn.browser.publisher;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.handler.CollectionEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Block collection event publisher
 */
@Slf4j
@Component
public class CollectionEventPublisher extends AbstractPublisher<CollectionEvent> {

    private static final EventTranslatorVararg<CollectionEvent>
            TRANSLATOR = (event, sequence, args) -> {
        event.setBlock((Block) args[0]);
        event.setTransactions((List<Transaction>) args[1]);
        event.setEpochMessage((EpochMessage) args[2]);
        event.setTraceId((String) args[3]);
    };

    @Override
    public int getRingBufferSize() {
        return config.getCollectionBufferSize();
    }

    private EventFactory<CollectionEvent> eventFactory = CollectionEvent::new;

    @Resource
    private CollectionEventHandler collectionEventHandler;

    @PostConstruct
    private void init() {
        Disruptor<CollectionEvent> disruptor = new Disruptor<>(eventFactory, getRingBufferSize(), DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(collectionEventHandler);
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
        register(CollectionEventPublisher.class.getSimpleName(), this);
    }

    public void publish(Block block, List<Transaction> transactions, EpochMessage epochMessage, String traceId) {
        ringBuffer.publishEvent(TRANSLATOR, block, transactions, epochMessage, traceId);
    }

}
