package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Date;

@Data
@Slf4j
@Builder
@Accessors(chain = true)
public class ProposalVote implements BusinessParam {
    //node id
    private String nodeId;
    //Proposal hash
    private String proposalHash;
    //voting options
    private int voteOption;
    //Transaction hash
    private String txHash;
    //block height
    private BigInteger bNum;
    //time
    private Date timestamp;
    //Pledge name
    private String stakingName;
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.PROPOSAL_VOTE;
    }
}
