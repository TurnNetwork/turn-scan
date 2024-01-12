package com.turn.browser.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.turn.browser.bean.CustomVoteProposal;
import com.turn.browser.dao.entity.Vote;
import com.turn.browser.dao.entity.VoteExample;
import com.turn.browser.dao.mapper.VoteMapper;
import com.turn.browser.request.proposal.VoteListRequest;
import com.turn.browser.response.RespPage;
import com.turn.browser.response.proposal.VoteListResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoteService {

    @Resource
    private VoteMapper voteMapper;

    public RespPage<VoteListResp> queryByProposal(VoteListRequest voteListRequest) {
        Page<?> page = PageHelper.startPage(voteListRequest.getPageNo(), voteListRequest.getPageSize(), true);
        /**
         * Query hash values based on timestamp in reverse order
         */
        VoteExample voteExample = new VoteExample();
        voteExample.setOrderByClause(" timestamp desc");
        VoteExample.Criteria criteria = voteExample.createCriteria();
        criteria.andProposalHashEqualTo(voteListRequest.getProposalHash());
        if (StringUtils.isNotBlank(voteListRequest.getOption())) {
            if (CustomVoteProposal.OptionEnum.SUPPORT.getCode().equalsIgnoreCase(voteListRequest.getOption()) || CustomVoteProposal.OptionEnum.OPPOSITION.getCode()
                                                                                                                                                         .equalsIgnoreCase(voteListRequest.getOption())) {
                int option = Integer.parseInt(voteListRequest.getOption());
                criteria.andOptionEqualTo(option);
            } else {
                List<Integer> optionList = new ArrayList<>();
                optionList.add(Integer.valueOf(CustomVoteProposal.OptionEnum.SUPPORT.getCode()));
                optionList.add(Integer.valueOf(CustomVoteProposal.OptionEnum.OPPOSITION.getCode()));
                criteria.andOptionNotIn(optionList);
            }
        }
        /** Query the voting list based on the proposal hash in pagination */
        List<Vote> votes = voteMapper.selectByExample(voteExample);
        RespPage<VoteListResp> respPage = new RespPage<>();
        if (!CollectionUtils.isEmpty(votes)) {
            List<VoteListResp> voteListResps = new ArrayList<>(votes.size());
            votes.forEach(vote -> {
                /**
                 * Loop through matching data
                 */
                VoteListResp resp = new VoteListResp();
                BeanUtils.copyProperties(vote, resp);
                resp.setVoter(vote.getNodeId());
                resp.setVoterName(vote.getNodeName());
                resp.setTxHash(vote.getHash());
                resp.setTimestamp(vote.getTimestamp().getTime());
                resp.setOption(String.valueOf(vote.getOption()));
                voteListResps.add(resp);
            });
            respPage.init(voteListResps, page.getTotal(), page.getTotal(), page.getPages());
        }
        return respPage;
    }

}
