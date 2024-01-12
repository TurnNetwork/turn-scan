package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * tyType=2000
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ProposalTextParam extends TxParam {

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
     * Node name
     */
    private String nodeName;
}
