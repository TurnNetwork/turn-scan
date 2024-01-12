package com.turn.browser.v0152.service;

import com.turn.browser.AgentTestBase;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.Web3jWrapper;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.v0152.bean.ErcContractId;
import com.turn.browser.v0152.contract.ErcContract;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.core.RemoteCall;
import com.bubble.protocol.core.Request;
import com.bubble.protocol.core.Response;
import com.bubble.protocol.core.methods.request.Transaction;
import com.bubble.tx.exceptions.BubbleCallException;
import com.bubble.tx.exceptions.BubbleCallTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ErcDetectServiceTest extends AgentTestBase {

    @Mock
    private TurnClient turnClient;

    @Mock
    private Web3jWrapper web3jWrapper;

    @Mock
    private Web3j web3j;

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    private ErcContract ercContract;

    @Mock
    private RemoteCall remoteCall;

    @InjectMocks
    @Spy
    private ErcDetectService ercDetectService;

    private String contractAddress = "atp1pcqqfnl2xwyts8xlvx7n5dlxy3z634u6sldhep";

    @Test
    public void detectInputData() throws IOException {
        when(turnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);
        when(web3jWrapper.getWeb3j()).thenReturn(web3j);
        when(turnClient.getWeb3jWrapper().getWeb3j().bubbleCall(any(Transaction.class), any(DefaultBlockParameterName.class))).thenReturn(request);
        // 测试超时异常
        //when(request.send()).thenThrow(new TurnCallTimeoutException(1, "超时", response));
        // 测试业务异常
        when(request.send()).thenThrow(new BusinessException("1"));
        Assertions.assertThrows(BusinessException.class, () -> ercDetectService.isSupportErc721Enumerable(contractAddress));
    }

    @Test
    public void getContractId() throws Exception {
        when(turnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);
        when(web3jWrapper.getWeb3j()).thenReturn(web3j);
        when(turnClient.getWeb3jWrapper().getWeb3j().bubbleCall(any(Transaction.class), any(DefaultBlockParameterName.class))).thenReturn(request);
        // 测试超时异常
        //when(request.send()).thenThrow(new TurnCallTimeoutException(1, "超时", response));
        // 测试业务异常
        when(request.send()).thenThrow(new BusinessException("1"));
        //when(ercContract.name()).thenReturn(remoteCall);
        //when(remoteCall.send()).thenThrow(new TurnCallTimeoutException(1, "超时", response));
        //ercDetectService.getContractId(contractAddress);
        Assertions.assertThrows(BusinessException.class, () -> ercDetectService.getContractId(contractAddress));
    }

}
