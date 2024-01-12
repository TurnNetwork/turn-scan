package com.turn.browser.bean;

import com.bubble.protocol.core.methods.response.Log;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.List;

@Data
public class Receipt {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;

    private Long blockNumber;
    private String gasUsed;
    private List<Log> logs;
    private String transactionHash;
    private String transactionIndex;
    private String status;
    private String contractAddress;
    private List<ContractInfo> contractCreated;
    private List<String> topics;

    private int logStatus;

    private String failReason;

    public int getStatus() {
        if (null == this.status)
            return SUCCESS;
        BigInteger statusQuantity = Numeric.decodeQuantity(this.status);
        return BigInteger.ONE.equals(statusQuantity) ? SUCCESS : FAILURE;
    }

    public BigInteger getGasUsed() {
        return Numeric.decodeQuantity(this.gasUsed);
    }

    /**
     * Decode log
     */
    public void decodeLogs() {
        if (this.logs == null || this.logs.isEmpty()) {
            this.logStatus = FAILURE;
            return;
        }
        Log log = this.logs.get(0);
        String data = log.getData();
        if (StringUtils.isBlank(data)) {
            this.logStatus = FAILURE;
            return;
        }
        RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(data));
        List<RlpType> rlpList = ((RlpList)(rlp.getValues().get(0))).getValues();
        String decodedStatus = new String(((RlpString)rlpList.get(0)).getBytes());
        int statusCode = Integer.parseInt(decodedStatus);
        if (statusCode == 0) {
            this.logStatus = SUCCESS;
        } else {
            this.failReason = decodedStatus;
            this.logStatus = FAILURE;
        }
    }
}