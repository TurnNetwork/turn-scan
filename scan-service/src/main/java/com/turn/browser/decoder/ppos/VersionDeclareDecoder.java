package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.turn.browser.param.TxParam;
import com.turn.browser.param.VersionDeclareParam;

import java.math.BigInteger;

/**
 * @description: 创建验证人交易输入参数解码器
 **/
public class VersionDeclareDecoder extends AbstractPPOSDecoder {
    private VersionDeclareDecoder(){}
    public static TxParam decode(RlpList rootList) {
        // 版本声明
        //声明的节点，只能是验证人/候选人
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        //声明的版本，有rpc的getProgramVersion接口获取
        BigInteger version =  bigIntegerResolver((RlpString) rootList.getValues().get(2));
        //声明的版本签名，有rpc的getProgramVersion接口获取
        String versionSign = stringResolver((RlpString) rootList.getValues().get(3));
        return VersionDeclareParam.builder()
                .activeNode(nodeId)
                .version(version.intValue())
                .versionSigns(versionSign)
                .build();
    }
}
