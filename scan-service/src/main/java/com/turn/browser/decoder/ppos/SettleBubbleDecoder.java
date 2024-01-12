package com.turn.browser.decoder.ppos;

import com.bubble.contracts.dpos.dto.req.AccTokenAsset;
import com.bubble.contracts.dpos.dto.req.AccountAsset;
import com.bubble.contracts.dpos.dto.req.SettlementBubbleParam;
import com.bubble.contracts.dpos.dto.req.SettlementInfo;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;
import com.turn.browser.param.CreateBubbleParam;
import com.turn.browser.param.SettleBubbleParam;
import com.turn.browser.param.TxParam;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettleBubbleDecoder extends AbstractPPOSDecoder {

    private SettleBubbleDecoder() {
    }

    public static TxParam decode(RlpList rootList, List<Log> logs) {
        String logData = logs.get(0).getData();
        RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(logData));
        List<RlpType> rlpList = ((RlpList) (rlp.getValues().get(0))).getValues();
        String l2SettleTxHash = stringResolver(((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(0)).getBytes())
                .getValues()));
        BigInteger bubbleId = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(1)).getBytes())
                                                     .getValues()
                                                     .get(0)).asPositiveBigInteger();

        RlpString settlementInfoRlP = ((RlpString) RlpDecoder.decode(((RlpString) rlpList.get(2)).getBytes())
                .getValues());
        List<RlpType> accountAssetListRlp = RlpDecoder.decode(settlementInfoRlP.getBytes()).getValues();

        String account = stringResolver(((RlpString) RlpDecoder.decode(((RlpString) accountAssetListRlp.get(0)).getBytes())
                .getValues()));
        BigInteger nativeAmount = ((RlpString) RlpDecoder.decode(((RlpString) accountAssetListRlp.get(1)).getBytes())
                .getValues()
                .get(0)).asPositiveBigInteger();

        RlpString accTokenAssetsListRlP = ((RlpString) RlpDecoder.decode(((RlpString) accountAssetListRlp.get(2)).getBytes())
                .getValues());
        List<RlpType> accTokenAssetsList = RlpDecoder.decode(accTokenAssetsListRlP.getBytes()).getValues();

        List<AccTokenAsset> assets = new ArrayList<>(accTokenAssetsList.size());
        accTokenAssetsList.forEach(x->{
            AccTokenAsset accTokenAsset = new AccTokenAsset();
            RlpString accTokenAssetsListRlp = ((RlpString) RlpDecoder.decode(((RlpString) x).getBytes()).getValues());
            List<RlpType> accTokenAssets = RlpDecoder.decode(accTokenAssetsListRlp.getBytes()).getValues();
            String tokenAddr = stringResolver(((RlpString) RlpDecoder.decode(((RlpString) accTokenAssets.get(0)).getBytes())
                    .getValues()));
            BigInteger balance = ((RlpString) RlpDecoder.decode(((RlpString) accTokenAssets.get(1)).getBytes())
                    .getValues()
                    .get(0)).asPositiveBigInteger();
            accTokenAsset.setBalance(balance);
            accTokenAsset.setTokenAddr(tokenAddr);
            assets.add(accTokenAsset);
        });

        AccountAsset accountAsset = new AccountAsset();
        accountAsset.setAccount(account);
        accountAsset.setNativeAmount(nativeAmount);
        accountAsset.setAccTokenAssets(assets.toArray(new AccTokenAsset[assets.size()]));

        AccountAsset[] accountAssets = {accountAsset};
        SettlementInfo settlementInfo = new SettlementInfo();
        settlementInfo.setSettlementInfo(accountAssets);
        SettleBubbleParam settleBubbleParam = new SettleBubbleParam();
        settleBubbleParam.setL2SettleTxHash(l2SettleTxHash);
        settleBubbleParam.setBubbleId(bubbleId);
        settleBubbleParam.setSettlementInfo(settlementInfo);

        return settleBubbleParam;
    }

}
