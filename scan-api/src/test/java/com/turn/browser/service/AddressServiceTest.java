package com.turn.browser.service;


import com.github.pagehelper.Page;
import com.turn.browser.ApiTestMockBase;
import com.turn.browser.bean.CustomAddressDetail;
import com.turn.browser.bean.RestrictingBalance;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.client.Web3jWrapper;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.RpPlan;
import com.turn.browser.dao.custommapper.CustomAddressMapper;
import com.turn.browser.dao.custommapper.CustomRpPlanMapper;
import com.turn.browser.dao.mapper.RpPlanMapper;
import com.turn.browser.request.address.QueryDetailRequest;
import com.turn.browser.request.address.QueryRPPlanDetailRequest;
import com.bubble.contracts.dpos.RestrictingPlanContract;
import com.bubble.contracts.dpos.RewardContract;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.resp.RestrictingItem;
import com.bubble.contracts.dpos.dto.resp.Reward;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.RemoteCall;
import com.bubble.protocol.core.Request;
import com.bubble.protocol.core.methods.response.BubbleGetBalance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.Silent.class)
public class AddressServiceTest extends ApiTestMockBase {

    @Mock
    private RpPlanMapper rpPlanMapper;

    @Mock
    private CustomRpPlanMapper customRpPlanMapper;

    @Mock
    private TurnClient turnClient;

    @Mock
    private SpecialApi specialApi;

    @Spy
    private AddressService target;

    @Mock
    private CustomAddressMapper customAddressMapper;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        //ReflectionTestUtils.setField(target, "addressMapper", addressMapper);
        ReflectionTestUtils.setField(target, "customAddressMapper", customAddressMapper);
        ReflectionTestUtils.setField(target, "rpPlanMapper", rpPlanMapper);
        ReflectionTestUtils.setField(target, "customRpPlanMapper", customRpPlanMapper);
        ReflectionTestUtils.setField(target, "turnClient", turnClient);
        ReflectionTestUtils.setField(target, "i18n", i18n);
        ReflectionTestUtils.setField(target, "blockChainConfig", blockChainConfig);
        ReflectionTestUtils.setField(target, "esBlockRepository", ESBlockRepository);
        ReflectionTestUtils.setField(target, "specialApi", specialApi);
        ReflectionTestUtils.setField(target, "statisticCacheService", statisticCacheService);
        RestrictingPlanContract restrictingPlanContract = mock(RestrictingPlanContract.class);
        when(turnClient.getRestrictingPlanContract()).thenReturn(restrictingPlanContract);
        RestrictingItem restrictingItem = new RestrictingItem();
        restrictingItem.setBalance("0x0");
        restrictingItem.setDebt("0x0");
        restrictingItem.setPledge("0x0");
        CallResponse<RestrictingItem> baseResponse = new CallResponse<RestrictingItem>();
        baseResponse.setCode(0);
        baseResponse.setData(restrictingItem);

