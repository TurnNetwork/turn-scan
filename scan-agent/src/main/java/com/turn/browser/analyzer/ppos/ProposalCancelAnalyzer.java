package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.ProposalBusinessMapper;
import com.turn.browser.dao.param.ppos.ProposalCancel;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.ProposalCancelParam;
import com.turn.browser.utils.RoundCalculation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * @description: Delegate business parameter converter
 **/
@Slf4j
@Service
public class ProposalCancelAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private ProposalBusinessMapper proposalBusinessMapper;

    /**
     * Submit cancellation proposal
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        ProposalCancelParam txParam = tx.getTxParam(ProposalCancelParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus())
            return null;

        long startTime = System.currentTimeMillis();

        ProposalCancel businessParam = ProposalCancel.builder()
                                                     .nodeId(txParam.getVerifier())
                                                     .pIDID(txParam.getPIDID())
                                                     .url(String.format(chainConfig.getProposalUrlTemplate(), txParam.getPIDID()))
                                                     .pipNum(String.format(chainConfig.getProposalPipNumTemplate(), txParam.getPIDID()))
                                                     .endVotingBlock(RoundCalculation.endBlockNumCal(tx.getNum().toString(),
                                                                                                     txParam.getEndVotingRound(),
                                                                                                     chainConfig).toBigInteger())
                                                     .topic(CustomProposal.QUERY_FLAG)
                                                     .description(CustomProposal.QUERY_FLAG)
                                                     .txHash(tx.getHash())
                                                     .blockNumber(BigInteger.valueOf(tx.getNum()))
                                                     .timestamp(tx.getTime())
                                                     .stakingName(txParam.getNodeName())
                                                     .canceledId(txParam.getCanceledProposalID())
                                                     .build();

        String desc = NodeOpt.TypeEnum.PROPOSALS.getTpl()
                                                .replace("ID", txParam.getPIDID())
                                                .replace("TITLE", businessParam.getTopic())
                                                .replace("TYPE", String.valueOf(CustomProposal.TypeEnum.CANCEL.getCode()))
                                                .replace("VERSION", "");

        proposalBusinessMapper.cancel(businessParam);

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
