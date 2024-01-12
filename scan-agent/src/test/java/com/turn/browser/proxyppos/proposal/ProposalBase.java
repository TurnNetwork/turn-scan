package com.turn.browser.proxyppos.proposal;

import com.bubble.contracts.dpos.abi.Function;
import com.bubble.contracts.dpos.dto.resp.Proposal;
import com.bubble.contracts.dpos.utils.EncoderUtils;
import com.bubble.crypto.Credentials;
import com.bubble.parameters.NetworkParameters;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.http.HttpService;
import com.bubble.tx.RawTransactionManager;
import com.bubble.tx.TransactionManager;
import com.turn.browser.proxyppos.ProxyContract;
import com.turn.browser.proxyppos.TestBase;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

public class ProposalBase extends TestBase {
    protected String nodeId1 = "77fffc999d9f9403b65009f1eb27bae65774e2d8ea36f7b20a89f82642a5067557430e6edfe5320bb81c3666a19cf4a5172d6533117d7ebcd0f2c82055499050";
    protected String nodeId2 = "411a6c3640b6cd13799e7d4ed286c95104e3a31fbb05d7ae0004463db648f26e93f7f5848ee9795fb4bbb5f83985afd63f750dc4cf48f53b0e84d26d6834c20c";

    protected final String TARGET_CONTRACT_ADDRESS = NetworkParameters.getDposContractAddressOfProposal();

    protected byte[] encode(Proposal p){
        Function f = new Function(p.getSubmitFunctionType(),p.getSubmitInputParameters());
        byte [] d = Hex.decode(EncoderUtils.functionEncoder(f));
        return d;
    }

    protected void sendRequest(byte[] d1, byte[] d2) throws Exception {
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
