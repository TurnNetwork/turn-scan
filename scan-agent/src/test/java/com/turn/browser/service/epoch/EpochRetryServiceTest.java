package com.turn.browser.service.epoch;

import com.bubble.contracts.dpos.NodeContract;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.resp.Node;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.RemoteCall;
import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.EpochInfo;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.client.Web3jWrapper;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.exception.CandidateException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EpochRetryServiceTest extends AgentTestBase {

    @Mock private BlockChainConfig chainConfig;
    @Mock TurnClient turnClient;
    @Mock
    SpecialApi specialApi;
    @Mock List<Node> curVerifiers;
    @Mock
    private NetworkStatCache networkStatCache;
    @InjectMocks
    @Spy
    private EpochRetryService target;

    @Before
    public void setup() throws Exception {
        when(chainConfig.getAddIssuePeriodBlockCount()).thenReturn(BigInteger.valueOf(250));
        when(chainConfig.getConsensusPeriodBlockCount()).thenReturn(BigInteger.valueOf(10));
        when(chainConfig.getSettlePeriodBlockCount()).thenReturn(BigInteger.valueOf(50));
        when(chainConfig.getBlockRewardRate()).thenReturn(BigDecimal.valueOf(0.6));
        when(chainConfig.getStakeRewardRate()).thenReturn(BigDecimal.valueOf(0.4));
        when(chainConfig.getSettlePeriodCountPerIssue()).thenReturn(BigInteger.valueOf(5));
        when(turnClient.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(501));

        Web3jWrapper web3jWrapper = mock(Web3jWrapper.class);
        when(turnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);
        Web3j web3j = mock(Web3j.class);
        when(web3jWrapper.getWeb3j()).thenReturn(web3j);

        when(specialApi.getHistoryValidatorList(any(),any())).thenReturn(validatorList);
        when(specialApi.getHistoryVerifierList(any(),any())).thenReturn(verifierList);

        when(curVerifiers.size()).thenReturn(4);
        when(turnClient.getLatestValidators()).thenReturn(validatorList);
        when(turnClient.getLatestVerifiers()).thenReturn(verifierList);

        NodeContract nodeContract = mock(NodeContract.class);
        when(turnClient.getNodeContract()).thenReturn(nodeContract);
        RemoteCall remoteCall = mock(RemoteCall.class);
        when(nodeContract.getCandidateList()).thenReturn(remoteCall);
        CallResponse response = mock(CallResponse.class);
        when(remoteCall.send()).thenReturn(response);
        when(response.getData()).thenReturn(verifierList);

        EpochInfo epochInfo = new EpochInfo();
        epochInfo.setAvgPackTime(BigDecimal.ONE);
        epochInfo.setPackageReward("0x99999");
        epochInfo.setRemainEpoch(BigDecimal.TEN);
        epochInfo.setStakingReward("0x333333");
        epochInfo.setYearEndNum(BigDecimal.TEN);
        epochInfo.setYearStartNum(BigDecimal.ONE);
        epochInfo.setYearNum(BigDecimal.ONE);
        when(specialApi.getEpochInfo(any(),any())).thenReturn(epochInfo);
    }

    /**
     * 测试增发周期变更
     */
    @Test
    public void issueEpochChange() throws Exception {
        target.issueChange(BigInteger.valueOf(501));
        //assertEquals(6000,target.getInciteAmount4Block().intValue());
        //assertEquals(24,target.getBlockReward().intValue());
        //assertEquals(4000,target.getInciteAmount4Stake().intValue());
        //assertEquals(800,target.getSettleStakeReward().intValue());
        verify(target, times(1)).issueChange(any(BigInteger.class));
    }

    /**
     * 测试共识周期变更
     */
    @Test
    public void consensusEpochChange() throws Exception {
        target.consensusChange(BigInteger.valueOf(41));
        verify(target, times(1)).consensusChange(any(BigInteger.class));
    }

//    /**
//     * 测试结算周期变更
//     */
//    @Test
//    public void settlementEpochChange() throws Exception {
//        target.issueChange(BigInteger.valueOf(321));
//        target.settlementChange(BigInteger.valueOf(321));
//        BigDecimal stakeReward=chainConfig.getStakeRewardRate()
//                .multiply(BigDecimal.ONE)
//                .divide(new BigDecimal(chainConfig.getSettlePeriodCountPerIssue()),0,RoundingMode.FLOOR)
//                .divide(BigDecimal.valueOf(curVerifiers.size()),0, RoundingMode.FLOOR);
//        //assertEquals(stakeReward.intValue(),target.getStakeReward().intValue());
//        verify(target, times(1)).settlementChange(any(BigInteger.class));
//    }

    /**
     * 测试取候选人列表
     */
    @Test
    public void getCandidatesNormal() throws Exception {
        NodeContract nodeContract = mock(NodeContract.class);
        when(turnClient.getNodeContract()).thenReturn(nodeContract);
        RemoteCall remoteCall = mock(RemoteCall.class);
        when(nodeContract.getCandidateList()).thenReturn(remoteCall);
        CallResponse response = mock(CallResponse.class);
        when(remoteCall.send()).thenReturn(response);
        when(response.getData()).thenReturn(verifierList);

        when(response.isStatusOk()).thenReturn(true);
        target.getCandidates();

        verify(target, times(1)).getCandidates();
    }

    /**
     * 测试取候选人列表
     */
    @Test(expected = CandidateException.class)
    public void getCandidatesException() throws Exception {
        NodeContract nodeContract = mock(NodeContract.class);
        when(turnClient.getNodeContract()).thenReturn(nodeContract);

        RemoteCall call = mock(RemoteCall.class);
        when(nodeContract.getCandidateList()).thenReturn(call);
        CallResponse response = mock(CallResponse.class);
        response.setData(candidateList);
        when(call.send()).thenReturn(response);

        when(response.isStatusOk()).thenReturn(true);
        response.setData(null);
        target.getCandidates();
    }
}
