package com.turn.browser.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lmax.disruptor.EventHandler;
import com.turn.browser.analyzer.TransactionAnalyzer;
import com.turn.browser.bean.*;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.cache.NodeCache;
import com.turn.browser.dao.custommapper.*;
import com.turn.browser.dao.mapper.NodeMapper;
import com.turn.browser.elasticsearch.dto.*;
import com.turn.browser.publisher.ComplementEventPublisher;
import com.turn.browser.service.block.BlockService;
import com.turn.browser.service.ppos.PPOSService;
import com.turn.browser.service.statistic.StatisticService;
import com.turn.browser.utils.CommonUtil;
import com.turn.browser.v0152.analyzer.MicroNodeAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Block event handler
 */
@Slf4j
@Component
public class CollectionEventHandler implements EventHandler<CollectionEvent> {

    @Resource
    private PPOSService pposService;

    @Resource
    private BlockService blockService;

    @Resource
    private StatisticService statisticService;

    @Resource
    private ComplementEventPublisher complementEventPublisher;

    @Resource
    private CustomNOptBakMapper customNOptBakMapper;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private CustomTxBakMapper customTxBakMapper;

    @Resource
    private AddressCache addressCache;

    @Resource
    private NodeCache nodeCache;

    @Resource
    private TransactionAnalyzer transactionAnalyzer;

    @Resource
    private CustomTx20BakMapper customTx20BakMapper;

    @Resource
    private CustomTx721BakMapper customTx721BakMapper;

    @Resource
    private CustomTx1155BakMapper customTx1155BakMapper;

    @Resource
    private CustomTxDelegationRewardBakMapper customTxDelegationRewardBakMapper;

    @Resource
    private MicroNodeAnalyzer microNodeAnalyzer;

