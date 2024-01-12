package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.turn.browser.param.RestrictingCreateParam;
import com.turn.browser.param.TxParam;

import java.util.List;

/**
 * @description: Create Restricting plan transaction input parameter decoder
 **/
public class RestrictingCreateDecoder extends AbstractPPOSDecoder {
    private RestrictingCreateDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //Create Restricting plan
        // Lock the position and release it to the account
        String account = addressResolver((RlpString) rootList.getValues().get(1));
        // List (array) of type RestrictingPlan
        List<RestrictingCreateParam.RestrictingPlan> plans = resolvePlan((RlpString) rootList.getValues().get(2));

        return RestrictingCreateParam.builder()
                .account(account)
                .plans(plans)
                .build();
    }
}
