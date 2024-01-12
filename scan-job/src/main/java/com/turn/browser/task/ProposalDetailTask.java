package com.turn.browser.task;

import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.CustomProposal;
import com.turn.browser.bean.ProposalMarkDownDto;
import com.turn.browser.dao.custommapper.CustomProposalMapper;
import com.turn.browser.dao.entity.Proposal;
import com.turn.browser.dao.entity.ProposalExample;
import com.turn.browser.dao.mapper.ProposalMapper;
import com.turn.browser.exception.BusinessException;
import com.turn.browser.exception.HttpRequestException;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.MarkDownParserUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;


@Slf4j
@Component
public class ProposalDetailTask {

    @Resource
    private CustomProposalMapper customProposalMapper;

    @Resource
    private ProposalMapper proposalMapper;

    /**
     * 1. Query the proposal information that has not been synchronized in the database
     * 2. Query the information on keybase based on proposalId
     * 3. Update and modify the database with the queried information
     * Executed every 15 seconds
     */
    @XxlJob("proposalDetailUpdateJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void proposalDetail() {
        // Only perform tasks when the program is running normally
        if (AppStatusUtil.isRunning()) start();
    }

    protected void start() {
        try {
            //Proposal for unfinished synchronization information of database acquisition information
            ProposalExample proposalExample = new ProposalExample();
            proposalExample.createCriteria().andCompletionFlagEqualTo(CustomProposal.FlagEnum.INCOMPLETE.getCode());
            List<Proposal> proposals = proposalMapper.selectByExample(proposalExample);
            //If it has already been added, no need to add it
            if (proposals.isEmpty()) return;

            for (Proposal proposal : proposals) {
                try {
                    ProposalMarkDownDto resp = getMarkdownInfo(proposal.getUrl());
                    proposal.setTopic(resp.getTopic());
                    proposal.setDescription(resp.getDescription());
                    if (CustomProposal.TypeEnum.CANCEL.getCode() == proposal.getType()) {
                        //Supplement relevant information corresponding to the canceled proposal
                        Proposal cp = proposalMapper.selectByPrimaryKey(proposal.getCanceledPipId());
                        proposal.setCanceledTopic(cp.getTopic());
                    }
                } catch (Exception e) {
                    log.error("Error updating proposal (proposal={}): {}", proposal.getHash(), e.getMessage());
                    continue;
                }
                //Modification status of synchronized proposal information has been completed
                proposal.setCompletionFlag(CustomProposal.FlagEnum.COMPLETE.getCode());
            }
            customProposalMapper.updateProposalDetailList(proposals);
            XxlJobHelper.handleSuccess("Update proposal details scheduled task completed");
        } catch (Exception e) {
            log.error("Exception in scheduled task of updating proposal details", e);
            throw e;
        }
    }


    /**
     * Get markdown information based on URL
     *
     * @param url
     * @return
     * @throws IOException
     * @throws BusinessException
     */
    private ProposalMarkDownDto getMarkdownInfo(String url) throws HttpRequestException {
        try {
            String fileUrl = MarkDownParserUtil.acquireMD(url);
            if (fileUrl == null) throw new BusinessException("Can't get" + url);
            String proposalMarkString = MarkDownParserUtil.parserMD(fileUrl);
            return JSON.parseObject(proposalMarkString, ProposalMarkDownDto.class);
        } catch (Exception e) {
            throw new HttpRequestException(e.getMessage());
        }
    }


}
