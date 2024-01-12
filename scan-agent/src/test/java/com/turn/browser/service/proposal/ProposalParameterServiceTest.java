package com.turn.browser.service.proposal;

import com.turn.browser.AgentTestBase;
import com.turn.browser.dao.custommapper.ProposalBusinessMapper;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.entity.Vote;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.dao.mapper.VoteMapper;
import com.turn.browser.bean.CustomProposal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProposalParameterServiceTest extends AgentTestBase {
    @Mock
    private ProposalMapper proposalMapper;
    @Mock
	private VoteMapper voteMapper;
    @Mock
	private ProposalBusinessMapper proposalBusinessMapper;
	@InjectMocks
    @Spy
    private ProposalParameterService target;

    @Before
    public void setup() throws Exception {

    }

    @Test
    public void test() throws Exception {
    	
    	when(voteMapper.selectByExample(any())).thenReturn(Collections.emptyList());
    	target.setSlashParameters("0x1");
    	
    	List<Vote> votes = new ArrayList<Vote>();
    	Vote vote = new Vote();
    	vote.setNodeId("0x1");
    	vote.setProposalHash("0x123124");
    	vote.setOption(1);
    	vote.setHash("0x");
    	votes.add(vote);
    	when(voteMapper.selectByExample(any())).thenReturn(votes);
    	Proposal proposal = new Proposal();
    	proposal.setHash("0x");
    	proposal.setStatus(CustomProposal.StatusEnum.CANCEL.getCode());
    	when(proposalMapper.selectByPrimaryKey(any())).thenReturn(proposal);
    	target.setSlashParameters("0x1");
    	proposal.setStatus(CustomProposal.StatusEnum.VOTING.getCode());
    	when(proposalMapper.selectByPrimaryKey(any())).thenReturn(proposal);
    	target.setSlashParameters("0x1");
    }
}
