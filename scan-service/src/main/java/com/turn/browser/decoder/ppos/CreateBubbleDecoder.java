package com.turn.browser.decoder.ppos;

import com.bubble.protocol.core.methods.response.Log;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;
import com.turn.browser.param.CreateBubbleParam;
import com.turn.browser.param.TxParam;

import java.math.BigInteger;
import java.util.List;

public class CreateBubbleDecoder extends AbstractPPOSDecoder {

    private CreateBubbleDecoder() {
    }

    public static TxParam decode(RlpList rootList, List<Log> logs) {
        String logData = logs.get(0).getData();
        RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(logData));
        List<RlpType> rlpList = ((RlpList) (rlp.getValues().get(0))).getValues();
        BigInteger bubbleId = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(1)).getBytes())
                                                     .getValues()
                                                     .get(0)).asPositiveBigInteger();
        CreateBubbleParam createBubbleParam = new CreateBubbleParam();
        createBubbleParam.setBubbleId(bubbleId);
        return createBubbleParam;
    }

}
