package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;
import com.turn.browser.param.ProposalParameterParam;
import com.turn.browser.param.TxParam;

/**
 * @description: parameter proposal transaction input parameter decoder
 **/
public class ProposalParameterDecoder extends AbstractPPOSDecoder {
    private ProposalParameterDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //Submit upgrade proposal
        //The verifier who submitted the proposal
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        //pIDID
        String pIdID = stringResolver((RlpString) rootList.getValues().get(2));
        pIdID = new String(Numeric.hexStringToByteArray(pIdID));
        //Parameter module
        String module = stringResolver((RlpString) rootList.getValues().get(3));
        module = new String(Numeric.hexStringToByteArray(module));
        //parameter name
        String name = stringResolver((RlpString) rootList.getValues().get(4));
        name = new String(Numeric.hexStringToByteArray(name));
        //Parameter value
        String newValue = stringResolver((RlpString) rootList.getValues().get(5));
        newValue =  new String(Numeric.hexStringToByteArray(newValue));
        return ProposalParameterParam.builder()
                .verifier(nodeId)
                .pIDID(pIdID)
                .module(module)
                .name(name)
                .newValue(newValue)
                .build();
    }
}
