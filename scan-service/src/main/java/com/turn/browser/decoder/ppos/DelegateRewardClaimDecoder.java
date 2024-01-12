package com.turn.browser.decoder.ppos;

import com.turn.browser.param.DelegateRewardClaimParam;
import com.turn.browser.param.TxParam;
import com.turn.browser.param.claim.Reward;
import com.turn.browser.utils.HexUtil;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: Receive entrustment reward transaction input parameter decoder
 **/
public class DelegateRewardClaimDecoder extends AbstractPPOSDecoder {
    private DelegateRewardClaimDecoder(){}
    public static TxParam decode(RlpList rootList,List<Log> logs) {

        String logData = logs.get(0).getData();
        RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(logData));
        List<RlpType> rlpList = ((RlpList)(rlp.getValues().get(0))).getValues();

        DelegateRewardClaimParam param = DelegateRewardClaimParam.builder()
                 .rewardList(new ArrayList<>())
                .build();
        if(rlpList.size() < 2) {
        	return param;
        }
        ((RlpList)RlpDecoder.decode(((RlpString)rlpList.get(1)).getBytes())
                .getValues()
                .get(0))
                .getValues()
                .forEach(rl -> {
                    RlpList rlpL = (RlpList)rl;

                    String nodeId = ((RlpString)rlpL.getValues().get(0)).asString();
                    BigInteger stakingNum = ((RlpString)rlpL.getValues().get(1)).asPositiveBigInteger();
                    BigInteger amount = ((RlpString)rlpL.getValues().get(2)).asPositiveBigInteger();

                    Reward reward = Reward.builder()
                            .nodeId(HexUtil.prefix(nodeId))
                            .stakingNum(stakingNum)
                            .reward(new BigDecimal(amount))
                            .build();
                    param.getRewardList().add(reward);
                });
        return param;
    }
}
