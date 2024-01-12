package com.turn.browser.analyzer.ppos;

import cn.hutool.core.util.StrUtil;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.dao.custommapper.ProposalBusinessMapper;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.dao.param.ppos.ProposalVote;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.ProposalVoteParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;

/**
 * @description: Proposal business parameter converter
 **/
@Slf4j
@Service
public class ProposalVoteAnalyzer extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private ProposalBusinessMapper proposalBusinessMapper;

    @Resource
    private ProposalMapper proposalMapper;

    /**
     * Vote for proposals (proposal voting)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        ProposalVoteParam txParam = tx.getTxParam(ProposalVoteParam.class);

        String proposalId = txParam.getProposalId();
        // Query voting proposal information
        Proposal proposal = null;
        try {
            proposal = proposalMapper.selectByPrimaryKey(proposalId);
            txParam.setPIDID(proposal.getPipId());
            txParam.setProposalType(String.valueOf(proposal.getType()));
        } catch (Exception e) {
            if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) {
                log.warn(StrUtil.format("Transaction [{}] failed, query proposal [{}] exception", tx.getHash(), proposalId), e);
            } else {
                log.error(StrUtil.format("Transaction [{}] is successful, query proposal [{}] is abnormal", tx.getHash(), proposalId), e);
                throw e;
            }
        }

        // Supplementary node name
        updateTxInfo(txParam, tx);

        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) {
            return null;
        }

        long startTime = System.currentTimeMillis();

        // Get parameters
        String nodeName = txParam.getNodeName();
        String txHash = tx.getHash();
        Long blockNum = event.getBlock().getNum();
        Date time = tx.getTime();

        // voting record
        ProposalVote businessParam = ProposalVote.builder()
                                                 .nodeId(txParam.getVerifier())
                                                 .txHash(txHash)
                                                 .bNum(BigInteger.valueOf(blockNum))
                                                 .timestamp(time)
                                                 .stakingName(nodeName)
                                                 .proposalHash(txParam.getProposalId())
                                                 .voteOption(Integer.valueOf(txParam.getOption()))
                                                 .build();

        proposalBusinessMapper.vote(businessParam);

        String desc = NodeOpt.TypeEnum.VOTE.getTpl()
                                           .replace("ID", proposal.getPipId())
                                           .replace("TITLE", proposal.getTopic())
                                           .replace("OPTION", txParam.getOption())
                                           .replace("TYPE", String.valueOf(proposal.getType()))
                                           .replace("VERSION", proposal.getNewVersion() == null ? "" : proposal.getNewVersion());

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(txParam.getVerifier());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.VOTE.getCode()));
        nodeOpt.setDesc(desc);
        nodeOpt.setTxHash(txHash);
        nodeOpt.setBNum(blockNum);
        nodeOpt.setTime(time);

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return nodeOpt;
    }

}
