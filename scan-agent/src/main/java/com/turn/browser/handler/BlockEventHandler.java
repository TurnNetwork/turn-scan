package com.turn.browser.handler;

import com.lmax.disruptor.EventHandler;
import com.turn.browser.analyzer.BlockAnalyzer;
import com.turn.browser.bean.BlockEvent;
import com.turn.browser.bean.CollectionBlock;
import com.turn.browser.bean.CommonConstant;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.exception.BeanCreateOrUpdateException;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.ContractInvokeException;
import com.turn.browser.publisher.CollectionEventPublisher;
import com.turn.browser.utils.CommonUtil;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Block event handler
 */
@Slf4j
@Component
public class BlockEventHandler implements EventHandler<BlockEvent> {

    @Resource
    private CollectionEventPublisher collectionEventPublisher;

    @Resource
    private BlockAnalyzer blockAnalyzer;

    /**
     * number of retries
     */
    private AtomicLong retryCount = new AtomicLong(0);

    @Override
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum, label = "BlockEventHandler")
    public void onEvent(BlockEvent event, long sequence, boolean endOfBatch) throws
                                                                             ExecutionException,
                                                                             InterruptedException,
                                                                             BeanCreateOrUpdateException,
                                                                             IOException,
                                                                             ContractInvokeException,
                                                                             BlankResponseException {
        surroundExec(event, sequence, endOfBatch);
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back.
     *
     * @param e:
     * @return: void
     */
    @Recover
    public void recover(Exception e) {
        retryCount.set(0);
        log.error("If the retry is completed or the service fails, please contact the administrator for processing.");
    }

    private void surroundExec(BlockEvent event, long sequence, boolean endOfBatch) throws
                                                                                   InterruptedException,
                                                                                   ExecutionException,
                                                                                   ContractInvokeException,
                                                                                   BeanCreateOrUpdateException,
                                                                                   BlankResponseException {
        CommonUtil.putTraceId(event.getTraceId());
        long startTime = System.currentTimeMillis();
        exec(event, sequence, endOfBatch);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        CommonUtil.removeTraceId();
    }

    private void exec(BlockEvent event, long sequence, boolean endOfBatch) throws
                                                                           InterruptedException,
                                                                           ExecutionException,
                                                                           BlankResponseException,
                                                                           BeanCreateOrUpdateException,
                                                                           ContractInvokeException {
        try {
            BubbleBlock.Block rawBlock = event.getBlockCF().get().getBlock();
            if (retryCount.incrementAndGet() > 1) {
                log.error("The number of retries [{}], the block [{}] is processed repeatedly, which may cause repeated data statistics.",
                          retryCount.get(),
                          rawBlock.getNumber());
            }
            ReceiptResult receiptResult = event.getReceiptCF().get();
            log.info("There are [{}] transactions in the current block [{}]",
                     rawBlock.getNumber(),
                     CommonUtil.ofNullable(() -> rawBlock.getTransactions().size()).orElse(0));
            // Analysis block
            CollectionBlock block = blockAnalyzer.analyze(rawBlock, receiptResult);
            block.setReward(event.getEpochMessage().getBlockReward().toString());
            // TODO It is normal logic to retry the code above this dividing line. If an exception occurs in the following code, the block may have been sent to CollectionEventHandler for processing, and the block will be processed multiple times.
            collectionEventPublisher.publish(block,
                                             block.getTransactions(),
                                             event.getEpochMessage(),
                                             event.getTraceId());
            // Release object reference
            event.releaseRef();
            retryCount.set(0);
        } catch (Exception e) {
            log.error("Block event handling exception", e);
            throw e;
        }
    }

}