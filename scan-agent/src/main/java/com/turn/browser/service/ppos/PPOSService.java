package com.turn.browser.service.ppos;

import java.util.ArrayList;
import java.util.List;

import com.turn.browser.analyzer.ppos.*;
import org.springframework.stereotype.Service;

import com.turn.browser.cache.AddressCache;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.DelegateExitResult;
import com.turn.browser.bean.TxAnalyseResult;
import com.turn.browser.analyzer.ppos.RestrictingCreateAnalyzer;
import com.turn.browser.analyzer.ppos.ReportAnalyzer;
import com.turn.browser.analyzer.ppos.StakeCreateAnalyzer;
import com.turn.browser.analyzer.ppos.StakeExitAnalyzer;
import com.turn.browser.analyzer.ppos.StakeIncreaseAnalyzer;
import com.turn.browser.analyzer.ppos.StakeModifyAnalyzer;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.exception.NoSuchBeanException;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@Service
public class PPOSService {

    @Resource
    private StakeCreateAnalyzer stakeCreateAnalyzer;

    @Resource
    private StakeModifyAnalyzer stakeModifyAnalyzer;

    @Resource
    private StakeIncreaseAnalyzer stakeIncreaseAnalyzer;

    @Resource
    private StakeExitAnalyzer stakeExitAnalyzer;

    @Resource
    private ReportAnalyzer reportAnalyzer;

    @Resource
    private DelegateCreateAnalyzer delegateCreateAnalyzer;

    @Resource
    private DelegateExitAnalyzer delegateExitAnalyzer;

    @Resource
    private ProposalTextAnalyzer proposalTextAnalyzer;

    @Resource
    private ProposalUpgradeAnalyzer proposalUpgradeAnalyzer;

    @Resource
    private ProposalParameterAnalyzer proposalParameterAnalyzer;

    @Resource
    private ProposalCancelAnalyzer proposalCancelAnalyzer;

    @Resource
    private ProposalVoteAnalyzer proposalVoteAnalyzer;

    @Resource
    private VersionDeclareAnalyzer proposalVersionAnalyzer;

    @Resource
    private RestrictingCreateAnalyzer restrictingCreateAnalyzer;

    @Resource
    private DelegateRewardClaimAnalyzer delegateRewardClaimAnalyzer;

    @Resource
    private NetworkStatCache networkStatCache;

    @Resource
    private AddressCache addressCache;

    private long preBlockNumber = 0L;

    /**
     * Analyze transactions and construct business warehousing parameter information
     *
     * @param event
     * @return
     */
    public TxAnalyseResult analyze(CollectionEvent event) {
        long startTime = System.currentTimeMillis();

        TxAnalyseResult tar = TxAnalyseResult.builder().nodeOptList(new ArrayList<>()).delegationRewardList(new ArrayList<>()).build();

        List<Transaction> transactions = event.getTransactions();

        if (event.getBlock().getNum() == 0) {
            return tar;
        }

        // Common transactions and virtual PPOS transactions uniformly set the seq sorting sequence number: block number * 100000 + auto-increment number (allTxCount)
        int allTxCount = 0;
        for (Transaction tx : transactions) {
            // Set the transaction sequence number for ordinary transactions
            tx.setSeq(event.getBlock().getNum() * 100000 + allTxCount);
            this.addressCache.update(tx);
            // self-increasing
            allTxCount++;
            // Analyze real transactions
            this.analyzePPosTx(event, tx, tar);
            // Analyze virtual transactions
            List<Transaction> virtualTxes = tx.getVirtualTransactions();
            for (Transaction vt : virtualTxes) {
                // Set the transaction sequence number of the contract calling ppos transaction
                vt.setSeq(event.getBlock().getNum() * 100000 + allTxCount);
                switch (vt.getTypeEnum()) {
                    // If it is a proposal transaction and the transaction is triggered by an internal call of the ordinary contract, then
                    //The format of the constructed virtual transaction HASH is: <ordinary contract call hash>-<contract internal ppos transaction index>
                    // Since the bottom layer executes multiple proposals inside the contract, only one can succeed and is unique.
                    //So when storing the proposal data in the platscan database, you can remove the "-<contract internal ppos transaction index>" of the virtual proposal transaction
                    // Prevent external parties from not being able to find the corresponding transaction information when querying proposals (that is, for proposals executed through ordinary contract agents, when viewing the transaction where the proposal is located in the browser, it will jump to the ordinary contract transaction)
                    case PROPOSAL_TEXT: // 2000
                    case PROPOSAL_UPGRADE: // 2001
                    case PROPOSAL_PARAMETER: // 2002
                    case PROPOSAL_CANCEL: // 2005
                        // case PROPOSAL_VOTE: // 2003 投票提案可以同时有多笔成功(实测)
                    case VERSION_DECLARE: // 2004
                        vt.setHash(vt.getHash().split("-")[0]);
                    default:
                        break;
                }
                this.analyzePPosTx(event, vt, tar);
                // self-increasing
                allTxCount++;
            }
        }

        Block block = event.getBlock();
        // If the current block number is the same as the previous one, it proves that this is a repeatedly processed block (for example: a certain part of the business processing failed due to the retry mechanism)
        // Prevent double counting
        if (block.getNum() == this.preBlockNumber) {
            return tar;
        }
        this.networkStatCache.updateByBlock(event.getBlock());
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
        this.preBlockNumber = block.getNum();
        return tar;
    }

