package com.turn.browser.task;

import cn.hutool.core.util.StrUtil;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.bean.ProposalParticipantStat;
import com.turn.browser.dao.custommapper.CustomProposalMapper;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.entity.ProposalExample;
import com.turn.browser.dao.mapper.NetworkStatMapper;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.service.proposal.ProposalService;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.TaskUtil;
import com.bubble.contracts.dpos.dto.resp.TallyResult;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Proposal information update task
 */
@Slf4j
@Component
public class ProposalInfoTask {

    @Resource
    private ProposalMapper proposalMapper;

    @Resource
    private CustomProposalMapper customProposalMapper;

    @Resource
    private ProposalService proposalService;

    @Resource
    private NetworkStatMapper networkStatMapper;

    /**
     * Proposal information update task
     * Executed every 15 seconds
     *
     * @param :
     * @return: void
     */
    @XxlJob("proposalInfoJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void proposalInfo() {
        try {
            if (AppStatusUtil.isRunning()) start();
        } catch (Exception e) {
            log.error("Abnormal update of proposal voting information", e);
            throw e;
        }
    }

    protected void start() {
        //Proposal for unfinished synchronization information of database acquisition information
        ProposalExample proposalExample = new ProposalExample();
        //For proposal information, you only need to update the status to
        //1. Voting in progress
        //2.Pre-upgrade
        //3. Passed
        List<Integer> statusList = new ArrayList<>();
        statusList.add(CustomProposal.StatusEnum.VOTING.getCode());
        statusList.add(CustomProposal.StatusEnum.PRE_UPGRADE.getCode());
        statusList.add(CustomProposal.StatusEnum.PASS.getCode());
        proposalExample.createCriteria().andStatusIn(statusList);
        List<Proposal> proposals = proposalMapper.selectByExample(proposalExample);
        //If you have already added it, there is no need to add it.
        if (proposals.isEmpty()) return;
        for (Proposal proposal : proposals) {
            try {
                List<NetworkStat> networkStat = networkStatMapper.selectByExample(null);
                //Send rpc request to query proposal results
                ProposalParticipantStat pps = proposalService.getProposalParticipantStat(proposal.getHash(), networkStat.get(0).getCurBlockHash());
                //Set the number of participants
                if (pps.getVoterCount() != null && !pps.getVoterCount().equals(proposal.getAccuVerifiers())) {
                    TaskUtil.console("The current block height [{}], the proposal end block height [{}], the total number of validators voting for the proposal [{}] [{}]->[{}] update",
                            networkStat.get(0).getCurNumber(),
                            proposal.getEndVotingBlock(),
                            proposal.getHash(),
                            proposal.getAccuVerifiers(),
                            pps.getVoterCount());
                    // There are changes
                    proposal.setAccuVerifiers(pps.getVoterCount());
                }
                /**
                 * When the synchronization block number is less than the end block and the corresponding cancellation proposal is unsuccessful, the update status will be skipped to prevent the proposal from ending prematurely during block chasing and causing data errors.
                 */
                ProposalExample pe = new ProposalExample();
                proposalExample.createCriteria().andCanceledPipIdEqualTo(proposal.getHash());
                proposalExample.createCriteria().andStatusEqualTo(CustomProposal.StatusEnum.PASS.getCode());
                List<Proposal> ppsList = proposalMapper.selectByExample(pe);
                if (networkStat.get(0).getCurNumber() < proposal.getEndVotingBlock() && ppsList.size() == 0) {
                    continue;
                }
                TallyResult tallyResult = proposalService.getTallyResult(proposal.getHash());
                if (tallyResult != null) {
                    //设置状态
                    int status = tallyResult.getStatus();
                    if (status != proposal.getStatus()) {
                        TaskUtil.console("Proposal voting [{}] status [{}]->[{}] update", proposal.getHash(), proposal.getStatus(), status);
                        // There are changes
                        proposal.setStatus(status);
                    }
                }
            } catch (Exception e) {
                XxlJobHelper.log(StrUtil.format("Proposal voting information [{}] update error: {}", proposal.getHash(), e.getMessage()));
                log.error("Proposal voting information update error", e);
            }
        }
        customProposalMapper.updateProposalInfoList(proposals);
        XxlJobHelper.handleSuccess("Proposal voting information updated successfully");
    }

}
