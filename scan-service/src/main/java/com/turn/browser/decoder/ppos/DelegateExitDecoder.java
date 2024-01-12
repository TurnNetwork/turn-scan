package com.turn.browser.decoder.ppos;

import cn.hutool.core.collection.CollUtil;
import com.turn.browser.param.DelegateExitParam;
import com.turn.browser.param.TxParam;
import com.bubble.contracts.dpos.dto.common.ErrorCode;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @description: Create a validator transaction input parameter decoder
 **/
public class DelegateExitDecoder extends AbstractPPOSDecoder {

    private DelegateExitDecoder() {
    }

    public static TxParam decode(RlpList rootList, List<Log> logs) {
        DelegateExitParam delegateExitParam = new DelegateExitParam();
        //Reduce/cancel commission
        //Represents the unique identifier of a certain pledge of a certain node
        String blockNumber = stringResolver((RlpString) rootList.getValues().get(1));
        blockNumber = blockNumber.replace("0x", "");
        //NodeId of the pledged node
        String nodeId = stringResolver((RlpString) rootList.getValues().get(2));
        //Amount of holding reduction entrustment (based on the smallest unit, 1TURN = 10**18 AAA)
        BigInteger amount = bigIntegerResolver((RlpString) rootList.getValues().get(3));
        BigInteger bl = BigInteger.ZERO;
        if (StringUtils.isNotBlank(blockNumber)) {
            bl = new BigInteger(blockNumber, 16);
        }
        delegateExitParam.setNodeId(nodeId).setStakingBlockNum(bl).setAmount(new BigDecimal(amount));
        if (CollUtil.isNotEmpty(logs)) {
            String logData = logs.get(0).getData();
            RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(logData));
            List<RlpType> rlpList = ((RlpList) (rlp.getValues().get(0))).getValues();
            String decodedStatus = new String(((RlpString) rlpList.get(0)).getBytes());
            int statusCode = Integer.parseInt(decodedStatus);
            if (statusCode == ErrorCode.SUCCESS) {
                BigInteger delegateIncome = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(1)).getBytes()).getValues().get(0)).asPositiveBigInteger();
                delegateExitParam.setDelegateIncome(new BigDecimal(delegateIncome));
                if (rlpList.size() > 2) {
                    BigInteger released = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(2)).getBytes()).getValues().get(0)).asPositiveBigInteger();
                    BigInteger restrictingPlan = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(3)).getBytes()).getValues().get(0)).asPositiveBigInteger();
                    BigInteger lockReleased = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(4)).getBytes()).getValues().get(0)).asPositiveBigInteger();
                    BigInteger lockRestrictingPlan = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(5)).getBytes()).getValues().get(0)).asPositiveBigInteger();
                    delegateExitParam.setDecodedStatus(decodedStatus)
                                     .setReleased(new BigDecimal(released))
                                     .setRestrictingPlan(new BigDecimal(restrictingPlan))
                                     .setLockReleased(new BigDecimal(lockReleased))
                                     .setLockRestrictingPlan(new BigDecimal(lockRestrictingPlan));
                }
            }
        }
        return delegateExitParam;
    }

}
