package com.turn.browser.service.proposal;

import com.turn.browser.bean.CustomProposal;
import com.turn.browser.bean.CustomVote.OptionEnum;
import com.turn.browser.dao.custommapper.ProposalBusinessMapper;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.entity.Vote;
import com.turn.browser.dao.entity.VoteExample;
import com.turn.browser.dao.entity.VoteExample.Criteria;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.dao.mapper.VoteMapper;
import com.turn.browser.dao.param.ppos.ProposalSlash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Update proposal parameter service after punishment
 */
@Slf4j
@Service
public class ProposalParameterService {

    @Resource
    private ProposalMapper proposalMapper;

    @Resource
    private VoteMapper voteMapper;

    @Resource
    private ProposalBusinessMapper proposalBusinessMapper;

    /**
     * Judge and process the corresponding node votes
     *
     * @return
     */
    public void setSlashParameters(String nodeId) {
        long startTime = System.currentTimeMillis();
        /**
         * Check whether there is voting data to be ended based on nodeId and not invalid votes
         */
        VoteExample voteExample = new VoteExample();
        Criteria credentials = voteExample.createCriteria();
        credentials.andNodeIdEqualTo(nodeId);
        List<Integer> options = new ArrayList<>();
        options.add(Integer.valueOf(OptionEnum.SUPPORT.getCode()));
        options.add(Integer.valueOf(OptionEnum.ABSTENTION.getCode()));
        options.add(Integer.valueOf(OptionEnum.OPPOSITION.getCode()));
        credentials.andOptionIn(options);
        List<Vote> votes = voteMapper.selectByExample(voteExample);
        /**
         * Return directly without voting
         */
        if (votes == null | votes.size() == 0) {
            log.debug("nodeId:{} not hava a vote", nodeId);
        }
        List<ProposalSlash> proposalSlashs = new ArrayList<>();
        votes.forEach(vote -> {
            /**
             * If the vote is already invalid, skip it directly
             */
            Proposal proposal = proposalMapper.selectByPrimaryKey(vote.getProposalHash());
            if (proposal.getStatus().intValue() == CustomProposal.StatusEnum.VOTING.getCode()) {
                /**
                 * If the proposal is in the voting, the voting needs to be updated as an invalid vote and the number of proposals should be reduced.
                 */
                ProposalSlash proposalSlash = new ProposalSlash();
                proposalSlash.setVoteHash(vote.getHash());
                proposalSlash.setHash(proposal.getHash());
                proposalSlash.setVoteOption(String.valueOf(vote.getOption()));
                proposalSlashs.add(proposalSlash);
            } else {
                log.debug("nodeId:{} ,proposal hash:{}, not voting", nodeId, proposal);
            }
        });
        if (!proposalSlashs.isEmpty()) {
            proposalBusinessMapper.proposalSlashUpdate(proposalSlashs);
        }
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
    }

}
