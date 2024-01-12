package com.turn.browser.handler;

import com.turn.browser.AgentTestBase;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.service.elasticsearch.EsImportService;
import com.turn.browser.service.redis.RedisImportService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
//@RunWith(MockitoJUnitRunner.Silent.class)
public class PersistenceEventHandlerTest extends AgentTestBase {

    @Mock
    private EsImportService esImportService;

    @Mock
    private RedisImportService redisImportService;

    @Mock
    private NetworkStatCache networkStatCache;

    @InjectMocks
    @Spy
    private PersistenceEventHandler target;

    @Before
    public void setup() throws Exception {

    }

}
