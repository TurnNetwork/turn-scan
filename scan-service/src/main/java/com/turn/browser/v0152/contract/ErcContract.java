package com.turn.browser.v0152.contract;

import com.bubble.protocol.core.RemoteCall;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import lombok.Data;
import org.apache.http.MethodNotSupportedException;

import java.math.BigInteger;
import java.util.List;

public interface ErcContract {
    RemoteCall<String> name();
    RemoteCall<String> symbol();
    RemoteCall<BigInteger> decimals();
    RemoteCall<BigInteger> totalSupply();
    RemoteCall<BigInteger> balanceOf(String who, BigInteger id);
    List<ErcTxEvent> getTxEvents(final TransactionReceipt transactionReceipt);
    RemoteCall<String> getTokenURI(BigInteger tokenId) throws MethodNotSupportedException;
    @Data
    class ErcTxEvent {
        private Log log;
        private String operator;
        private String from;
        private String to;
        //TokenId of erc 721 1155
        private BigInteger tokenId;
        /// Represents the transfer amount of erc20, erc1155
        private BigInteger value;
    }
}
