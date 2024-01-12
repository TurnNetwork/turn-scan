package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * txType=2002
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class ProposalParameterParam extends TxParam {
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
     * Parameter module
     */
    private String module;
    /**
     * parameter name
     */
    private String name;
    /**
     * New value of parameter
     */
    private String newValue;
    /**
     * Node name
     */
    private String nodeName;
}
