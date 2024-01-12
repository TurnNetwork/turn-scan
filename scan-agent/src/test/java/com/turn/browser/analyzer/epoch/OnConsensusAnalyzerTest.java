package com.turn.browser.analyzer.epoch;

import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.EpochBusinessMapper;
import com.turn.browser.dao.custommapper.SlashBusinessMapper;
import com.turn.browser.dao.entity.Slash;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.mapper.SlashMapper;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.dao.param.ppos.Report;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.service.proposal.ProposalParameterService;
import com.turn.browser.service.statistic.StatisticService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OnConsensusAnalyzerTest extends AgentTestBase {

    @Mock
    private BlockChainConfig chainConfig;

    @Mock
    private EpochBusinessMapper epochBusinessMapper;

    @Mock
    private SlashBusinessMapper slashBusinessMapper;

    @Mock
    private StakingMapper stakingMapper;

    @Mock
    private NetworkStatCache networkStatCache;

    @Mock
    private ProposalParameterService proposalParameterService;

    @Mock
    private StatisticService statisticService;

    @InjectMocks
    @Spy
    private OnConsensusAnalyzer target;

    @Mock
    private SlashMapper slashMapper;

    @Before
    public void setup() throws Exception {
        when(chainConfig.getConsensusPeriodBlockCount()).thenReturn(blockChainConfig.getConsensusPeriodBlockCount());
        List<String> nodeIdList = new ArrayList<>();
        nodeList.forEach(n -> nodeIdList.add(n.getNodeId()));
        List<Staking> stakings = new ArrayList();
        stakingList.forEach(s -> stakings.add(s));
        when(slashBusinessMapper.getException(nodeIdList)).thenReturn(stakings);

        Staking data = stakings.get(0);
        List<Report> reportList = new ArrayList();
        Report report = Report.builder().stakingBlockNum(BigInteger.TEN).slashRate(BigDecimal.valueOf(3.3)).slash2ReportRate(BigDecimal.TEN).settingEpoch(33).txHash("3r33434343").build();
        BeanUtils.copyProperties(data, report);
        reportList.add(report);
        when(stakingMapper.selectByPrimaryKey(any())).thenReturn(data);
        when(chainConfig.getDuplicateSignSlashRate()).thenReturn(BigDecimal.TEN);
        List<Slash> reportedNodeIdList =new ArrayList<>();
        when(slashMapper.selectByExampleWithBLOBs(any())).thenReturn(reportedNodeIdList);
    }

    @Test
    public void convert() {
        Block block = blockList.get(0);
        EpochMessage epochMessage = EpochMessage.newInstance();
        epochMessage.setCurValidatorList(validatorList);
        CollectionEvent collectionEvent = new CollectionEvent();
        collectionEvent.setBlock(block);
        collectionEvent.setEpochMessage(epochMessage);
        target.analyze(collectionEvent, block);
    }

}
