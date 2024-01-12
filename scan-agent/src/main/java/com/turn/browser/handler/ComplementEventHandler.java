package com.turn.browser.handler;

import com.lmax.disruptor.EventHandler;
import com.turn.browser.bean.ComplementEvent;
import com.turn.browser.publisher.PersistenceEventPublisher;
import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Block event handler
 */
@Slf4j
@Component
public class ComplementEventHandler implements EventHandler<ComplementEvent> {

    @Resource
    private PersistenceEventPublisher persistenceEventPublisher;

    @Override
    public void onEvent(ComplementEvent event, long sequence, boolean endOfBatch) {
        surroundExec(event, sequence, endOfBatch);
    }

    private void surroundExec(ComplementEvent event, long sequence, boolean endOfBatch) {
        CommonUtil.putTraceId(event.getTraceId());
        long startTime = System.currentTimeMillis();
        exec(event, sequence, endOfBatch);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        CommonUtil.removeTraceId();
    }

    private void exec(ComplementEvent event, long sequence, boolean endOfBatch) {
        try {
            // Post to persistence queue
            persistenceEventPublisher.publish(event.getBlock(), event.getTransactions(), event.getNodeOpts(), event.getDelegationRewards(), event.getTraceId());
            // Release object reference
            event.releaseRef();
        } catch (Exception e) {
            log.error("", e);
            throw e;
        }
    }

}