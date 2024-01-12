package com.turn.browser.cache;

import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.NodeItem;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.exception.NoSuchBeanException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProposalCacheTest extends AgentTestBase {

    @Mock
    private ProposalMapper proposalMapper;

    @InjectMocks
    @Spy
    private ProposalCache proposalCache;

    @Before
    public void setup() {
        List<Proposal> proposalList = new ArrayList<>();
        Proposal proposal = new Proposal();
        proposalList.add(proposal);
        proposal.setCanceledPipId("123");
        when(proposalMapper.selectByExample(any())).thenReturn(proposalList);
    }

    @Test
    public void test() throws NoSuchBeanException {
        proposalCache.init();
        proposalCache.add(4444L, "0xsfsfdsf");
        proposalCache.get(4444L);
        //assertEquals(paramProposalCache.get(444L),"0xsfsfdsf");
    }

}
