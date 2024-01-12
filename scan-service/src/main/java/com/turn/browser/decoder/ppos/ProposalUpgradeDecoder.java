package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;
import com.turn.browser.param.ProposalUpgradeParam;
import com.turn.browser.param.TxParam;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Create a validator transaction input parameter decoder
 **/
public class ProposalUpgradeDecoder extends AbstractPPOSDecoder {
    private ProposalUpgradeDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //Submit upgrade proposal
        //The verifier who submitted the proposal
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        //pIDID
        String pIdID = stringResolver((RlpString) rootList.getValues().get(2));
        pIdID = new String(Numeric.hexStringToByteArray(pIdID));
        //updated version
        BigInteger version = bigIntegerResolver((RlpString) rootList.getValues().get(3));
        //voting cutoff block height
        BigInteger round = bigIntegerResolver((RlpString) rootList.getValues().get(4));
        //End round conversion end block height

        return ProposalUpgradeParam.builder()
                .verifier(nodeId)
                .endVotingRound(new BigDecimal(round))
                .newVersion(version.intValue())
                .pIDID(pIdID)
                .build();
    }
}
