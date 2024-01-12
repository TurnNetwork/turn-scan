package com.turn.browser.analyzer.epoch;

import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.dao.mapper.GasEstimateLogMapper;
import com.turn.browser.publisher.GasEstimateEventPublisher;
import com.turn.browser.dao.custommapper.EpochBusinessMapper;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomGasEstimateLogMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.elasticsearch.dto.Block;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OnSettleAnalyzerTest extends AgentTestBase {

    @Mock
    private BlockChainConfig chainConfig;

    @Mock
    private EpochBusinessMapper epochBusinessMapper;

    @Mock
    private StakingMapper stakingMapper;

    @Mock
    private GasEstimateEventPublisher gasEstimateEventPublisher;

    @Mock
    private CustomGasEstimateLogMapper customGasEstimateLogMapper;

    @InjectMocks
    @Spy
    private OnSettleAnalyzer target;

    @Mock
    private GasEstimateLogMapper gasEstimateLogMapper;

    @Before
    public void setup() throws Exception {
        when(chainConfig.getUnStakeRefundSettlePeriodCount()).thenReturn(blockChainConfig.getUnStakeRefundSettlePeriodCount());
        when(chainConfig.getMaxSettlePeriodCount4AnnualizedRateStat()).thenReturn(blockChainConfig.getMaxSettlePeriodCount4AnnualizedRateStat());
        when(chainConfig.getSettlePeriodCountPerIssue()).thenReturn(blockChainConfig.getSettlePeriodCountPerIssue());
        when(stakingMapper.selectByExampleWithBLOBs(any())).thenReturn(new ArrayList<>(stakingList));
        when(gasEstimateLogMapper.deleteByPrimaryKey(any())).thenReturn(1);
    }

    @Test
    public void convert() throws IOException {
        Block block = blockList.get(0);
        EpochMessage epochMessage = EpochMessage.newInstance();
        epochMessage.setCurValidatorList(validatorList);
        epochMessage.setPreValidatorList(validatorList);
        epochMessage.setCurVerifierList(verifierList);
        epochMessage.setPreVerifierList(verifierList);
        epochMessage.setStakeReward(new BigDecimal("10000"));
        epochMessage.setSettleEpochRound(BigInteger.TEN);
        CollectionEvent collectionEvent = new CollectionEvent();
        collectionEvent.setBlock(block);
        collectionEvent.setEpochMessage(epochMessage);
        target.analyze(collectionEvent, block);
    }


}
