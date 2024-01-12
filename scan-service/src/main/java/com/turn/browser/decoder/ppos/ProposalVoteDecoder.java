package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.turn.browser.param.ProposalVoteParam;
import com.turn.browser.param.TxParam;

import java.math.BigInteger;

/**
 * @description: Create a validator transaction input parameter decoder
 **/
public class ProposalVoteDecoder extends AbstractPPOSDecoder {
    private ProposalVoteDecoder(){}
    public static TxParam decode(RlpList rootList) {
        // Vote for proposal
        //Vote validator
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        //Proposal ID
        String proposalID = stringResolver((RlpString) rootList.getValues().get(2));
        //voting options
        BigInteger option = bigIntegerResolver((RlpString) rootList.getValues().get(3));
        //Node code version, obtained by rpc's getProgramVersion interface
        BigInteger programVersion = bigIntegerResolver((RlpString) rootList.getValues().get(4));
        //Code version signature, obtained through the getProgramVersion interface of rpc
        String versionSign = stringResolver((RlpString) rootList.getValues().get(5));

        return ProposalVoteParam.builder()
                .verifier(nodeId)
                .proposalId(proposalID)
                .option(option.toString())
                .programVersion(programVersion.toString())
                .versionSign(versionSign)
                .build();
    }
}
