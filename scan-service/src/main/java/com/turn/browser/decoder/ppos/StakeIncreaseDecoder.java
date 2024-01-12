package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.turn.browser.param.StakeIncreaseParam;
import com.turn.browser.param.TxParam;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Increase staking transaction input parameter decoder
 **/
public class StakeIncreaseDecoder extends AbstractPPOSDecoder {
    private StakeIncreaseDecoder(){}
    public static TxParam decode(RlpList rootList) {
        // Increase staking
        //NodeId of the staking node
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        //typ indicates whether to use the free amount of the account or the locked amount of the account for pledge 0: free amount; 1: locked amount
        BigInteger type = bigIntegerResolver((RlpString) rootList.getValues().get(2));
        //Pledged AAA
        BigInteger amount =  bigIntegerResolver((RlpString) rootList.getValues().get(3));

        return StakeIncreaseParam.builder()
                .nodeId(nodeId)
                .type(type.intValue())
                .amount(new BigDecimal(amount))
                .stakingBlockNum(null)
                .build();
    }
}
