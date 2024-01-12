package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.dao.param.ppos.ProposalParameter;
import com.turn.browser.dao.param.ppos.ProposalSlash;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ProposalBusinessMapper {

    /**
     * Text proposal
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void text(BusinessParam param);

    /**
     *Upgrade proposal
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void upgrade(BusinessParam param);

    /**
     * Cancel proposal
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void cancel(BusinessParam param);

    /**
     * Vote
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void vote(BusinessParam param);

    /**
     * Parameter proposal
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void parameter(ProposalParameter businessParam);

    /**
     *Proposal data update
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void proposalSlashUpdate(@Param("proposalSlashs") List<ProposalSlash> proposalSlashs);

}