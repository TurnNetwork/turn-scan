package com.turn.browser.analyzer.ppos;

import com.turn.browser.cache.NodeCache;
import com.turn.browser.bean.NodeItem;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.exception.NoSuchBeanException;
import com.turn.browser.param.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * @description: Business parameter converter base class
 **/
@Slf4j
public abstract class PPOSAnalyzer<T> {

    @Resource
    protected NodeCache nodeCache;

    protected NodeItem updateNodeCache(String nodeId, String nodeName) {
        NodeItem nodeItem;
        try {
            nodeItem = nodeCache.getNode(nodeId);
            nodeItem.setNodeName(StringUtils.isBlank(nodeName) ? nodeItem.getNodeName() : nodeName);
        } catch (NoSuchBeanException e) {
            nodeItem = NodeItem.builder().nodeId(nodeId).nodeName(nodeName).build();
            nodeCache.addNode(nodeItem);
        }
        return nodeItem;
    }

    protected void updateNodeCache(String nodeId, String nodeName, BigInteger stakingBlockNum) {
        NodeItem nodeItem = updateNodeCache(nodeId, nodeName);
        nodeItem.setStakingBlockNum(stakingBlockNum);
    }

    protected int isInit(String benefitAddress) {
        return InnerContractAddrEnum.INCENTIVE_POOL_CONTRACT.getAddress().equalsIgnoreCase(benefitAddress) ?
                BusinessParam.YesNoEnum.YES.getCode()
                : BusinessParam.YesNoEnum.NO.getCode();
    }

    public abstract T analyze(CollectionEvent event, Transaction tx) throws NoSuchBeanException, BlockNumberException;


    protected void updateTxInfo(TxParam txParam, Transaction tx) {
        if (txParam == null)
            return;
        NodeItem nodeItem;
        try {
            switch (tx.getTypeEnum()) {
                case STAKE_CREATE:
                    break;
                case STAKE_MODIFY:
                    break;
                case STAKE_INCREASE:
                    StakeIncreaseParam sip = (StakeIncreaseParam) txParam;
                    nodeItem = nodeCache.getNode(sip.getNodeId());
                    sip.setNodeName(nodeItem.getNodeName())
                            .setStakingBlockNum(nodeItem.getStakingBlockNum());
                    break;
                case STAKE_EXIT:
                    StakeExitParam sep = (StakeExitParam) txParam;
                    nodeItem = nodeCache.getNode(sep.getNodeId());
                    sep.setNodeName(nodeItem.getNodeName())
                            .setStakingBlockNum(nodeItem.getStakingBlockNum());
                    break;
                case DELEGATE_CREATE:
                    DelegateCreateParam dcp = (DelegateCreateParam) txParam;
                    nodeItem = nodeCache.getNode(dcp.getNodeId());
                    dcp.setNodeName(nodeItem.getNodeName()).setStakingBlockNum(nodeItem.getStakingBlockNum());
                    break;
                case DELEGATE_EXIT:
                    DelegateExitParam dep = (DelegateExitParam) txParam;
                    nodeItem = nodeCache.getNode(dep.getNodeId());
                    dep.setNodeName(nodeItem.getNodeName());
                    break;
                case PROPOSAL_TEXT:
                    ProposalTextParam ptp = (ProposalTextParam) txParam;
                    nodeItem = nodeCache.getNode(ptp.getVerifier());
                    ptp.setNodeName(nodeItem.getNodeName());
                    break;
                case PROPOSAL_UPGRADE:
                    ProposalUpgradeParam pup = (ProposalUpgradeParam) txParam;
                    nodeItem = nodeCache.getNode(pup.getVerifier());
                    pup.setNodeName(nodeItem.getNodeName());
                    break;
                case PROPOSAL_PARAMETER:
                    ProposalParameterParam ppp = (ProposalParameterParam) txParam;
                    nodeItem = nodeCache.getNode(ppp.getVerifier());
                    ppp.setNodeName(nodeItem.getNodeName());
                    break;
                case PROPOSAL_CANCEL:
                    ProposalCancelParam pcp = (ProposalCancelParam) txParam;
                    nodeItem = nodeCache.getNode(pcp.getVerifier());
                    pcp.setNodeName(nodeItem.getNodeName());
                    break;
                case PROPOSAL_VOTE:
                    ProposalVoteParam pvp = (ProposalVoteParam) txParam;
                    nodeItem = nodeCache.getNode(pvp.getVerifier());
                    pvp.setNodeName(nodeItem.getNodeName());
                    break;
                case VERSION_DECLARE:
                    VersionDeclareParam vdp = (VersionDeclareParam) txParam;
                    nodeItem = nodeCache.getNode(vdp.getActiveNode());
                    vdp.setNodeName(nodeItem.getNodeName());
                    break;
                case REPORT:
                    ReportParam rp = (ReportParam) txParam;
                    nodeItem = nodeCache.getNode(rp.getVerify());
                    rp.setNodeName(nodeItem.getNodeName())
                            .setStakingBlockNum(nodeItem.getStakingBlockNum());
                    break;
                case RESTRICTING_CREATE:
                    break;
                case CLAIM_REWARDS:
                    // 把交易回执里的领取奖励数量设置到TxInfo,
                    DelegateRewardClaimParam drcp = (DelegateRewardClaimParam) txParam;
                    drcp.getRewardList().forEach(reward -> {
                        String nodeName = "Unknown";
                        try {
                            NodeItem node = nodeCache.getNode(reward.getNodeId());
                            nodeName = node.getNodeName();
                        } catch (NoSuchBeanException e) {
                            log.error("{}", e.getMessage());
                        }
                        reward.setNodeName(nodeName);
                    });
                    break;
                default:
                    break;
            }
            tx.setInfo(txParam.toJSONString());
        } catch (NoSuchBeanException e) {
            log.warn("The node information cannot be found in the cache, and the node name and pledge block number cannot be filled in.:{}", txParam.toJSONString());
        }
    }

}
