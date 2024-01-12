package com.turn.browser.dao.param.ppos;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Penalty proposal data update
 */
@Data
@Accessors(chain = true)
public class ProposalSlash {
    //Vote hash
    private String voteHash;
    //voting options
    private String voteOption;
    //Proposal hash
    private String hash;

}
