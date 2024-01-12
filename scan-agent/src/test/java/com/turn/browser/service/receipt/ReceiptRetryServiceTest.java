package com.turn.browser.service.receipt;

import com.turn.browser.AgentTestBase;
import com.turn.browser.client.TurnClient;
import com.turn.browser.bean.ReceiptResult;
import com.turn.browser.exception.NetworkException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class ReceiptRetryServiceTest extends AgentTestBase {
    @Mock
    private TurnClient turnClient;
    @Mock
    private ReceiptRetryService target;

    @Before
    public void setup(){
        ReflectionTestUtils.setField(target, "turnClient", turnClient);
    }

    @Test
    public void testNormal() throws Exception, NetworkException {
        when(target.getReceipt(anyLong())).thenCallRealMethod();
        ReceiptResult rr = receiptResultList.get(0);
        when(turnClient.getReceiptResult(any())).thenReturn(rr);
        target.getReceipt(1L);

        verify(target, times(1)).getReceipt(any());
    }

    @Test(expected = RuntimeException.class)
    public void getBlockException() throws Exception, NetworkException {
        when(target.getReceipt(anyLong())).thenCallRealMethod();
        when(turnClient.getReceiptResult(any())).thenThrow(new RuntimeException(""));
        target.getReceipt(1L);
    }
}
