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
public class ProposalText implements BusinessParam {
    //node id
    private String nodeId;
    //pIDID replacement
    private String pIDID;
    //proposal url
    private String url;
    //PIP-pip_id
    private String pipNum;
    //voting ends soon
    private BigInteger endVotingBlock;
    //Proposal topic
    private String topic;
    //Proposal description
    private String description;
    //Transaction hash
    private String txHash;
    //block height
    private BigInteger blockNumber;
    //time
    private Date timestamp;
    //Pledge name
    private String stakingName;
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.PROPOSAL_TEXT;
    }
}