    /**
     * Analyze real transactions
     *
     * @param event
     * @param tx
     * @param tar
     * @return void
     */
    private void analyzePPosTx(CollectionEvent event, Transaction tx, TxAnalyseResult tar) {
        try {
            log.info("Parsing the real transaction [{}] type is enum:[{}] code:[{}] desc:[{}]", tx.getHash(), tx.getTypeEnum(), tx.getTypeEnum().getCode(), tx.getTypeEnum().getDesc());
            // Call the transaction analysis engine to analyze transactions to supplement relevant data
            NodeOpt nodeOpt = null;
            DelegationReward delegationReward = null;
            switch (tx.getTypeEnum()) {
                case STAKE_CREATE:
                    nodeOpt = this.stakeCreateAnalyzer.analyze(event, tx);
                    break;
                case STAKE_MODIFY:
                    nodeOpt = this.stakeModifyAnalyzer.analyze(event, tx);
                    break;
                case STAKE_INCREASE:
                    nodeOpt = this.stakeIncreaseAnalyzer.analyze(event, tx);
                    break;
                case STAKE_EXIT:
                    nodeOpt = this.stakeExitAnalyzer.analyze(event, tx);
                    break;
                case DELEGATE_CREATE:
                    this.delegateCreateAnalyzer.analyze(event, tx);
                    break;
                case DELEGATE_EXIT:
                    DelegateExitResult der = this.delegateExitAnalyzer.analyze(event, tx);
                    delegationReward = der.getDelegationReward();
                    break;
                case PROPOSAL_TEXT:
                    nodeOpt = this.proposalTextAnalyzer.analyze(event, tx);
                    if (Transaction.StatusEnum.SUCCESS.getCode() == tx.getStatus()) {
                        tar.setProposalQty(tar.getProposalQty() + 1);
                    }
                    break;
                case PROPOSAL_UPGRADE:
                    nodeOpt = this.proposalUpgradeAnalyzer.analyze(event, tx);
                    if (Transaction.StatusEnum.SUCCESS.getCode() == tx.getStatus()) {
                        tar.setProposalQty(tar.getProposalQty() + 1);
                    }
                    break;
                case PROPOSAL_PARAMETER:
                    nodeOpt = this.proposalParameterAnalyzer.analyze(event, tx);
                    if (Transaction.StatusEnum.SUCCESS.getCode() == tx.getStatus()) {
                        tar.setProposalQty(tar.getProposalQty() + 1);
                    }
                    break;
                case PROPOSAL_CANCEL:
                    nodeOpt = this.proposalCancelAnalyzer.analyze(event, tx);
                    if (Transaction.StatusEnum.SUCCESS.getCode() == tx.getStatus()) {
                        tar.setProposalQty(tar.getProposalQty() + 1);
                    }
                    break;
                case PROPOSAL_VOTE:
                    nodeOpt = this.proposalVoteAnalyzer.analyze(event, tx);
                    break;
                case VERSION_DECLARE:
                    nodeOpt = this.proposalVersionAnalyzer.analyze(event, tx);
                    break;
                case REPORT:
                    nodeOpt = this.reportAnalyzer.analyze(event, tx);
                    break;
                case RESTRICTING_CREATE:
                    this.restrictingCreateAnalyzer.analyze(event, tx);
                    break;
                case CLAIM_REWARDS:
                    delegationReward = this.delegateRewardClaimAnalyzer.analyze(event, tx);
                    break;
                default:
                    break;
            }
            if (nodeOpt != null) {
                tar.getNodeOptList().add(nodeOpt);
            }
            if (delegationReward != null) {
                tar.getDelegationRewardList().add(delegationReward);
            }
        } catch (BusinessException | NoSuchBeanException | BlockNumberException e) {
            log.debug("", e);
        }
    }

}