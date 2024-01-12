package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.turn.browser.param.DelegateCreateParam;
import com.turn.browser.param.TxParam;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Create a validator transaction input parameter decoder
 **/
public class DelegateCreateDecoder extends AbstractPPOSDecoder{
    private DelegateCreateDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //Initiate delegation
        //typ indicates whether to use the free amount of the account or the locked amount of the account for pledge 0: free amount; 1: locked amount
        BigInteger type = bigIntegerResolver((RlpString) rootList.getValues().get(1));
        //NodeId of the pledged node
        String nodeId = stringResolver((RlpString) rootList.getValues().get(2));
        //Amount of commission
        BigInteger amount = bigIntegerResolver((RlpString) rootList.getValues().get(3));

        return DelegateCreateParam.builder()
                .type(type.intValue())
                .nodeId(nodeId)
                .amount(new BigDecimal(amount))
                .nodeName("")
                .stakingBlockNum(null)
                .build();
    }
}