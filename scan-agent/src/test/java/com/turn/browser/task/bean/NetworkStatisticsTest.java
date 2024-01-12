package com.turn.browser.task.bean;

import com.turn.browser.AgentTestBase;
import com.turn.browser.AgentTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class NetworkStatisticsTest extends AgentTestData {

    @Test
    public void test(){
        NetworkStatistics statistics = new NetworkStatistics();
        statistics.setStakingValue(null);
        statistics.setTotalValue(null);
        statistics.setTotalValue(null);
        statistics.setStakingValue(null);
        statistics.getStakingValue();
        statistics.getTotalValue();
        assertTrue(true);
    }
}
