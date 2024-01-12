package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * txType=2003
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ProposalVoteParam extends TxParam{
    /**
     * Validator of voting
     */
    private String verifier;
    public void setVerifier(String verifier){
        this.verifier= HexUtil.prefix(verifier);
    }
    /**
     * Proposal ID
     */
    private String proposalId;

    /**
     * voting options
     * 0x01: Support
     * 0x02: Objection
     * 0x03: Objection
     */
    private String option;

    /**
     * Node code version
     */
    private String programVersion;

    /**
     * Code version signing
     */
    private String versionSign;

    /**
     * pidid of the canceled proposal
     */
    private String pIDID;

    /**
     * Type of proposal that was canceled
     */
    private String proposalType;

    /**
     * The name of the validator node that voted
     */
    private String nodeName;

    /**
     *Proposal URL
     */
    private String url;
}
