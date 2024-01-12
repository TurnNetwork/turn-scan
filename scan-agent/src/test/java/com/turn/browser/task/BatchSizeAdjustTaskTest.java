package com.turn.browser.task;

import com.turn.browser.AgentTestData;
import com.turn.browser.client.TurnClient;
import com.turn.browser.enums.AppStatus;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.handler.PersistenceEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigInteger;

import static org.mockito.Mockito.*;

/**
 * @description:
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class BatchSizeAdjustTaskTest extends AgentTestData {
    @Mock
    private TurnClient turnClient;
    @Mock
    private PersistenceEventHandler persistenceEventHandler;
    @InjectMocks
    @Spy
    private BatchSizeAdjustTask target;

    @Before
    public void setup() throws Exception {
        when(turnClient.getLatestBlockNumber()).thenReturn(BigInteger.TEN);
    }

    @Test
    public void test() throws IOException {
        AppStatusUtil.setStatus(AppStatus.RUNNING);

        when(persistenceEventHandler.getMaxBlockNumber()).thenReturn(BigInteger.TEN.longValue());
        target.batchSizeAdjust();
        when(persistenceEventHandler.getMaxBlockNumber()).thenReturn(100L);
        target.batchSizeAdjust();
        verify(target, times(2)).batchSizeAdjust();

        doThrow(new RuntimeException("")).when(turnClient).getLatestBlockNumber();
        target.batchSizeAdjust();
    }
}
