package com.turn.browser.proxyppos.delegate;

import com.bubble.parameters.NetworkParameters;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.http.HttpService;
import com.bubble.tx.RawTransactionManager;
import com.bubble.tx.TransactionManager;
import com.turn.browser.proxyppos.ProxyContract;
import com.turn.browser.proxyppos.TestBase;

import java.math.BigInteger;

public class RewardBase extends TestBase {
    protected final String TARGET_CONTRACT_ADDRESS = NetworkParameters.getDposContractAddressOfReward();
    protected void sendRequest(byte[] d1, byte[] d2) throws Exception {
        // lax1alckh87rsx0ks2vkh4wzt3477xk3vymcwldxf4
        Web3j web3j = Web3j.build(new HttpService("http://192.168.120.145:6790"));
        TransactionManager manager = new RawTransactionManager(web3j, delegateCredentials);
        ProxyContract contract = ProxyContract.load(proxyDelegateContractAddress, web3j, manager, gasProvider, chainId);
        BigInteger contractBalance = web3j.bubbleGetBalance(proxyDelegateContractAddress, DefaultBlockParameterName.LATEST).send().getBalance();
        BigInteger delegatorBalance = web3j.bubbleGetBalance(delegateCredentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
        System.out.println("*********************");
        System.out.println("*********************");
        System.out.println("ContractBalance("+proxyDelegateContractAddress+"):"+contractBalance);
        System.out.println("OperatorBalance("+delegateCredentials.getAddress()+"):"+delegatorBalance);
        System.out.println("*********************");
        System.out.println("*********************");
        invokeProxyContract(
                contract,
                d1,TARGET_CONTRACT_ADDRESS,
                d2,TARGET_CONTRACT_ADDRESS
        );
    }
}
