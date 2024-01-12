package com.turn.browser.task;

import com.turn.browser.AgentTestData;
import com.turn.browser.bean.ProposalParticipantStat;
import com.turn.browser.bean.CollectionNetworkStat;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.enums.AppStatus;
import com.turn.browser.service.proposal.ProposalService;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.custommapper.CustomProposalMapper;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.ContractInvokeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

/**
 * @description:
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProposalInfoTaskTest extends AgentTestData {
    @Mock
    private NetworkStatCache networkStatCache;
    @Mock
    private ProposalMapper proposalMapper;
    @Mock
    private CustomProposalMapper customProposalMapper;
    @Mock
    private ProposalService proposalService;


    @Before
    public void setup() throws IOException {
//        ReflectionTestUtils.setField(target, "turnClient", turnClient);
//        Web3jWrapper ww = mock(Web3jWrapper.class);
//        when(turnClient.getWeb3jWrapper()).thenReturn(ww);
//        Web3j web3j = mock(Web3j.class);
//        when(ww.getWeb3j()).thenReturn(web3j);
//        Request rq = mock(Request.class);
//        when(web3j.turnBlockNumber()).thenReturn(rq);
//        TurnBlockNumber rs = mock(TurnBlockNumber.class);
//        when(rq.send()).thenReturn(rs);
//        when(rs.getBlockNumber()).thenReturn(BigInteger.TEN);
    }

    @Test
    public void test() throws ContractInvokeException, BlankResponseException {
        AppStatusUtil.setStatus(AppStatus.RUNNING);
        when(proposalMapper.selectByExample(any())).thenReturn(new ArrayList<>(proposalList));
        NetworkStat net = CollectionNetworkStat.newInstance();
        net.setCurNumber(100000L);
        when(networkStatCache.getNetworkStat()).thenReturn(net);
    }
}
