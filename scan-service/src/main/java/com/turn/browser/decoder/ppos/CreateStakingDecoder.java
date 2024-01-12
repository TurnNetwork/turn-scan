package com.turn.browser.decoder.ppos;

import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.utils.Numeric;
import com.turn.browser.param.CreateStakeParam;
import com.turn.browser.param.TxParam;

import java.math.BigInteger;

public class CreateStakingDecoder extends AbstractPPOSDecoder {

    private CreateStakingDecoder() {
    }

    public static TxParam decode(RlpList rootList) {
        String nodeId = stringResolver((RlpString) rootList.getValues().get(1));
        BigInteger amount = bigIntegerResolver((RlpString) rootList.getValues().get(2));
        String beneficiary = stringResolver((RlpString) rootList.getValues().get(3));
        String name = stringResolver((RlpString) rootList.getValues().get(4));
        name = new String(Numeric.hexStringToByteArray(name));
        String details = stringResolver((RlpString) rootList.getValues().get(5));
        details = new String(Numeric.hexStringToByteArray(details));
        String electronURI = stringResolver((RlpString) rootList.getValues().get(6));
        electronURI = new String(Numeric.hexStringToByteArray(electronURI));
        String rpcURI = stringResolver((RlpString) rootList.getValues().get(7));
        rpcURI = new String(Numeric.hexStringToByteArray(rpcURI));
        String p2pURI = stringResolver((RlpString) rootList.getValues().get(8));
        p2pURI = new String(Numeric.hexStringToByteArray(p2pURI));
        BigInteger versionInteger = bigIntegerResolver((RlpString) rootList.getValues().get(9));
        byte[] bytes = versionInteger.toByteArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            stringBuffer.append(bytes[i]).append(".");
        }
        String version = stringBuffer.toString();
        version = version.substring(0,version.lastIndexOf("."));

        Integer isOperator = boolResolver((RlpString) rootList.getValues().get(11))==true?1:0;
        CreateStakeParam createStakeParam = new CreateStakeParam();
        createStakeParam.setNodeId(nodeId)
                        .setAmount(amount)
                        .setBeneficiary(beneficiary)
                        .setName(name)
                        .setDetails(details)
                        .setElectronURI(electronURI)
                        .setRpcUri(rpcURI)
                        .setP2pURI(p2pURI)
                        .setVersion(version)
                        .setIsOperator(isOperator);
        return createStakeParam;
    }

}
