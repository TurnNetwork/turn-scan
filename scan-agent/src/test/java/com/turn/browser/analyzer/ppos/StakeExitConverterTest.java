package com.turn.browser.analyzer.ppos;

import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.cache.NodeCache;
import com.turn.browser.bean.NodeItem;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.dao.custommapper.StakeBusinessMapper;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.service.ppos.StakeEpochService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * @Description: 退出验证人(退出质押)转化器测试类
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class StakeExitConverterTest extends AgentTestBase {

    @Mock
    private StakeBusinessMapper stakeBusinessMapper;
    @Mock
    private NodeCache nodeCache;
    @Mock
    private NetworkStatCache networkStatCache;
    @Mock
    private StakingMapper stakingMapper;
    @Mock
    private BlockChainConfig chainConfig;
    @Mock
    private StakeEpochService stakeEpochService;
    @InjectMocks
    @Spy
    private StakeExitAnalyzer target;

    @Before
    public void setup()throws Exception{
        NodeItem nodeItem = NodeItem.builder()
                .nodeId("0xbfc9d6578bab4e510755575e47b7d137fcf0ad0bcf10ed4d023640dfb41b197b9f0d8014e47ecbe4d51f15db514009cbda109ebcf0b7afe06600d6d423bb7fbf")
                .nodeName("zrj-node1")
                .stakingBlockNum(new BigInteger("20483"))
                .build();

        when(stakingMapper.selectByPrimaryKey(any())).thenReturn(stakingList.get(0));
        when(nodeCache.getNode(any())).thenReturn(nodeItem);
        when(chainConfig.getSettlePeriodBlockCount()).thenReturn(BigInteger.valueOf(400));
        when(chainConfig.getUnStakeRefundSettlePeriodCount()).thenReturn(BigInteger.valueOf(400));
        when(stakeEpochService.getUnStakeEndBlock(anyString(),any(BigInteger.class),anyBoolean())).thenReturn(BigInteger.TEN);
        when(stakeEpochService.getUnStakeFreeDuration()).thenReturn(BigInteger.TEN);
    }


    @Test
    public void convert() throws BlockNumberException {
        Transaction tx = new Transaction();
        for (Transaction transaction : transactionList){
            if(transaction.getTypeEnum().equals(Transaction.TypeEnum.STAKE_EXIT))
                tx=transaction;
        }
        EpochMessage epochMessage = EpochMessage.newInstance();
        epochMessage.setSettleEpochRound(new BigInteger("13"));
        CollectionEvent collectionEvent = new CollectionEvent();
        collectionEvent.setEpochMessage(epochMessage);
        target.analyze(collectionEvent,tx);
    }
}