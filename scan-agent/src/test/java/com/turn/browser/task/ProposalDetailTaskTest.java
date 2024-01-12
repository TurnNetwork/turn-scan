//package com.turn.browser.task;
//
//import com.turn.browser.AgentTestBase;
//import com.turn.browser.AgentTestData;
//import com.turn.browser.enums.AppStatus;
//import com.turn.browser.utils.AppStatusUtil;
//import com.turn.browser.dao.entity.Proposal;
//import com.turn.browser.dao.custommapper.CustomProposalMapper;
//import com.turn.browser.dao.mapper.ProposalMapper;
//import com.turn.browser.bean.CustomProposal;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.ArrayList;
//
//import static org.mockito.Mockito.*;
//
///**
// * @description:
// **/
//@RunWith(MockitoJUnitRunner.Silent.class)
//public class ProposalDetailTaskTest extends AgentTestData {
//    @Mock
//    private CustomProposalMapper customProposalMapper;
//    @Mock
//    private ProposalMapper proposalMapper;
//    @InjectMocks
//    @Spy
//    private ProposalDetailTask target;
//
//    @Before
//    public void setup() {
//
//    }
//
//    @Test
//    public void test() {
//        AppStatusUtil.setStatus(AppStatus.RUNNING);
//        when(proposalMapper.selectByExample(any())).thenReturn(new ArrayList<>(proposalList));
//        Proposal proposal = proposalList.get(0);
//        proposal.setType(CustomProposal.TypeEnum.CANCEL.getCode());
//        when(proposalMapper.selectByPrimaryKey(any())).thenReturn(proposal);
//        target.proposalDetail();
//        verify(target, times(1)).proposalDetail();
//
//        doThrow(new RuntimeException("")).when(proposalMapper).selectByPrimaryKey(any());
//        target.proposalDetail();
//    }
//}
