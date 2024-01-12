package com.turn.browser.analyzer.epoch;

import com.bubble.protocol.Web3j;
import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.HistoryLowRateSlash;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.client.Web3jWrapper;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.dao.custommapper.EpochBusinessMapper;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.bean.CustomStaking.StatusEnum;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.service.ppos.StakeEpochService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OnElectionAnalyzerTest extends AgentTestBase {

    @Mock
    private EpochBusinessMapper epochBusinessMapper;
    @Mock
    private NetworkStatCache networkStatCache;
    @Mock
    private SpecialApi specialApi;
    @Mock
    private TurnClient turnClient;
    @Mock
    private StakingMapper stakingMapper;
    @Mock
    private StakeEpochService stakeEpochService;
    @Mock
    private BlockChainConfig chainConfig;
    @InjectMocks
    @Spy
    private OnElectionAnalyzer target;

    @Before
    public void setup() throws Exception {
        List<Staking> list = new ArrayList<>();
        for (int i = 0; i < this.stakingList.size(); i++) {
            Staking staking = new Staking();
            staking.setNodeId(this.stakingList.get(i).getNodeId());
            staking.setStakingBlockNum(this.stakingList.get(i).getStakingBlockNum());
            staking.setStakingHes(BigDecimal.ONE);
            staking.setStatDelegateHes(BigDecimal.ONE);
            staking.setStatDelegateLocked(BigDecimal.ONE);
            staking.setStatDelegateReleased(BigDecimal.ONE);
            staking.setStakingLocked(BigDecimal.ONE);
            staking.setStakingReduction(BigDecimal.ONE);
            staking.setStatus(StatusEnum.EXITING.getCode());
            staking.setUnStakeFreezeDuration(1);
            staking.setUnStakeEndBlock(1l);
            if (i == 1) {
                staking.setStatus(StatusEnum.CANDIDATE.getCode());
            } else {
                staking.setStakingReduction(new BigDecimal("-1"));
            }
            staking.setLowRateSlashCount(0);
            list.add(staking);
        }

        List<HistoryLowRateSlash> list1 = new ArrayList<>();
        HistoryLowRateSlash historyLowRateSlash = new HistoryLowRateSlash();
        historyLowRateSlash.setNodeId("0x");
        historyLowRateSlash.setAmount(BigInteger.ZERO);
        list1.add(historyLowRateSlash);
        when(this.specialApi.getHistoryLowRateSlashList(any(), any())).thenReturn(list1);
        when(this.stakingMapper.selectByExample(any())).thenReturn(list);
        when(this.chainConfig.getConsensusPeriodBlockCount()).thenReturn(BigInteger.TEN);
        when(this.chainConfig.getSettlePeriodBlockCount()).thenReturn(BigInteger.TEN);
        when(this.chainConfig.getSlashBlockRewardCount()).thenReturn(BigDecimal.TEN);
        when(this.chainConfig.getSlashBlockRewardCount()).thenReturn(BigDecimal.TEN);
        when(this.chainConfig.getStakeThreshold()).thenReturn(BigDecimal.TEN);
        when(this.stakeEpochService.getUnStakeEndBlock(anyString(), any(BigInteger.class), anyBoolean()))
            .thenReturn(BigInteger.TEN);
        when(this.stakeEpochService.getUnStakeFreeDuration()).thenReturn(BigInteger.TEN);
        when(this.stakeEpochService.getZeroProduceFreeDuration()).thenReturn(BigInteger.TEN);
        Web3jWrapper web3jWrapper = mock(Web3jWrapper.class);
        when(this.turnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);
        Web3j web3j = mock(Web3j.class);
        when(web3jWrapper.getWeb3j()).thenReturn(web3j);
    }

    @Test
    public void convert() throws BlockNumberException {
        Block block = this.blockList.get(0);
        EpochMessage epochMessage = EpochMessage.newInstance();
        epochMessage.setPreValidatorList(this.validatorList);
        epochMessage.setSettleEpochRound(BigInteger.TEN);

        CollectionEvent collectionEvent = new CollectionEvent();
        collectionEvent.setBlock(block);
        collectionEvent.setEpochMessage(epochMessage);
        this.target.analyze(collectionEvent, block);
        when(this.stakingMapper.selectByExample(any())).thenReturn(Collections.EMPTY_LIST);
        this.target.analyze(collectionEvent, block);
        try {
            when(this.stakingMapper.selectByExample(any())).thenReturn(null);
            this.target.analyze(collectionEvent, block);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
