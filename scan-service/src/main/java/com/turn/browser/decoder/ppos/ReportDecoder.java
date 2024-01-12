package com.turn.browser.decoder.ppos;

import com.turn.browser.param.ReportParam;
import com.turn.browser.param.TxParam;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;

import java.math.BigInteger;

public class ReportDecoder extends AbstractPPOSDecoder {
    private ReportDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //Report double signature
        //type
        BigInteger type = bigIntegerResolver((RlpString) rootList.getValues().get(1));
        //data
        String evidence = stringResolver((RlpString) rootList.getValues().get(2));
        evidence = new String(Numeric.hexStringToByteArray(evidence));

        evidence= normalization(evidence);

        return ReportParam.builder()
                .type(type)
                .data(evidence)
                .build().init();
    }
}
