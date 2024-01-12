package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;
import com.turn.browser.param.StakeCreateParam;
import com.turn.browser.param.TxParam;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @description: Create staking transaction input parameter decoder
 *
 * return Arrays.<Type>asList(new Uint16(stakingAmountType.getValue()) 1
 *         , new BytesType(Numeric.hexStringToByteArray(benifitAddress)) 2
 *         , new BytesType(Numeric.hexStringToByteArray(nodeId)) 3
 *         , new Utf8String(externalId) 4
 *         , new Utf8String(nodeName) 5
 *         , new Utf8String(webSite) 6
 *         , new Utf8String(details) 7
 *         , new Int256(amount) 8
 *         , new Uint16(rewardPer) 9
 *         , new Uint32(processVersion.getProgramVersion()) 10
 *         , new BytesType(Numeric.hexStringToByteArray(processVersion.getProgramVersionSign())) 11
 *         , new BytesType(Numeric.hexStringToByteArray(blsPubKey)) 12
 *         , new BytesType(Numeric.hexStringToByteArray(blsProof)) 13
 **/
public class StakeCreateDecoder extends AbstractPPOSDecoder {
    private StakeCreateDecoder(){}
    public static TxParam decode(RlpList rootList) {
        //Initiate stake
        //typ indicates whether to use the free amount of the account or the locked amount of the account for pledge 0: free amount; 1: locked amount
        BigInteger type = bigIntegerResolver((RlpString) rootList.getValues().get(1));
        //The benefit account benefitAddress used to receive block rewards and staking rewards
        String address = addressResolver((RlpString) rootList.getValues().get(2));
        //NodeId of the pledged node
        String nodeId = stringResolver((RlpString) rootList.getValues().get(3));
        //ExternalId externalId
        String externalId = stringResolver((RlpString) rootList.getValues().get(4));
        externalId = new String(Numeric.hexStringToByteArray(externalId));
        //The name of the pledged node nodeName
        String nodeName = stringResolver((RlpString) rootList.getValues().get(5));
        nodeName = new String(Numeric.hexStringToByteArray(nodeName));
        //Node’s third-party homepage website
        String website = stringResolver((RlpString) rootList.getValues().get(6));
        website = new String(Numeric.hexStringToByteArray(website));
        //Node description details
        String details = stringResolver((RlpString) rootList.getValues().get(7));
        details = new String(Numeric.hexStringToByteArray(details));
        //Pledged AAA amount programVersion
        BigInteger amount = bigIntegerResolver((RlpString) rootList.getValues().get(8));
        //The real version of the program is obtained by managing rpc
        BigInteger rewardPer = bigIntegerResolver((RlpString) rootList.getValues().get(9));
        //The real version of the program is obtained by managing rpc
        BigInteger version = bigIntegerResolver((RlpString) rootList.getValues().get(10));
        return StakeCreateParam.builder()
                .type(type.intValue())
                .benefitAddress(address)
                .nodeId(nodeId)
                .externalId("0x".equals(externalId)?"":externalId)
                .nodeName(nodeName)
                .website(website)
                .details(details)
                .amount(new BigDecimal(amount))
                .programVersion(version)
                .delegateRewardPer(rewardPer.intValue()) // 必填
                .build();
    }
}
