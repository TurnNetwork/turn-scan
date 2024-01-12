package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;
import com.turn.browser.param.StakeModifyParam;
import com.turn.browser.param.TxParam;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

import static com.turn.browser.decoder.ppos.AbstractPPOSDecoder.*;

/**
 * @description: Modify the staking transaction input parameter decoder
 */
public class StakeModifyDecoder extends AbstractPPOSDecoder {
    private StakeModifyDecoder(){}
    public static TxParam decode(RlpList rootList) {
        // Modify staking information
        //Income account used to receive block rewards and staking rewards
        String benefitAddress = addressResolver((RlpString) rootList.getValues().get(1));
        //NodeId of the staking node
        String nodeId = stringResolver((RlpString) rootList.getValues().get(2));
        //External ID
        BigInteger rewardPer = bigIntegerResolver((RlpString) rootList.getValues().get(3));
        //ExternalId externalId
        String externalId = stringResolver((RlpString) rootList.getValues().get(4));
        externalId = externalId==null?null:new String(Numeric.hexStringToByteArray(externalId));
        //The name of the pledged node
        String nodeName = stringResolver((RlpString) rootList.getValues().get(5));
        nodeName = nodeName==null?null:new String(Numeric.hexStringToByteArray(nodeName));
        //The third-party home page of the node
        String website = stringResolver((RlpString) rootList.getValues().get(6));
        website = website==null?null:new String(Numeric.hexStringToByteArray(website));
        //Node description
        String detail = stringResolver((RlpString) rootList.getValues().get(7));
        detail = detail==null?null:new String(Numeric.hexStringToByteArray(detail));

        String externalId2 = externalId;
        if(StringUtils.isBlank(externalId)) externalId2 = null;
        if("0x".equals(externalId)) externalId2 = "";

        return StakeModifyParam.builder()
                .nodeId(nodeId)
                .benefitAddress(StringUtils.isBlank(benefitAddress)?null:benefitAddress) // Optional
                .externalId(externalId2) // Optional
                .nodeName(StringUtils.isBlank(nodeName)?null:nodeName) // Optional
                .website(StringUtils.isBlank(website)?null:website) // Optional
                .details(StringUtils.isBlank(detail)?null:detail) // Optional
                .delegateRewardPer(rewardPer==null?null:rewardPer.intValue()) // Optional
                .build();
    }
}
