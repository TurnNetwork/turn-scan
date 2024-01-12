package com.turn.browser.proxyppos.staking;

import com.bubble.crypto.Credentials;
import com.bubble.parameters.NetworkParameters;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.http.HttpService;
import com.bubble.tx.RawTransactionManager;
import com.bubble.tx.TransactionManager;
import com.turn.browser.proxyppos.ProxyContract;
import com.turn.browser.proxyppos.TestBase;

import java.math.BigInteger;

public class StakingBase extends TestBase {
    protected final String TARGET_CONTRACT_ADDRESS = NetworkParameters.getDposContractAddressOfStaking();
    protected void sendRequest(byte[] d1,byte[] d2) throws Exception {
        BigInteger contractBalance = defaultWeb3j.bubbleGetBalance(proxyStakingContractAddress, DefaultBlockParameterName.LATEST).send().getBalance();
        BigInteger delegatorBalance = defaultWeb3j.bubbleGetBalance(defaultCredentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
        System.out.println("*********************");
        System.out.println("*********************");
        System.out.println("ContractBalance("+proxyStakingContractAddress+"):"+contractBalance);
        System.out.println("OperatorBalance("+defaultCredentials.getAddress()+"):"+delegatorBalance);
        System.out.println("*********************");
        System.out.println("*********************");

        Credentials credentials = Credentials.create("a689f0879f53710e9e0c1025af410a530d6381eebb5916773195326e123b822b");
        Web3j web3j = Web3j.build(new HttpService("http://192.168.120.145:6790"));
        TransactionManager manager = new RawTransactionManager(web3j, credentials);
        ProxyContract contract = ProxyContract.load(proxyStakingContractAddress, web3j, manager, gasProvider, chainId);
        invokeProxyContract(
                contract,
                d1,TARGET_CONTRACT_ADDRESS,
                d2,TARGET_CONTRACT_ADDRESS
        );
    }
}
