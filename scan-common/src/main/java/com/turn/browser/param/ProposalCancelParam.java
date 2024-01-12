package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ProposalCancelParam extends TxParam {

    /**
     * The verifier who submitted the proposal
     */
    private String verifier;
    public void setVerifier(String verifier){
        this.verifier= HexUtil.prefix(verifier);
    }

    /**
     * Proposal pIDID
     */
    private String pIDID;

    /**
     * Proposal voting deadline block height (EpochSize*N-20, no more than 2 weeks of block height
     */
    private BigDecimal endVotingRound;

    /**
     * Canceled target proposals
     */
    private String canceledProposalID;

    /**
     * Node name
     */
    private String nodeName;
}
