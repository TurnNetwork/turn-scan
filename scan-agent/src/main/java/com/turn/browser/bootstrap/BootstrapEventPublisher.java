package com.turn.browser.bootstrap;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.publisher.AbstractPublisher;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * Self-test event producer
 */
@Slf4j
@Component
public class BootstrapEventPublisher extends AbstractPublisher<BootstrapEvent> {

    private static final EventTranslatorVararg<BootstrapEvent>
            TRANSLATOR = (event, sequence, args) -> {
        event.setBlockCF((CompletableFuture<BubbleBlock>) args[0]);
        event.setReceiptCF((CompletableFuture<ReceiptResult>) args[1]);
        event.setCallback((Callback) args[2]);
        event.setTraceId((String) args[3]);
    };

    @Override
    public int getRingBufferSize() {
        return config.getBlockBufferSize();
    }

    private EventFactory<BootstrapEvent> eventFactory = BootstrapEvent::new;

    @Resource
    private BootstrapEventHandler handler;

    @Getter
    private Disruptor<BootstrapEvent> disruptor;

    @PostConstruct
    public void init() {
        disruptor = new Disruptor<>(eventFactory, getRingBufferSize(), DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(handler);
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
        register(BootstrapEventPublisher.class.getSimpleName(), this);
    }

    public void publish(CompletableFuture<BubbleBlock> blockCF, CompletableFuture<ReceiptResult> receiptCF, Callback callback, String traceId) {
        ringBuffer.publishEvent(TRANSLATOR, blockCF, receiptCF, callback, traceId);
    }

    public void shutdown() {
        disruptor.shutdown();
        unregister(BootstrapEventPublisher.class.getSimpleName());
    }

}
