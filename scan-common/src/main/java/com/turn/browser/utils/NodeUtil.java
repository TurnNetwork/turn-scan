package com.turn.browser.utils;

import com.bubble.crypto.ECDSASignature;
import com.bubble.crypto.Hash;
import com.bubble.crypto.Sign;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import com.bubble.rlp.solidity.RlpEncoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;
import com.bubble.utils.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeUtil {

    private NodeUtil() {
    }

    /**
     * Calculate node public key through block
     *
     * @param block
     * @return
     * @throws Exception
     */
    public static String getPublicKey(BubbleBlock.Block block) {
        String publicKey = testBlock(block).toString(16);
        // If the number is less than 128, add 0 in front.
        int lack = 128 - publicKey.length();
        if (lack <= 0) {
            return publicKey;
        }
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < lack; i++)
            prefix.append("0");
        prefix.append(publicKey);
        return prefix.toString();
    }

    public static BigInteger testBlock(BubbleBlock.Block block) {
        String extraData = block.getExtraData();
        String signature = extraData.substring(66, extraData.length());
        byte[] msgHash = getMsgHash(block);
        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        byte v = signatureBytes[64];
        byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
        return Sign.recoverFromSignature(v, new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s)), msgHash);
    }

    private static byte[] getMsgHash(BubbleBlock.Block block) {
        byte[] signData = encode(block);
        return Hash.sha3(signData);
    }

    public static byte[] encode(BubbleBlock.Block block) {
        List<RlpType> values = asRlpValues(block);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    static List<RlpType> asRlpValues(BubbleBlock.Block block) {
        List<RlpType> result = new ArrayList<>();
        //ParentHash  common.Hash    `json:"parentHash"       gencodec:"required"`
        result.add(RlpString.create(decodeHash(block.getParentHash())));
        //Coinbase    common.Address `json:"miner"            gencodec:"required"`
        result.add(RlpString.create(decodeHash(block.getMiner())));
        //Root        common.Hash    `json:"stateRoot"        gencodec:"required"`
        result.add(RlpString.create(decodeHash(block.getStateRoot())));
        //TxHash      common.Hash    `json:"transactionsRoot" gencodec:"required"`
        result.add(RlpString.create(decodeHash(block.getTransactionsRoot())));
        //ReceiptHash common.Hash    `json:"receiptsRoot"     gencodec:"required"`
        result.add(RlpString.create(decodeHash(block.getReceiptsRoot())));
        //Bloom       Bloom          `json:"logsBloom"        gencodec:"required"`
        result.add(RlpString.create(decodeHash(block.getLogsBloom())));
        //Number      *big.Int       `json:"number"           gencodec:"required"`
        result.add(RlpString.create(block.getNumber()));
        //GasLimit    uint64         `json:"gasLimit"         gencodec:"required"`
        result.add(RlpString.create(block.getGasLimit()));
        //GasUsed     uint64         `json:"gasUsed"          gencodec:"required"`
        result.add(RlpString.create(block.getGasUsed()));
        //Time        *big.Int       `json:"timestamp"        gencodec:"required"`
        result.add(RlpString.create(block.getTimestamp()));
        //Extra       []byte         `json:"extraData"        gencodec:"required"`
        result.add(RlpString.create(decodeHash(block.getExtraData().substring(0, 66))));
        //Nonce       BlockNonce     `json:"nonce"`
        result.add(RlpString.create(decodeHash(block.getNonceRaw())));
        return result;
    }

    static byte[] decodeHash(String hex) {
        return Hex.decode(Numeric.cleanHexPrefix(hex));
    }

    static byte[] decodeAddress(String address) {
        return Hex.decode(address);
    }

}
