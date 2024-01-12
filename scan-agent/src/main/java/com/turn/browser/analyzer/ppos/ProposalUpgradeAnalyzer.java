package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.cache.ProposalCache;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.ProposalBusinessMapper;
import com.turn.browser.dao.param.ppos.ProposalUpgrade;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.ProposalUpgradeParam;
import com.turn.browser.utils.RoundCalculation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Upgrade proposal business parameter converter
 **/
@Slf4j
@Service
public class ProposalUpgradeAnalyzer
        extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private ProposalBusinessMapper proposalBusinessMapper;

    @Resource
    private ProposalCache proposalCache;

    /**
     * Submit an upgrade proposal (Create proposal)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        ProposalUpgradeParam txParam = tx.getTxParam(ProposalUpgradeParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus())
            return null;

        BigInteger voteNum = RoundCalculation.endBlockNumCal(tx.getNum().toString(), txParam.getEndVotingRound(), chainConfig).toBigInteger();
        long startTime = System.currentTimeMillis();
        ProposalUpgrade businessParam = ProposalUpgrade.builder()
                                                       .nodeId(txParam.getVerifier())
                                                       .pIDID(txParam.getPIDID())
                                                       .url(String.format(chainConfig.getProposalUrlTemplate(), txParam.getPIDID()))
                                                       .pipNum(String.format(chainConfig.getProposalPipNumTemplate(), txParam.getPIDID()))
                                                       .endVotingBlock(voteNum)
                                                       .activeBlock(RoundCalculation.activeBlockNumCal(new BigDecimal(voteNum), chainConfig)
                                                                                    .toBigInteger())
                                                       .topic(CustomProposal.QUERY_FLAG)
                                                       .description(CustomProposal.QUERY_FLAG)
                                                       .txHash(tx.getHash())
                                                       .blockNumber(BigInteger.valueOf(tx.getNum()))
                                                       .timestamp(tx.getTime())
                                                       .stakingName(txParam.getNodeName())
                                                       .newVersion(String.valueOf(txParam.getNewVersion()))
                                                       .build();

        proposalBusinessMapper.upgrade(businessParam);

        // Add to parameter proposal cache Map<future effective block number, List<proposal ID>>
        BigInteger activeBlockNum = businessParam.getActiveBlock();
        proposalCache.add(activeBlockNum.longValue(), tx.getHash());

        String desc = NodeOpt.TypeEnum.PROPOSALS.getTpl()
                                                .replace("ID", txParam.getPIDID())
                                                .replace("TITLE", businessParam.getTopic())
                                                .replace("TYPE", String.valueOf(CustomProposal.TypeEnum.UPGRADE.getCode()))
                                                .replace("VERSION", businessParam.getNewVersion());

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
