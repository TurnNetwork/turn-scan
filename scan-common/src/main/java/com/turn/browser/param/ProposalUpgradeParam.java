package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * txType=2001
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ProposalUpgradeParam extends TxParam {

    /**
     * The verifier who submitted the proposal
     */
    private String verifier;
    public void setVerifier(String verifier){
        this.verifier= HexUtil.prefix(verifier);
    }

    /**
     * Proposal at pIDID
     */
    private String pIDID;

    /**
     * Proposal voting deadline block height (EpochSize*N-20, no more than 2 weeks of block height)
     */
    private BigDecimal endVotingRound;

    /**
     * updated version
     */
    private Integer newVersion;

    /**
     * Node name
     */
    private String nodeName;
}
