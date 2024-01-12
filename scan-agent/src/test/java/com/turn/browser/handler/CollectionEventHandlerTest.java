package com.turn.browser.handler;

import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.bean.TxAnalyseResult;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.custommapper.CustomNOptBakMapper;
import com.turn.browser.dao.custommapper.CustomTxBakMapper;
import com.turn.browser.dao.mapper.NOptBakMapper;
import com.turn.browser.dao.mapper.TxBakMapper;
import com.turn.browser.publisher.ComplementEventPublisher;
import com.turn.browser.service.block.BlockService;
import com.turn.browser.service.ppos.PPOSService;
import com.turn.browser.service.statistic.StatisticService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class CollectionEventHandlerTest extends AgentTestBase {

    @Mock
    private PPOSService pposService;

    @Mock
    private BlockService blockService;

    @Mock
    private StatisticService statisticService;

    @Mock
    private ComplementEventPublisher complementEventPublisher;

    @Mock
    private NetworkStatCache networkStatCache;

    @Mock
    private NOptBakMapper nOptBakMapper;

    @Mock
    private TxBakMapper txBakMapper;

    @Mock
    private CustomTxBakMapper customTxBakMapper;

    @Mock
    private CustomNOptBakMapper customNOptBakMapper;

    @InjectMocks
    @Spy
    private CollectionEventHandler target;

    @Mock
    private AddressCache addressCache;

    @Before
    public void setup() throws Exception {

        NetworkStat networkStat = mock(NetworkStat.class);
        when(networkStatCache.getNetworkStat()).thenReturn(networkStat);
        when(networkStat.getTxQty()).thenReturn(1000);
        when(blockService.analyze(any())).thenReturn(nodeOptList);
        TxAnalyseResult txAnalyseResult = TxAnalyseResult.builder()
                .nodeOptList(nodeOptList)
                .delegationRewardList(new ArrayList<>())
                .build();
        when(pposService.analyze(any())).thenReturn(txAnalyseResult);
        when(txBakMapper.deleteByExample(any())).thenReturn(100);
        when(nOptBakMapper.deleteByExample(any())).thenReturn(100);
    }

    @Test
    public void test() throws Exception {
        CollectionEvent event = new CollectionEvent();
        event.setBlock(blockList.get(0));
        event.setEpochMessage(EpochMessage.newInstance());
        event.setTransactions(new ArrayList<>(transactionList));
//        when(customTxBakMapper.batchInsertOrUpdateSelective(any(), any())).thenReturn(100);
        target.onEvent(event, 33, false);
        verify(target, times(1)).onEvent(any(), anyLong(), anyBoolean());

        doThrow(new RuntimeException("")).when(blockService).analyze(any());
        try {
            target.onEvent(event, 33, false);
        } catch (Exception e) {
        }
    }

}
