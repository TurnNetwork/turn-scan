package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.turn.browser.param.StakeExitParam;
import com.turn.browser.param.TxParam;

/**
 * @description: Exit stake transaction input parameter decoder
 **/
public class StakeExitDecoder extends AbstractPPOSDecoder {
    private StakeExitDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //NodeId of the staking node
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        return StakeExitParam.builder()
                .nodeId(nodeId)
                .nodeName("")
                .stakingBlockNum(null)
                .build();
    }
}