        RemoteCall<CallResponse<RestrictingItem>> remoteCall = mock(RemoteCall.class);
        when(restrictingPlanContract.getRestrictingInfo(anyString())).thenReturn(remoteCall);
        when(remoteCall.send()).thenReturn(baseResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getDetails() throws Exception {
        QueryDetailRequest req = new QueryDetailRequest();
        req.setAddress("0x0000000000000000000000000000000000000000");
//		assertNotNull(addressService.getDetails(req));

        CustomAddressDetail address = new CustomAddressDetail();
        address.setAddress("0x0000000000000000000000000000000000000000");
        address.setBalance(BigDecimal.ZERO);
        address.setDelegateHes(BigDecimal.ZERO);
        address.setStakingValue(BigDecimal.ZERO);
        address.setDelegateValue(BigDecimal.ZERO);
        address.setContractDestroyHash("0x");
        when(customAddressMapper.findAddressDetail(req.getAddress())).thenReturn(address);
        when(addressMapper.selectByPrimaryKey(req.getAddress())).thenReturn(address);
        List<RestrictingBalance> restrictingBalances = new ArrayList<RestrictingBalance>();
        RestrictingBalance restrictingBalance = new RestrictingBalance();
        restrictingBalance.setAccount("0x0000000000000000000000000000000000000000");
        restrictingBalance.setFreeBalance("0x10");
        restrictingBalance.setLockBalance("0x10");
        restrictingBalance.setPledgeBalance("0x10");
        restrictingBalances.add(restrictingBalance);
        when(specialApi.getRestrictingBalance(any(), any())).thenReturn(restrictingBalances);
        Web3jWrapper web3jWrapper = mock(Web3jWrapper.class);
        when(turnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);
        Web3j web3j = mock(Web3j.class);
        when(web3jWrapper.getWeb3j()).thenReturn(web3j);
        Request<?, BubbleGetBalance> rq = mock(Request.class);
        doReturn(rq).when(web3j).bubbleGetBalance(any(), any());
        BubbleGetBalance turnGetBalance = mock(BubbleGetBalance.class);
        when(rq.send()).thenReturn(turnGetBalance);

        when(turnClient.getWeb3jWrapper().getWeb3j().bubbleGetBalance(any(), any()).send().getBalance()).thenReturn(BigInteger.ONE);

        RewardContract rewardContract = mock(RewardContract.class);
        when(turnClient.getRewardContract()).thenReturn(rewardContract);

        RemoteCall<CallResponse<List<Reward>>> remoteCall = mock(RemoteCall.class);
        when(rewardContract.getDelegateReward(any(), any())).thenReturn(remoteCall);

        CallResponse<List<Reward>> callResponse = mock(CallResponse.class);
        when(remoteCall.send()).thenReturn(callResponse);
        when(turnClient.getRewardContract().getDelegateReward(any(), any()).send().getData()).thenReturn(null);
        target.getDetails(req);
        List<Reward> rewards = new ArrayList<Reward>();
        Reward reward = new Reward();
        reward.setReward("0x0");
        when(turnClient.getRewardContract().getDelegateReward(any(), any()).send().getData()).thenReturn(rewards);

        Page<RpPlan> rpPlans = new Page<RpPlan>();
        RpPlan rpPlan = new RpPlan();
        rpPlans.add(rpPlan);
        when(rpPlanMapper.selectByExample(any())).thenReturn(rpPlans);
        target.getDetails(req);
    }

    @Test
    public void rpplanDetail() throws Exception {

        List<RestrictingBalance> restrictingBalances = new ArrayList<RestrictingBalance>();
        RestrictingBalance restrictingBalance = new RestrictingBalance();
        restrictingBalance.setAccount("0x1000000000000000000000000000000000000001");
        restrictingBalance.setFreeBalance("0x10");
        restrictingBalance.setLockBalance("0x10");
        restrictingBalance.setPledgeBalance("0x10");
        restrictingBalances.add(restrictingBalance);
        when(specialApi.getRestrictingBalance(any(), any())).thenReturn(restrictingBalances);
        Web3jWrapper web3jWrapper = mock(Web3jWrapper.class);
        when(turnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);

        QueryRPPlanDetailRequest req = new QueryRPPlanDetailRequest();
        req.setPageNo(0);
        req.setPageSize(10);
        req.setAddress("0x60ceca9c1290ee56b98d4e160ef0453f7c40d219");
        Page<RpPlan> rpPlansPage = new Page<RpPlan>();
        RpPlan rpPlan = new RpPlan();
        rpPlan.setAddress("0x60ceca9c1290ee56b98d4e160ef0453f7c40d219");
        rpPlan.setAmount(BigDecimal.TEN);
        rpPlan.setEpoch(BigInteger.TEN);
        rpPlan.setNumber(10l);
        rpPlansPage.add(rpPlan);
        when(rpPlanMapper.selectByExample(any())).thenReturn(rpPlansPage);
        when(customRpPlanMapper.selectSumByAddress(any())).thenReturn(BigDecimal.TEN);
        assertNotNull(target.rpplanDetail(req));
    }

}
