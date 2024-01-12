package com.turn.browser.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSONUtil;
import com.bubble.abi.solidity.EventEncoder;
import com.bubble.crypto.Credentials;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.methods.response.BubbleGetTransactionReceipt;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import com.bubble.protocol.http.HttpService;
import com.bubble.tx.gas.DefaultGasProvider;
import com.turn.browser.contract.TexasHoldem;
import org.junit.jupiter.api.Test;


import java.util.List;

public class Bubtest {

    public static void main(String[] args) {
        try {
            Web3j web3j = Web3j.build(new HttpService("http://192.168.31.117:7789"));
            BubbleGetTransactionReceipt send = web3j.bubbleGetTransactionReceipt(
                    "0xf847b70c2c9b69cc74ea497bc8107f3ecbf522c8a44364cfad96bcc87756bf3a").send();
            TransactionReceipt transactionReceipt = send.getTransactionReceipt().get();
            Console.log("====={}", JSONUtil.toJsonStr(transactionReceipt));
            Credentials credentials = Credentials.create(
                    "a43a4bfa7bf527ebf86e177ac1486e4976dd3d09fcdeef38395105bfe56c733c");
            TexasHoldem texasHoldem = TexasHoldem.load("0x7B058933362024fdFc695A442AeA26fC58D16fEB",
                                                web3j,
                                                credentials,
                                                new DefaultGasProvider());
//            List<TexasHoldem.JoinGameEventResponse> startRoundEvents =  ReflectUtil.invoke(texasHoldem, "getJoinGameEvents", transactionReceipt);
            List<TexasHoldem.JoinGameEventResponse> startRoundEvents = texasHoldem.getJoinGameEvents(transactionReceipt);
            Console.log("====={}", JSONUtil.toJsonStr(startRoundEvents));
//            Console.log("====={}", EventEncoder.encode(TexasHoldem.JOINGAME_EVENT));
        } catch (Exception e) {
            Console.log("异常{}", e);
        }
    }



}
