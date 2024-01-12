package com.turn.browser.dao.custommapper;//package com.turn.browser.dao.mapper.mapper_old;

import com.turn.browser.dao.entity.Proposal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomProposalMapper {
    int updateProposalDetailList( @Param("proposalList") List <Proposal> list );
    int updateProposalInfoList( @Param("proposalList") List <Proposal> list );
    
    List<Proposal> selectVotingProposal(String nodeId);
}
