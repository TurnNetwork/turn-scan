package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;
import com.turn.browser.param.ProposalCancelParam;
import com.turn.browser.param.TxParam;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Create a validator transaction input parameter decoder
 **/
public class ProposalCancelDecoder extends AbstractPPOSDecoder {
    private ProposalCancelDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //Submit cancellation proposal
        //The verifier who submitted the proposal
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        //pIDID of this proposal
        String pIdID = stringResolver((RlpString) rootList.getValues().get(2));
        pIdID = new String(Numeric.hexStringToByteArray(pIdID));
        //voting cutoff block height
        BigInteger round = bigIntegerResolver((RlpString) rootList.getValues().get(3));
        //Cancelled pIDID
        String cancelPidID = stringResolver((RlpString) rootList.getValues().get(4));

        return ProposalCancelParam.builder()
                .verifier(nodeId)
                .pIDID(pIdID)
                .endVotingRound(new BigDecimal(round))
                .canceledProposalID(cancelPidID)
                .build();
    }
}
