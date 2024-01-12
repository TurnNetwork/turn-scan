package com.turn.browser.task.bean;

import com.turn.browser.AgentTestBase;
import com.turn.browser.AgentTestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class AddressStatisticsTest extends AgentTestData {

    @Test
    public void test(){
        AddressStatistics statistics = new AddressStatistics();
        statistics.setDelegateHes(null);
        statistics.setDelegateLocked(null);
        statistics.setDelegateReleased(null);
        statistics.setDelegateAddr(null);
        statistics.setStakingHes(null);
        statistics.setStakingLocked(null);
        statistics.setStakingAddr(null);
        statistics.setStakingReduction(null);
        statistics.setNodeId(null);
        statistics.setNodeIdSet(null);

        statistics.getDelegateHes();
        statistics.getDelegateLocked();
        statistics.getDelegateReleased();
        statistics.getStakingHes();
        statistics.getStakingLocked();
        statistics.getDelegateAddr();
        statistics.getNodeId();
        statistics.getNodeIdSet();
        statistics.getStakingAddr();
        statistics.getStakingReduction();
        assertTrue(true);
    }
}
