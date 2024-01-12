package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.turn.browser.param.TxParam;
import com.turn.browser.param.WithdrewStakingParam;


public class WithdrewStakingDecoder extends AbstractPPOSDecoder {

    private WithdrewStakingDecoder() {
    }

    public static TxParam decode(RlpList rootList) {
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        WithdrewStakingParam withdrewStakingParam = new WithdrewStakingParam();
        withdrewStakingParam.setNodeId(nodeId);
        return withdrewStakingParam;
    }

}
