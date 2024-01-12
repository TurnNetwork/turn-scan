package com.turn.browser.analyzer.ppos;

import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.ComplementNodeOpt;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.cache.ProposalCache;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.ProposalBusinessMapper;
import com.turn.browser.dao.param.ppos.ProposalParameter;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.param.ProposalParameterParam;
import com.turn.browser.service.govern.ParameterService;
import com.turn.browser.utils.RoundCalculation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Parameter proposal business parameter converter
 **/
@Slf4j
@Service
public class ProposalParameterAnalyzer extends PPOSAnalyzer<NodeOpt> {

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private ProposalBusinessMapper proposalBusinessMapper;

    @Resource
    private ProposalCache proposalCache;

    @Resource
    private ParameterService parameterService;

    /**
     * Submit parameter proposal (create proposal)
     *
     * @param event
     * @param tx
     * @return com.turn.browser.elasticsearch.dto.NodeOpt
     */
    @Override
    public NodeOpt analyze(CollectionEvent event, Transaction tx) {
        ProposalParameterParam txParam = tx.getTxParam(ProposalParameterParam.class);
        // Supplementary node name
        updateTxInfo(txParam, tx);
        // Failed transactions do not analyze business data
        if (Transaction.StatusEnum.FAILURE.getCode() == tx.getStatus()) {
            return null;
        }

        long startTime = System.currentTimeMillis();

        BigDecimal voteEndBlockNum = RoundCalculation.getParameterProposalVoteEndBlockNum(tx.getNum(), chainConfig);
        BigDecimal activeBlockNum = voteEndBlockNum.add(BigDecimal.ONE);
        String staleValue = parameterService.getValueInBlockChainConfig(txParam.getName());
        ProposalParameter businessParam = ProposalParameter.builder()
                                                           .nodeId(txParam.getVerifier())
                                                           .pIDID(txParam.getPIDID())
                                                           .url(String.format(chainConfig.getProposalUrlTemplate(), txParam.getPIDID()))
                                                           .pipNum(String.format(chainConfig.getProposalPipNumTemplate(), txParam.getPIDID()))
                                                           .endVotingBlock(voteEndBlockNum.toBigInteger())
                                                           .activeBlock(activeBlockNum.toBigInteger())
                                                           .topic(CustomProposal.QUERY_FLAG)
                                                           .description(CustomProposal.QUERY_FLAG)
                                                           .txHash(tx.getHash())
                                                           .blockNumber(BigInteger.valueOf(tx.getNum()))
                                                           .timestamp(tx.getTime())
                                                           .stakingName(txParam.getNodeName())
                                                           .module(txParam.getModule())
                                                           .name(txParam.getName())
                                                           .staleValue(staleValue)
                                                           .newValue(txParam.getNewValue())
                                                           .build();

        // Business data storage
        proposalBusinessMapper.parameter(businessParam);

        // Add to parameter proposal cache Map<future effective block number, List<proposal ID>>
        proposalCache.add(activeBlockNum.longValue(), tx.getHash());

        String desc = NodeOpt.TypeEnum.PARAMETER.getTpl()
                                                .replace("ID", txParam.getPIDID())
                                                .replace("TITLE", businessParam.getTopic())
                                                .replace("TYPE", String.valueOf(CustomProposal.TypeEnum.PARAMETER.getCode()))
                                                .replace("MODULE", businessParam.getModule())
                                                .replace("NAME", businessParam.getName())
                                                .replace("VALUE", businessParam.getNewValue());

        NodeOpt nodeOpt = ComplementNodeOpt.newInstance();
        nodeOpt.setNodeId(txParam.getVerifier());
        nodeOpt.setType(Integer.valueOf(NodeOpt.TypeEnum.PARAMETER.getCode()));
        nodeOpt.setDesc(desc);
        nodeOpt.setTxHash(tx.getHash());
        nodeOpt.setBNum(event.getBlock().getNum());
        nodeOpt.setTime(event.getBlock().getTime());

        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);

        return nodeOpt;
    }

}
