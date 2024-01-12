package com.turn.browser.service.elasticsearch;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.turn.browser.elasticsearch.dto.*;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.LongSummaryStatistics;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EsImportService {

    @Resource
    private EsBlockService esBlockService;

    @Resource
    private EsTransactionService esTransactionService;

    @Resource
    private EsDelegateRewardService esDelegateRewardService;

    @Resource
    private EsErc20TxService esErc20TxService;

    @Resource
    private EsErc721TxService esErc721TxService;

    @Resource
    private EsErc1155TxService esErc1155TxService;

    private static final int SERVICE_COUNT = 6;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(SERVICE_COUNT);

    /**
     * number of retries
     */
    private AtomicLong retryCount = new AtomicLong(0);

    /**
     * Do you need to retry?
     */
    private AtomicBoolean isRetry = new AtomicBoolean(false);

    private <T> void submit(EsService<T> service, Set<T> data, CountDownLatch latch, ESKeyEnum eSKeyEnum, String traceId) {
        EXECUTOR.submit(() -> {
            try {
                CommonUtil.putTraceId(traceId);
                service.save(data);
                statisticsLog(data, eSKeyEnum);
                isRetry.set(false);
            } catch (IOException e) {
                isRetry.set(true);
                log.error(StrUtil.format("ES[{}] batch storage exception", eSKeyEnum.name()), e);
            } finally {
                latch.countDown();
                CommonUtil.removeTraceId();
            }
        });
    }

    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void batchImport(Set<Block> blocks, Set<Transaction> transactions, Set<DelegationReward> delegationRewards) throws Exception {
        Set<ErcTx> erc20TxList = getErc20TxList(transactions);
        Set<ErcTx> erc721TxList = getErc721TxList(transactions);
        Set<ErcTx> erc1155TxList = getErc1155TxList(transactions);
        if (log.isDebugEnabled()) {
            log.debug("ES batch import: {}(blocks({}), transactions({}), delegationRewards({}), erc20TxList({}), erc721TxList({}), erc721TxList({}))",
                      Thread.currentThread().getStackTrace()[1].getMethodName(),
                      blocks.size(),
                      transactions.size(),
                      delegationRewards.size(),
                      erc20TxList.size(),
                      erc721TxList.size(),
                      erc1155TxList.size());
        }
        try {
            long startTime = System.currentTimeMillis();
            CountDownLatch latch = new CountDownLatch(SERVICE_COUNT);
            submit(esBlockService, blocks, latch, ESKeyEnum.Block, CommonUtil.getTraceId());
            submit(esTransactionService, transactions, latch, ESKeyEnum.Transaction, CommonUtil.getTraceId());
            submit(esDelegateRewardService, delegationRewards, latch, ESKeyEnum.DelegateReward, CommonUtil.getTraceId());
            submit(esErc20TxService, erc20TxList, latch, ESKeyEnum.Erc20Tx, CommonUtil.getTraceId());
            submit(esErc721TxService, erc721TxList, latch, ESKeyEnum.Erc721Tx, CommonUtil.getTraceId());
            submit(esErc1155TxService, erc1155TxList, latch, ESKeyEnum.Erc1155Tx, CommonUtil.getTraceId());
            latch.await();
            if (isRetry.get()) {
                LongSummaryStatistics blockSum = blocks.stream().collect(Collectors.summarizingLong(Block::getNum));
                throw new Exception(StrUtil.format("ES related block [{}]-[{}] information batch storage exception, retry [{}] times", blockSum.getMin(), blockSum.getMax(), retryCount .incrementAndGet()));
            } else {
                retryCount.set(0);
            }
            log.debug("Processing time: {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Loading ES exception", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Retryable(value = BusinessException.class, maxAttempts = Integer.MAX_VALUE)
    public void batchImport(Set<Block> blocks, Set<Transaction> transactions, Set<ErcTx> erc20TxList, Set<ErcTx> erc721TxList, Set<ErcTx> erc1155TxList, Set<DelegationReward> delegationRewards) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("ES batch import: {}(blocks({}), transactions({}), delegationRewards({}), erc20TxList({}), erc721TxList({}), erc721TxList({}))",
                      Thread.currentThread().getStackTrace()[1].getMethodName(),
                      blocks.size(),
                      transactions.size(),
                      delegationRewards.size(),
                      erc20TxList.size(),
                      erc721TxList.size(),
                      erc1155TxList.size());
        }
        try {
            long startTime = System.currentTimeMillis();
            CountDownLatch latch = new CountDownLatch(SERVICE_COUNT);
            submit(esBlockService, blocks, latch, ESKeyEnum.Block, CommonUtil.getTraceId());
            submit(esTransactionService, transactions, latch, ESKeyEnum.Transaction, CommonUtil.getTraceId());
            submit(esDelegateRewardService, delegationRewards, latch, ESKeyEnum.DelegateReward, CommonUtil.getTraceId());
            submit(esErc20TxService, erc20TxList, latch, ESKeyEnum.Erc20Tx, CommonUtil.getTraceId());
            submit(esErc721TxService, erc721TxList, latch, ESKeyEnum.Erc721Tx, CommonUtil.getTraceId());
            submit(esErc1155TxService, erc1155TxList, latch, ESKeyEnum.Erc1155Tx, CommonUtil.getTraceId());
            latch.await();
            if (isRetry.get()) {
                LongSummaryStatistics blockSum = blocks.stream().collect(Collectors.summarizingLong(Block::getNum));
                throw new Exception(StrUtil.format("ES related block [{}]-[{}] information batch storage exception, retry [{}] times", blockSum.getMin(), blockSum.getMax(), retryCount .incrementAndGet()));
            } else {
                retryCount.set(0);
            }
            log.debug("Processing time: {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Loading ES exception", e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Get erc20 transaction list
     */
    public Set<ErcTx> getErc20TxList(Set<Transaction> transactions) {
        Set<ErcTx> result = new HashSet<>();
        if (transactions != null && !transactions.isEmpty()) {
            for (Transaction tx : transactions) {
                if (tx.getErc20TxList().isEmpty()) continue;
                result.addAll(tx.getErc20TxList());
            }
        }
        return result;
    }

    /**
     * Get erc721 transaction list
     */
    public Set<ErcTx> getErc721TxList(Set<Transaction> transactions) {
        Set<ErcTx> result = new HashSet<>();
        if (transactions != null && !transactions.isEmpty()) {
            for (Transaction tx : transactions) {
                if (tx.getErc721TxList().isEmpty()) continue;
                result.addAll(tx.getErc721TxList());
            }
        }
        return result;
    }

    /**
     * Get erc1155 transaction list
     */
    public Set<ErcTx> getErc1155TxList(Set<Transaction> transactions) {
        Set<ErcTx> result = new HashSet<>();
        if (transactions != null && !transactions.isEmpty()) {
            for (Transaction tx : transactions) {
                if (tx.getErc1155TxList().isEmpty()) continue;
                result.addAll(tx.getErc1155TxList());
            }
        }
        return result;
    }

    /**
     * Print statistics info
     *
     * @param data
     * @param eSKeyEnum
     * @return void
     */
    private <T> void statisticsLog(Set<T> data, ESKeyEnum eSKeyEnum) {
        try {
            if (eSKeyEnum.compareTo(ESKeyEnum.Block) == 0) {
                if (CollUtil.isNotEmpty(data)) {
                    LongSummaryStatistics blockSum = ((Set<Block>) data).stream().collect(Collectors.summarizingLong(Block::getNum));
                    log.info("ES batch storage success statistics: block [{}]-[{}]", blockSum.getMin(), blockSum.getMax());
                } else {
                    log.info("ES batch storage success statistics: block [{}]-[{}]", 0, 0);
                }
            } else if (eSKeyEnum.compareTo(ESKeyEnum.Transaction) == 0) {
                log.info("ES batch warehousing success statistics: number of transactions [{}]", data.size());
            } else if (eSKeyEnum.compareTo(ESKeyEnum.Erc20Tx) == 0) {
                log.info("ES batch warehousing success statistics: erc20 transaction number [{}]", data.size());
            } else if (eSKeyEnum.compareTo(ESKeyEnum.Erc721Tx) == 0) {
                log.info("ES batch warehousing success statistics: erc721 transaction number [{}]", data.size());
            } else if (eSKeyEnum.compareTo(ESKeyEnum.Erc1155Tx) == 0) {
                log.info("ES batch warehousing success statistics: erc1155 transaction number [{}]", data.size());
            }
        } catch (Exception e) {
            log.error("ES batch warehousing success statistics printing exception", e);
        }
    }

}
