package com.turn.browser.service.account;

import com.turn.browser.AgentTestBase;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.Web3jWrapper;
import com.turn.browser.exception.BusinessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.Request;
import com.bubble.protocol.core.methods.response.BubbleGetBalance;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AccountServiceTest extends AgentTestBase {
    @Mock
    private TurnClient turnClient;
    @Spy
    private AccountService target;

    @Before
    public void setup() throws Exception {
        ReflectionTestUtils.setField(target, "turnClient", turnClient);
        Web3jWrapper web3jWrapper = mock(Web3jWrapper.class);
        when(turnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);
        Web3j web3j = mock(Web3j.class);
        when(web3jWrapper.getWeb3j()).thenReturn(web3j);
        Request request = mock(Request.class);
        when(web3j.bubbleGetBalance(anyString(),any())).thenReturn(request);
        BubbleGetBalance pgb = mock(BubbleGetBalance.class);
        when(request.send()).thenReturn(pgb);
        when(pgb.getBalance()).thenReturn(BigInteger.TEN);
    }

    /**
     * 根据区块号获取激励池余额
     */
    @Test(expected = BusinessException.class)
    public void getInciteBalance() {
        BigDecimal balance = target.getInciteBalance(BigInteger.valueOf(501));
        assertEquals(BigDecimal.TEN,balance);

        doThrow(new BusinessException("")).when(turnClient).getWeb3jWrapper();
        target.getInciteBalance(BigInteger.ONE);
    }

    /**
     * 根据区块号获取锁仓池余额
     */
    @Test(expected = BusinessException.class)
    public void getLockCabinBalance() {
        BigDecimal balance = target.getLockCabinBalance(BigInteger.valueOf(501));
        assertEquals(BigDecimal.TEN,balance);

        doThrow(new BusinessException("")).when(turnClient).getWeb3jWrapper();
        target.getInciteBalance(BigInteger.ONE);
    }

    /**
     * 根据区块号获取质押池余额
     */
    @Test(expected = BusinessException.class)
    public void getStakingBalance() {
        BigDecimal balance = target.getStakingBalance(BigInteger.valueOf(501));
        assertEquals(BigDecimal.TEN,balance);

        doThrow(new BusinessException("")).when(turnClient).getWeb3jWrapper();
        target.getInciteBalance(BigInteger.ONE);
    }
}