    /**
     * number of retries
     */
    private AtomicLong retryCount = new AtomicLong(0);

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    @Retryable(value = Exception.class, maxAttempts = Integer.MAX_VALUE)
    public void onEvent(CollectionEvent event, long sequence, boolean endOfBatch) throws Exception {
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

    private void surroundExec(CollectionEvent event, long sequence, boolean endOfBatch) throws Exception {
        CommonUtil.putTraceId(event.getTraceId());
        long startTime = System.currentTimeMillis();
        exec(event, sequence, endOfBatch);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        CommonUtil.removeTraceId();
    }

    private void exec(CollectionEvent event, long sequence, boolean endOfBatch) throws Exception {
        // Ensure that the event is the original copy, and the retry mechanism uses copyEvent every time
        CollectionEvent copyEvent = copyCollectionEvent(event);
        try {
            Map<String, Receipt> receiptMap = copyEvent.getBlock().getReceiptMap();
            List<com.bubble.protocol.core.methods.response.Transaction> rawTransactions = copyEvent.getBlock().getOriginTransactions();
            for (com.bubble.protocol.core.methods.response.Transaction tr : rawTransactions) {
                CollectionTransaction transaction = transactionAnalyzer.analyze(copyEvent.getBlock(), tr, receiptMap.get(tr.getHash()));
                // Add the parsed transaction to the transaction list of the current block
                copyEvent.getBlock().getTransactions().add(transaction);
                copyEvent.getTransactions().add(transaction);
                // Set the erc20 transaction number and erc721u transaction number of the current block in order to update the network_stat table
                copyEvent.getBlock().setErc20TxQty(copyEvent.getBlock().getErc20TxQty() + transaction.getErc20TxList().size());
                copyEvent.getBlock().setErc721TxQty(copyEvent.getBlock().getErc721TxQty() + transaction.getErc721TxList().size());
                copyEvent.getBlock().setErc1155TxQty(copyEvent.getBlock().getErc1155TxQty() + transaction.getErc1155TxList().size());
            }
            // sub chain release
            microNodeAnalyzer.releaseBubble(copyEvent.getBlock().getNum());

            List<Transaction> transactions = copyEvent.getTransactions();
            // Ensure transaction index order from small to large
            transactions.sort(Comparator.comparing(Transaction::getIndex));

            // Parse business parameters based on block number
            List<NodeOpt> nodeOpts1 = blockService.analyze(copyEvent);
            // Analyze business parameters based on transactions
            TxAnalyseResult txAnalyseResult = pposService.analyze(copyEvent);
            // Summary operation records
            if (CollUtil.isNotEmpty(txAnalyseResult.getNodeOptList())) {
                nodeOpts1.addAll(txAnalyseResult.getNodeOptList());
            }
            // Transactions are stored in mysql. Because the cache cannot implement self-incrementing IDs, the operation log table will no longer be deleted.
            if (CollUtil.isNotEmpty(transactions)) {
                // 依赖于数据库的自增id
                customTxBakMapper.batchInsertOrUpdateSelective(transactions);
                addTxErc20Bak(transactions);
                addTxErc721Bak(transactions);
                addTxErc1155Bak(transactions);
            }
            List<DelegationReward> delegationRewardList = txAnalyseResult.getDelegationRewardList();
            // Delegated reward transactions are stored in the database
            if (CollUtil.isNotEmpty(delegationRewardList)) {
                customTxDelegationRewardBakMapper.batchInsert(delegationRewardList);
            }
            // The operation log is stored in mysql, and then synchronized to es by the scheduled task. Because the cache cannot realize the auto-increment ID, it is no longer included in the database by the ring queue, and the operation log table is no longer deleted.
            if (CollUtil.isNotEmpty(nodeOpts1)) {
                // Depends on the auto-increment id of the database
                customNOptBakMapper.batchInsertOrUpdateSelective(nodeOpts1);
            }
            // Statistical business parameters are based on the MySQL database block height, so it must be ensured that the block height is the last one entered into the database.
            statisticService.analyze(copyEvent);
            // TODO It is normal logic to retry code exceptions above this dividing line. If an exception occurs in the following code, the block-related transactions may have been sent to the ComplementEventHandler for processing, and the block will be processed multiple times.
            complementEventPublisher.publish(copyEvent.getBlock(), transactions, nodeOpts1, delegationRewardList, event.getTraceId());
            // Release object reference
            event.releaseRef();
            retryCount.set(0);
        } catch (Exception e) {
            log.error(StrUtil.format("Block[{}] parsing transaction exception", copyEvent.getBlock().getNum()), e);
            throw e;
        } finally {
            // Whether the current transaction ends normally or abnormally, the address cache needs to be reset to prevent dirty data from being retained in the cache if there is a problem anywhere in the code.
            // Because the address cache is the incremental cache of the current transaction, when StatisticsAddressAnalyzer merges data into the database:
            // 1. If an exception occurs, due to transaction guarantee, the address data of the current transaction statistics will not be stored in mysql. At this time, the incremental cache should be cleared and the cache will be regenerated during the next retry.
            // 2. If it ends normally, the address data of the current transaction statistics will be stored in mysql. At this time, the incremental cache should be cleared.
            addressCache.cleanAll();
        }
    }

    /**
     * Simulate deep copy
     * Because CollectionEvent refers to a third-party jar object and does not implement a serialized interface, deep copying cannot be done.
     *
     * @param event:
     * @return: com.turn.browser.bean.CollectionEvent
     */
    private CollectionEvent copyCollectionEvent(CollectionEvent event) {
        CollectionEvent copyEvent = new CollectionEvent();
        Block block = new Block();
        BeanUtil.copyProperties(event.getBlock(), block);
        copyEvent.setBlock(block);
        copyEvent.getTransactions().addAll(event.getTransactions());
        EpochMessage epochMessage = EpochMessage.newInstance();
        BeanUtil.copyProperties(event.getEpochMessage(), epochMessage);
        copyEvent.setEpochMessage(epochMessage);
        copyEvent.setTraceId(event.getTraceId());
        if (retryCount.incrementAndGet() > 1) {
            initNodeCache();
            List<String> txHashList = CollUtil.newArrayList();
            if (CollUtil.isNotEmpty(event.getBlock().getOriginTransactions())) {
                txHashList = event.getBlock().getOriginTransactions().stream().map(com.bubble.protocol.core.methods.response.Transaction::getHash).collect(Collectors.toList());
            }
            log.warn("The number of retries [{}], the node is re-initialized, and the block [{}] transaction list {} is processed repeatedly", retryCount.get(), event.getBlock().getNum(), JSONUtil.toJsonStr(txHashList));
        }
        return copyEvent;
    }

    /**
     * Initialize node cache
     *
     * @param :
     * @return: void
     */
    private void initNodeCache() {
        nodeCache.cleanNodeCache();
        List<com.turn.browser.dao.entity.Node> nodeList = nodeMapper.selectByExample(null);
        nodeCache.init(nodeList);
    }

    /**
     * erc20 transaction storage
     *
     * @param transactions:
     * @return: void
     */
    private void addTxErc20Bak(List<Transaction> transactions) {
        List<ErcTx> erc20List = new ArrayList<>();
        transactions.forEach(transaction -> {
            if (CollUtil.isNotEmpty(transaction.getErc20TxList())) {
                erc20List.addAll(transaction.getErc20TxList());
            }
        });
        if (CollUtil.isNotEmpty(erc20List)) {
            customTx20BakMapper.batchInsert(erc20List);
        }
    }

    /**
     * erc721 transaction storage
     *
     * @param transactions:
     * @return: void
     */
    private void addTxErc721Bak(List<Transaction> transactions) {
        List<ErcTx> erc721List = new ArrayList<>();
        transactions.forEach(transaction -> {
            if (CollUtil.isNotEmpty(transaction.getErc721TxList())) {
                erc721List.addAll(transaction.getErc721TxList());
            }
        });
        if (CollUtil.isNotEmpty(erc721List)) {
            customTx721BakMapper.batchInsert(erc721List);
        }
    }

    /**
     * erc1155 transaction storage
     *
     * @param transactions:
     * @return: void
     */
    private void addTxErc1155Bak(List<Transaction> transactions) {
        Set<ErcTx> erc1155Set = new HashSet<>();
        transactions.forEach(transaction -> {
            if (CollUtil.isNotEmpty(transaction.getErc1155TxList())) {
                erc1155Set.addAll(transaction.getErc1155TxList());
            }
        });
        if (CollUtil.isNotEmpty(erc1155Set)) {
            customTx1155BakMapper.batchInsert(erc1155Set);
        }
    }
}
