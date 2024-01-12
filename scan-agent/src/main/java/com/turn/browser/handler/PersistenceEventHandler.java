package com.turn.browser.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.lmax.disruptor.EventHandler;
import com.turn.browser.bean.CommonConstant;
import com.turn.browser.bean.PersistenceEvent;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.config.DisruptorConfig;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.service.elasticsearch.EsImportService;
import com.turn.browser.service.redis.RedisImportService;
import com.turn.browser.utils.CommonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Block event handler
 */
@Slf4j
@Component
public class PersistenceEventHandler implements EventHandler<PersistenceEvent> {

    @Resource
    private EsImportService esImportService;

    @Resource
    private RedisImportService redisImportService;

    @Resource
    private NetworkStatCache networkStatCache;

    @Resource
    private DisruptorConfig disruptorConfig;

    // Maximum block number processed
    @Getter
    private volatile long maxBlockNumber;

    private Set<Block> blockStage = new HashSet<>();

    private Set<Transaction> transactionStage = new HashSet<>();

    private Set<DelegationReward> delegationRewardStage = new HashSet<>();

    /**
     * number of retries
     */
    private AtomicLong retryCount = new AtomicLong(0);

    @Override
    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE, label = "PersistenceEventHandler")
    public void onEvent(PersistenceEvent event, long sequence, boolean endOfBatch) throws Exception {
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

    private void surroundExec(PersistenceEvent event, long sequence, boolean endOfBatch) throws Exception {
        CommonUtil.putTraceId(event.getTraceId());
        long startTime = System.currentTimeMillis();
        exec(event, sequence, endOfBatch);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        CommonUtil.removeTraceId();
    }

    private void exec(PersistenceEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            log.info("The current block [{}] has [{}] transactions, [{}] node operations, and [{}] commission rewards",
                     event.getBlock().getNum(),
                     CommonUtil.ofNullable(() -> event.getTransactions().size()).orElse(0),
                     CommonUtil.ofNullable(() -> event.getNodeOpts().size()).orElse(0),
                     CommonUtil.ofNullable(() -> event.getDelegationRewards().size()).orElse(0));
            blockStage.add(event.getBlock());
            transactionStage.addAll(event.getTransactions());
            // Remove redundant fields in Transaction
            if (CollUtil.isNotEmpty(transactionStage)) {
                for (Transaction transaction : transactionStage) {
                    transaction.setBin("");
                }
            }
            delegationRewardStage.addAll(event.getDelegationRewards());
            List<Long> blockNums = CollUtil.newArrayList();
            if (retryCount.incrementAndGet() > 1) {
                if (CollUtil.isNotEmpty(blockStage)) {
                    blockNums = blockStage.stream().map(Block::getNum).sorted().collect(Collectors.toList());
                }
                // The ES ID uses hash as the key, which will be overwritten if it is stored repeatedly.
                log.error("The data of the relevant block [{}] is repeatedly entered into the database, which may cause the data to be repeatedly entered into the database. The number of retries is [{}]", JSONUtil.toJsonStr(blockNums), retryCount.get());
            }

            // Set the transaction list attribute of the block to null to prevent transaction information from being stored in the block information.
            event.getBlock().setTransactions(null);

            // If the number of blocks in the block temporary storage area does not reach the batch storage size, return
            if (blockStage.size() < disruptorConfig.getPersistenceBatchSize()) {
                maxBlockNumber = event.getBlock().getNum();
                retryCount.set(0);
                return;
            } else {
                blockNums = blockStage.stream().map(Block::getNum).sorted().collect(Collectors.toList());
                log.info("The relevant block [{}] meets the storage standards", JSONUtil.toJsonStr(blockNums));
            }

            statisticsLog();

            // Inbound ES Inbound node operations are recorded to ES
            esImportService.batchImport(blockStage, transactionStage, delegationRewardStage);
            // Store in Redis and update statistical records in Redis
            Set<NetworkStat> statistics = new HashSet<>();
            statistics.add(networkStatCache.getNetworkStat());
            redisImportService.batchImport(blockStage, transactionStage, statistics);
            blockStage.clear();
            transactionStage.clear();
            delegationRewardStage.clear();

            maxBlockNumber = event.getBlock().getNum();
            // Release object reference
            event.releaseRef();
            retryCount.set(0);
        } catch (Exception e) {
            log.error("Data storage exception", e);
            throw e;
        }
    }

    /**
     * Print statistics
     *
     * @param
     * @return void
     */
    private void statisticsLog() {
        try {
            Map<Object, List<Transaction>> map = transactionStage.stream().collect(Collectors.groupingBy(Transaction::getNum));
            if (CollUtil.isNotEmpty(transactionStage)) {
                map.forEach((blockNum, transactions) -> {
                    IntSummaryStatistics erc20Size = transactions.stream().collect(Collectors.summarizingInt(transaction -> transaction.getErc20TxList().size()));
                    IntSummaryStatistics erc721Size = transactions.stream().collect(Collectors.summarizingInt(transaction -> transaction.getErc721TxList().size()));
                    IntSummaryStatistics erc1155Size = transactions.stream().collect(Collectors.summarizingInt(transaction -> transaction.getErc1155TxList().size()));
                    IntSummaryStatistics transferTxSize = transactions.stream().collect(Collectors.summarizingInt(transaction -> transaction.getTransferTxList().size()));
                    IntSummaryStatistics pposTxSize = transactions.stream().collect(Collectors.summarizingInt(transaction -> transaction.getPposTxList().size()));
                    IntSummaryStatistics virtualTransactionSize = transactions.stream().collect(Collectors.summarizingInt(transaction -> transaction.getVirtualTransactions().size()));
                    log.info("Prepare to store redis and ES: The current block height is [{}], the number of transactions is [{}], the number of erc20 transactions is [{}], the number of erc721 transactions is [{}], and the number of erc1155 transactions is [{}] , the number of internal transfer transactions is [{}], the number of PPOS call transactions is [{}], and the number of virtual transactions is [{}]",
                             blockNum,
                             CommonUtil.ofNullable(() -> transactions.size()).orElse(0),
                             erc20Size.getSum(),
                             erc721Size.getSum(),
                             erc1155Size.getSum(),
                             transferTxSize.getSum(),
                             pposTxSize.getSum(),
                             virtualTransactionSize.getSum());
                });
            }
        } catch (Exception e) {
            log.error("Exception in printing statistics ", e);
        }
    }

}