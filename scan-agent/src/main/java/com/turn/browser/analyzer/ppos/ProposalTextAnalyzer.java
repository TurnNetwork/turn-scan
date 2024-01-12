package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.ProposalBusinessMapper;
import com.turn.browser.dao.param.ppos.ProposalText;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.ProposalTextParam;
import com.turn.browser.utils.RoundCalculation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * @description: Proposal business parameter converter
 **/
@Slf4j
@Service
public class ProposalTextAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private ProposalBusinessMapper proposalBusinessMapper;

    /**
     * Submit text proposal (Create proposal)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        ProposalTextParam txParam = tx.getTxParam(ProposalTextParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus())
            return null;

        long startTime = System.currentTimeMillis();

        ProposalText businessParam = ProposalText.builder()
                                                 .nodeId(txParam.getVerifier())
                                                 .pIDID(txParam.getPIDID())
                                                 .url(String.format(chainConfig.getProposalUrlTemplate(), txParam.getPIDID()))
                                                 .pipNum(String.format(chainConfig.getProposalPipNumTemplate(), txParam.getPIDID()))
                                                 .endVotingBlock(RoundCalculation.endBlockNumCal(tx.getNum().toString(),
                                                                                                 chainConfig.getProposalTextConsensusRounds(),
                                                                                                 chainConfig).toBigInteger())
                                                 .topic(CustomProposal.QUERY_FLAG)
                                                 .description(CustomProposal.QUERY_FLAG)
                                                 .txHash(tx.getHash())
                                                 .blockNumber(BigInteger.valueOf(tx.getNum()))
                                                 .timestamp(tx.getTime())
                                                 .stakingName(txParam.getNodeName())
                                                 .build();
        proposalBusinessMapper.text(businessParam);


        String desc = NodeOpt.TypeEnum.PROPOSALS.getTpl()
                                                .replace("ID", txParam.getPIDID())
                                                .replace("TITLE", businessParam.getTopic())
                                                .replace("TYPE", String.valueOf(CustomProposal.TypeEnum.TEXT.getCode()))
                                                .replace("VERSION", "");

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(txParam.getVerifier());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.PROPOSALS.getCode()));
        nodeOpt.setDesc(desc);
        nodeOpt.setTxHash(tx.getHash());
        nodeOpt.setBNum(event.getBlock().getNum());
        nodeOpt.setTime(event.getBlock().getTime());

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return nodeOpt;
    }

}
