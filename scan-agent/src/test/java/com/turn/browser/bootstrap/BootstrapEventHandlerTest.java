package com.turn.browser.bootstrap;

import com.turn.browser.AgentTestBase;
import com.turn.browser.analyzer.BlockAnalyzer;
import com.turn.browser.bean.CollectionBlock;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.client.TurnClient;
import com.turn.browser.dao.entity.TxBak;
import com.turn.browser.dao.mapper.NOptBakMapper;
import com.turn.browser.dao.mapper.TxBakMapper;
import com.turn.browser.exception.BeanCreateOrUpdateException;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.ContractInvokeException;
import com.turn.browser.service.elasticsearch.EsImportService;
import com.turn.browser.service.redis.RedisImportService;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class BootstrapEventHandlerTest extends AgentTestBase {

    @Mock
    private EsImportService esImportService;

    @Mock
    private RedisImportService redisImportService;

    @Mock
    private TxBakMapper txBakMapper;

    @Mock
    private NOptBakMapper nOptBakMapper;

    @Mock
    private TurnClient turnClient;

    @Mock
    private AddressCache addressCache;

    @Spy
    private BlockAnalyzer blockAnalyzer;

    @InjectMocks
    private BootstrapEventHandler target;

    private ReceiptResult receiptResult;

    @Before
    public void setup() {
        receiptResult = receiptResultList.get(0);
    }

    @Test
    public void test() throws Exception {
        CompletableFuture<BubbleBlock> blockCF = getBlockAsync(7000L);
        CompletableFuture<ReceiptResult> receiptCF = getReceiptAsync(7000L);
        BootstrapEvent bootstrapEvent = new BootstrapEvent();
        bootstrapEvent.setBlockCF(blockCF);
        bootstrapEvent.setReceiptCF(receiptCF);
        ShutdownCallback sc = new ShutdownCallback();
        sc.setEndBlockNum(7000L);
        bootstrapEvent.setCallback(sc);

        List<TxBak> txBaks = new ArrayList<>();
        TxBak bak = new TxBak();
        bak.setHash(receiptCF.get().getResult().get(0).getTransactionHash());
        bak.setNum(receiptCF.get().getResult().get(0).getBlockNumber());
        bak.setId(100L);
        txBaks.add(bak);
        when(txBakMapper.selectByExample(any())).thenReturn(txBaks);
        target.onEvent(bootstrapEvent, 1, false);

    }

    /**
     * 异步获取区块
     */
    public CompletableFuture<BubbleBlock> getBlockAsync(Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> {
            BubbleBlock pb = new BubbleBlock();
            BubbleBlock.Block block = rawBlockMap.get(receiptResult.getResult().get(0).getBlockNumber());
            pb.setResult(block);
            return pb;
        });
    }

    /**
     * 异步获取区块
     */
    public CompletableFuture<ReceiptResult> getReceiptAsync(Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> receiptResult);
    }

}
