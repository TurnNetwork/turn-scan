package com.turn.browser.publisher;

import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.config.DisruptorConfig;
import com.turn.browser.handler.PersistenceEventHandler;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.utils.CommonUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class PersistenceEventPublisherTest extends AgentTestBase {

    @Mock
    private PersistenceEventHandler handler;

    @InjectMocks
    @Spy
    private PersistenceEventPublisher target;

    @Mock
    protected DisruptorConfig config;

    @Before
    public void setup() {
        when(target.getRingBufferSize()).thenReturn(1024);
    }

    @Test
    public void test() {
        ReflectionTestUtils.invokeMethod(target, "init");
        EpochMessage epochMessage = EpochMessage.newInstance();
        Block block = blockList.get(0);
        List<Transaction> transactions = new ArrayList<>(transactionList);

        target.publish(block, transactions, nodeOptList, Collections.emptyList(), CommonUtil.createTraceId());
        target.getRingBufferSize();
        target.info();
        target.getPublisherMap();
        target.register(target.getClass().getSimpleName(), target);
        target.unregister(target.getClass().getSimpleName());
        verify(target, times(1)).publish(any(), any(), any(), any(), any());
    }

}
