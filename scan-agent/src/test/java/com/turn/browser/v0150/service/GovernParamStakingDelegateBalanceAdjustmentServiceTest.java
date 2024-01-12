package com.turn.browser.v0150.service;

import com.bubble.contracts.dpos.ProposalContract;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.resp.GovernParam;
import com.bubble.contracts.dpos.dto.resp.ParamItem;
import com.bubble.contracts.dpos.dto.resp.ParamValue;
import com.bubble.protocol.core.RemoteCall;
import com.turn.browser.AgentTestBase;
import com.turn.browser.client.TurnClient;
import com.turn.browser.dao.mapper.ConfigMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.ModifiableGovernParamEnum;
import com.turn.browser.v0150.V0150Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class GovernParamStakingDelegateBalanceAdjustmentServiceTest extends AgentTestBase {
    @Mock
    private ConfigMapper configMapper;
    @Mock
    private TurnClient turnClient;
    @Mock
    private V0150Config v0150Config;

    @Spy
    @InjectMocks
    private RestrictingMinimumReleaseParamService target;

    @Test
    public void test() throws Exception {
        Block block = blockList.get(0);

        ProposalContract proposalContract = mock(ProposalContract.class);
        when(turnClient.getProposalContract()).thenReturn(proposalContract);

        RemoteCall remoteCall0 = mock(RemoteCall.class);
        CallResponse callResponse0 = mock(CallResponse.class);
        when(proposalContract.getActiveVersion()).thenReturn(remoteCall0);
        when(remoteCall0.send()).thenReturn(callResponse0);
        when(callResponse0.getData()).thenReturn(BigInteger.TEN);
        when(v0150Config.getRestrictingMinimumReleaseActiveVersion()).thenReturn(BigInteger.TEN);

        RemoteCall remoteCall1 = mock(RemoteCall.class);
        CallResponse callResponse1 = mock(CallResponse.class);
        when(proposalContract.getParamList(any())).thenReturn(remoteCall1);
        when(remoteCall1.send()).thenReturn(callResponse1);
        GovernParam gp = new GovernParam();
        ParamItem pi = new ParamItem();
        pi.setName(ModifiableGovernParamEnum.RESTRICTING_MINIMUM_RELEASE.getName());
        pi.setModule(ModifiableGovernParamEnum.RESTRICTING_MINIMUM_RELEASE.getModule());
        pi.setDesc("");
        gp.setParamItem(pi);
        ParamValue pv = new ParamValue();
        pv.setActiveBlock("98900");
        pv.setStaleValue("100000");
        pv.setValue("100000");
        gp.setParamValue(pv);
        when(callResponse1.getData()).thenReturn(Arrays.asList(gp));

        target.checkRestrictingMinimumReleaseParam(block);


    }
}
