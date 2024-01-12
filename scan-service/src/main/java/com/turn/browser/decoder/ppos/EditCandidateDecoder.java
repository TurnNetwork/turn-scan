package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;
import com.turn.browser.param.CreateStakeParam;
import com.turn.browser.param.EditCandidateParam;
import com.turn.browser.param.TxParam;

import java.math.BigInteger;

public class EditCandidateDecoder extends AbstractPPOSDecoder {

    private EditCandidateDecoder() {
    }

    public static TxParam decode(RlpList rootList) {
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        String beneficiary = stringResolver((RlpString) rootList.getValues().get(2));
        String name = stringResolver((RlpString) rootList.getValues().get(3));
        name = new String(Numeric.hexStringToByteArray(name));
        String details = stringResolver((RlpString) rootList.getValues().get(4));
        details = new String(Numeric.hexStringToByteArray(details));
        String rpcUri = stringResolver((RlpString) rootList.getValues().get(5));
        rpcUri = new String(Numeric.hexStringToByteArray(rpcUri));
        EditCandidateParam createStakeParam = new EditCandidateParam();
        createStakeParam.setNodeId(nodeId)
                        .setBeneficiary(beneficiary)
                        .setName(name)
                        .setDetails(details)
                        .setRpcUri(rpcUri);
        return createStakeParam;
    }

}
