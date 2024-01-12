package com.turn.browser.handler;

import com.turn.browser.AgentTestBase;
import com.turn.browser.analyzer.BlockAnalyzer;
import com.turn.browser.bean.BlockEvent;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.client.TurnClient;
import com.turn.browser.exception.BeanCreateOrUpdateException;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.ContractInvokeException;
import com.turn.browser.publisher.CollectionEventPublisher;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class BlockEventHandlerTest extends AgentTestBase {

    @Mock
    private CollectionEventPublisher collectionEventPublisher;

    @Mock
    private TurnClient turnClient;

    @Mock
    private AddressCache addressCache;

    @Spy
    private BlockAnalyzer blockAnalyzer;

    @InjectMocks
    private BlockEventHandler target;

    private ReceiptResult receiptResult;

    @Before
    public void setup() {
        receiptResult = receiptResultList.get(0);
    }

    @Test
    public void test() throws InterruptedException, ExecutionException, BeanCreateOrUpdateException, IOException, ContractInvokeException, BlankResponseException {
        CompletableFuture<BubbleBlock> blockCF = getBlockAsync(7000L);
        CompletableFuture<ReceiptResult> receiptCF = getReceiptAsync(7000L);
        BlockEvent blockEvent = new BlockEvent();
        blockEvent.setBlockCF(blockCF);
        blockEvent.setReceiptCF(receiptCF);
        blockEvent.setEpochMessage(EpochMessage.newInstance());

        target.onEvent(blockEvent, 1, false);

        //verify(target, times(1)).onEvent(any(), anyLong(), anyBoolean());
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
