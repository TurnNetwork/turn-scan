package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
public class ProposalParameter implements BusinessParam {
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
    //effective block height
    private BigInteger activeBlock;
    //Proposal topic
    private String topic;
    //Proposal description
    private String description;
    //Parameter module
    private String module;
    //parameter name
    private String name;
    //Original parameter value
    private String staleValue;
    //Parameter value
    private String newValue;
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
        return BusinessType.PROPOSAL_PARAMETER;
    }
}
