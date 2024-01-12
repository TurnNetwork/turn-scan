package com.turn.browser.bootstrap;

import com.lmax.disruptor.EventHandler;
import com.turn.browser.analyzer.BlockAnalyzer;
import com.turn.browser.bean.CollectionBlock;
import com.turn.browser.bean.CommonConstant;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.dao.entity.TxBak;
import com.turn.browser.dao.entity.TxBakExample;
import com.turn.browser.dao.mapper.TxBakMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.service.elasticsearch.EsImportService;
import com.turn.browser.service.redis.RedisImportService;
import com.turn.browser.utils.CommonUtil;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Self-test event handler
 */
@Slf4j
@Component
public class BootstrapEventHandler implements EventHandler<BootstrapEvent> {

    @Resource
    private EsImportService esImportService;

    @Resource
    private RedisImportService redisImportService;

    @Resource
    private TxBakMapper txBakMapper;

    @Resource
    private BlockAnalyzer blockAnalyzer;

    private Set<Block> blocks = new HashSet<>();

    private Set<Transaction> transactions = new HashSet<>();

    @Override
    @Retryable(value = Exception.class, maxAttempts = CommonConstant.reTryNum, label = "BootstrapEventHandler")
    public void onEvent(BootstrapEvent event, long sequence, boolean endOfBatch) throws Exception {
        surroundExec(event, sequence, endOfBatch);
    }

    /**
     * If the retry is completed or unsuccessful, this method will be called back.
     *
     * @param e:
     * @return: void
     * @date: 2022/5/6
     */
    @Recover
    public void recover(Exception e) {
        log.error("If the retry is completed or the service fails, please contact the administrator for processing.");
    }

    private void surroundExec(BootstrapEvent event, long sequence, boolean endOfBatch) throws Exception {
        CommonUtil.putTraceId(event.getTraceId());
        long startTime = System.currentTimeMillis();
        exec(event, sequence, endOfBatch);
        log.info("Processing time:{} ms", System.currentTimeMillis() - startTime);
        CommonUtil.removeTraceId();
    }

    private void exec(BootstrapEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            BubbleBlock.Block rawBlock = event.getBlockCF().get().getBlock();
            ReceiptResult receiptResult = event.getReceiptCF().get();
            CollectionBlock block = blockAnalyzer.analyze(rawBlock, receiptResult);

            this.clear();
            this.blocks.add(block);
            this.transactions.addAll(block.getTransactions());
            block.setTransactions(null);

            Long txMaxId = 0L;
            if (!this.transactions.isEmpty()) {
                // Query transaction information supplementary table and fill in missing information
                List<String> txHashes = new ArrayList<>();
                this.transactions.forEach(tx -> txHashes.add(tx.getHash()));
                TxBakExample txBakExample = new TxBakExample();
                txBakExample.createCriteria().andHashIn(txHashes);
                List<TxBak> txBaks = this.txBakMapper.selectByExample(txBakExample);
                Map<String, TxBak> txBakMap = new HashMap<>();
                for (TxBak bak : txBaks) {
                    if (bak.getId() > txMaxId) txMaxId = bak.getId();
                    txBakMap.put(bak.getHash(), bak);
                }
                // Update transaction information stored in ES and Redis
                this.transactions.forEach(tx -> {
                    TxBak bak = txBakMap.get(tx.getHash());
                    if (bak == null) {
                        log.error("Transaction [{}] cannot find backup information in the transaction backup table!", tx.getHash());
                        return;
                    }
                    BeanUtils.copyProperties(bak, tx);
                });
            }

            this.esImportService.batchImport(this.blocks, this.transactions, Collections.emptySet());
            this.redisImportService.batchImport(this.blocks, this.transactions, Collections.emptySet());

            this.clear();
            event.getCallback().call(block.getNum());

            // Releases the event's reference to the object
            event.releaseRef();
        } catch (Exception e) {
            log.error("", e);
            throw e;
        }

    }

    private void clear() {
        this.blocks.clear();
        this.transactions.clear();
    }

}