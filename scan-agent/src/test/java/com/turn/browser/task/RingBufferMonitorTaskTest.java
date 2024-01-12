package com.turn.browser.task;

import com.turn.browser.AgentTestBase;
import com.turn.browser.AgentTestData;
import com.turn.browser.enums.AppStatus;
import com.turn.browser.utils.AppStatusUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @description:
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class RingBufferMonitorTaskTest extends AgentTestData {
    @InjectMocks
    @Spy
    private RingBufferMonitorTask target;

    @Before
    public void setup() {
    }

    @Test
    public void test() {
        AppStatusUtil.setStatus(AppStatus.RUNNING);
        target.ringBufferMonitor();
        verify(target, times(1)).ringBufferMonitor();
    }
}
