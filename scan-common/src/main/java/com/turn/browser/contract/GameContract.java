package com.turn.browser.contract;

import com.bubble.abi.solidity.FunctionEncoder;
import com.bubble.abi.solidity.TypeReference;
import com.bubble.abi.solidity.datatypes.*;
import com.bubble.abi.solidity.datatypes.Event;
import com.bubble.abi.solidity.datatypes.generated.Uint256;
import com.bubble.abi.solidity.datatypes.generated.Uint32;
import com.bubble.abi.solidity.datatypes.generated.Uint64;
import com.bubble.abi.solidity.datatypes.generated.Uint8;
import com.bubble.crypto.Credentials;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.RemoteCall;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import com.bubble.tx.Contract;
import com.bubble.tx.TransactionManager;
import com.bubble.tx.gas.ContractGasProvider;
import lombok.Data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://github.com/Turnnetwork/client-sdk-java/releases">turn-web3j command line tools</a>,
 * or the com.turn.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/Turnnetwork/client-sdk-java/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.3.0.4.
 */
@SuppressWarnings("rawtypes")
public class GameContract extends Contract {
    public static final String BINARY = "6080604052600580546001600160a01b031690553480156200002057600080fd5b5060405162001c3138038062001c3183398101604081905262000043916200017d565b6000620000518682620002e9565b506001620000608582620002e9565b5060026200006f8482620002e9565b5060036200007e8382620002e9565b5060058054336001600160a01b031991821617909155600480549091166001600160a01b039290921691909117905550620003b592505050565b634e487b7160e01b600052604160045260246000fd5b600082601f830112620000e057600080fd5b81516001600160401b0380821115620000fd57620000fd620000b8565b604051601f8301601f19908116603f01168101908282118183101715620001285762000128620000b8565b816040528381526020925086838588010111156200014557600080fd5b600091505b838210156200016957858201830151818301840152908201906200014a565b600093810190920192909252949350505050565b600080600080600060a086880312156200019657600080fd5b85516001600160401b0380821115620001ae57600080fd5b620001bc89838a01620000ce565b96506020880151915080821115620001d357600080fd5b620001e189838a01620000ce565b95506040880151915080821115620001f857600080fd5b6200020689838a01620000ce565b945060608801519150808211156200021d57600080fd5b506200022c88828901620000ce565b608088015190935090506001600160a01b03811681146200024c57600080fd5b809150509295509295909350565b600181811c908216806200026f57607f821691505b6020821081036200029057634e487b7160e01b600052602260045260246000fd5b50919050565b601f821115620002e457600081815260208120601f850160051c81016020861015620002bf5750805b601f850160051c820191505b81811015620002e057828155600101620002cb565b5050505b505050565b81516001600160401b03811115620003055762000305620000b8565b6200031d816200031684546200025a565b8462000296565b602080601f8311600181146200035557600084156200033c5750858301515b600019600386901b1c1916600185901b178555620002e0565b600085815260208120601f198616915b82811015620003865788860151825594840194600190910190840162000365565b5085821015620003a55787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b61186c80620003c56000396000f3fe608060405234801561001057600080fd5b50600436106100b45760003560e01c8063821975a411610071578063821975a41461015757806390fde66f1461017d578063a9490cb1146101b1578063b0db5bde146101b9578063b6fc2ace146101cc578063e88cd51f146101df57600080fd5b80630b90d84c146100b95780630fcbd962146100ce57806321eff7fc146100ec57806327fc43e2146101295780632d8c2fb41461013c5780638052474d1461014f575b600080fd5b6100cc6100c73660046113ec565b6101e7565b005b6100d6610233565b6040516100e3919061146f565b60405180910390f35b61011b6100fa366004611482565b6001600160a01b031660009081526007602052604090205463ffffffff1690565b6040519081526020016100e3565b6100cc6101373660046114ab565b6102c5565b6100cc61014a3660046114ce565b61030f565b6100d661033c565b61011b6101653660046114ce565b63ffffffff1660009081526006602052604090205490565b60055461019890600160a01b900467ffffffffffffffff1681565b60405167ffffffffffffffff90911681526020016100e3565b6100d661034b565b6100cc6101c73660046113ec565b61035a565b6100cc6101da3660046114e9565b61042f565b6100d661053f565b6101f1828261035a565b63ffffffff8216600081815260066020526040808220829055517ff17f8a7ba1cc9ab696225d16f43470811b34953153618c7d120f3878383e08439190a25050565b60606003805461024290611502565b80601f016020809104026020016040519081016040528092919081815260200182805461026e90611502565b80156102bb5780601f10610290576101008083540402835291602001916102bb565b820191906000526020600020905b81548152906001019060200180831161029e57829003601f168201915b5050505050905090565b60006102d08261054e565b90507f675473dc84f2beb0bb2cbdae7a0001c5d9da2cd6b6b9d5a8dd3879c14bd80d6c8160405161030391815260200190565b60405180910390a15050565b63ffffffff81166000908152600660205260409020546004546101f19082906001600160a01b03166106b7565b60606000805461024290611502565b60606001805461024290611502565b8063ffffffff16600560148282829054906101000a900467ffffffffffffffff16610385919061154c565b825467ffffffffffffffff9182166101009390930a92830291909202199091161790555033600090815260076020526040812080548392906103ce90849063ffffffff1661156d565b92506101000a81548163ffffffff021916908363ffffffff1602179055507f7886168ffa46e156cf0640ec3c83b209f712afed53cab6970e43f8ae357c7aab828260405161030392919063ffffffff92831681529116602082015260400190565b60058054600160e01b900463ffffffff1690601c61044c8361158a565b82546101009290920a63ffffffff818102199093169183160217909155600554600160e01b900416600090815260066020908152604080832085905560045481519283019091529181526104b6925083916001600160a01b031690678ac7230489e80000906107db565b6005546040516000815233918391600160e01b90910463ffffffff16907f247bfaeea57629232924ca4ccc5f99b52c6dec09c085a4be46d326b3b5fcc0579060200160405180910390a46005546040513391600160e01b900463ffffffff16907f94a23cd53bf5ba1d2903ed9a1cd6418ae0d681ab5bbae27df124f57f70a9b62790600090a350565b60606002805461024290611502565b60408051600280825260608201909252600091829190816020015b60608152602001906001900390816105695790505090506000610595610590611f41610961565b610975565b905060006105a86105908660ff16610961565b905081836000815181106105be576105be6115c3565b602002602001018190525080836001815181106105dd576105dd6115c3565b602002602001018190525060006105f3846109e4565b905060006106088260026001609d1b01610a28565b905060006106456106408360408051808201825260008082526020918201528151808301909252825182529182019181019190915290565b610a9b565b9050600061066c8260008151811061065f5761065f6115c3565b6020026020010151610bb1565b905060006106a96106a48360408051808201825260008082526020918201528151808301909252825182529182019181019190915290565b610c2f565b9a9950505050505050505050565b60408051600380825260808201909252600091816020015b60608152602001906001900390816106cf57905050905060006106f6610590611f49610961565b9050600061070661059086610961565b9050600061071661059086610c7d565b9050828460008151811061072c5761072c6115c3565b6020026020010181905250818460018151811061074b5761074b6115c3565b6020026020010181905250808460028151811061076a5761076a6115c3565b60200260200101819052506000610780856109e4565b905060006107958260026001609d1b01610a28565b90507f59edfbeaccba98ecd40c3c030e66ff5130e12e5b290e7558c7e12e434e9a0bc96000826040516107c99291906115d9565b60405180910390a15050505050505050565b60408051600580825260c08201909252600091816020015b60608152602001906001900390816107f3579050509050600061081a610590611f46610961565b9050600061082a61059088610961565b9050600061083a61059088610c7d565b9050600061084a61059088610961565b9050600061085a61059088610975565b90508486600081518110610870576108706115c3565b6020026020010181905250838660018151811061088f5761088f6115c3565b602002602001018190525082866002815181106108ae576108ae6115c3565b602002602001018190525081866003815181106108cd576108cd6115c3565b602002602001018190525080866004815181106108ec576108ec6115c3565b60200260200101819052506000610902876109e4565b905060006109178260026001609d1b01610a28565b90507f59edfbeaccba98ecd40c3c030e66ff5130e12e5b290e7558c7e12e434e9a0bc960008260405161094b929190611615565b60405180910390a1505050505050505050505050565b606061096f61059083610cb5565b92915050565b606080825160011480156109a35750608083600081518110610999576109996115c3565b016020015160f81c105b156109af57508161096f565b6109bb83516080610de4565b836040516020016109cd929190611651565b604051602081830303815290604052905092915050565b606060006109f183610f93565b90506109ff815160c0610de4565b81604051602001610a11929190611651565b604051602081830303815290604052915050919050565b81516060906000828180846020890182895af1610a4157fe5b3d91508167ffffffffffffffff811115610a5d57610a5d6115ad565b6040519080825280601f01601f191660200182016040528015610a87576020820181803683370190505b5090503d6000602083013e95945050505050565b6060610aa6826110c7565b610aaf57600080fd5b6000610aba83611102565b905060008167ffffffffffffffff811115610ad757610ad76115ad565b604051908082528060200260200182016040528015610b1c57816020015b6040805180820190915260008082526020820152815260200190600190039081610af55790505b5090506000610b2e8560200151611187565b8560200151610b3d9190611680565b90506000805b84811015610ba657610b5483611208565b9150604051806040016040528083815260200184815250848281518110610b7d57610b7d6115c3565b6020908102919091010152610b928284611680565b925080610b9e81611693565b915050610b43565b509195945050505050565b8051606090610bbf57600080fd5b600080610bcb846112ac565b9150915060008167ffffffffffffffff811115610bea57610bea6115ad565b6040519080825280601f01601f191660200182016040528015610c14576020820181803683370190505b50905060208101610c268482856112f3565b50949350505050565b805160009015801590610c4457508151602110155b610c4d57600080fd5b600080610c59846112ac565b815191935091506020821015610c755760208290036101000a90045b949350505050565b604051606082811b6bffffffffffffffffffffffff191660208301529061096f90603401604051602081830303815290604052610975565b6060600082604051602001610ccc91815260200190565b604051602081830303815290604052905060005b6020811015610d2357818181518110610cfb57610cfb6115c3565b01602001516001600160f81b031916600003610d235780610d1b81611693565b915050610ce0565b6000610d308260206116ac565b67ffffffffffffffff811115610d4857610d486115ad565b6040519080825280601f01601f191660200182016040528015610d72576020820181803683370190505b50905060005b8151811015610c26578383610d8c81611693565b945081518110610d9e57610d9e6115c3565b602001015160f81c60f81b828281518110610dbb57610dbb6115c3565b60200101906001600160f81b031916908160001a90535080610ddc81611693565b915050610d78565b6060806038841015610e4b5760408051600180825281830190925290602082018180368337019050509050610e1983856116bf565b60f81b81600081518110610e2f57610e2f6115c3565b60200101906001600160f81b031916908160001a905350610f8c565b600060015b610e5a81876116ee565b15610e805781610e6981611693565b9250610e79905061010082611702565b9050610e50565b610e8b826001611680565b67ffffffffffffffff811115610ea357610ea36115ad565b6040519080825280601f01601f191660200182016040528015610ecd576020820181803683370190505b509250610eda85836116bf565b610ee59060376116bf565b60f81b83600081518110610efb57610efb6115c3565b60200101906001600160f81b031916908160001a905350600190505b818111610f8957610100610f2b82846116ac565b610f37906101006117fd565b610f4190886116ee565b610f4b9190611809565b60f81b838281518110610f6057610f606115c3565b60200101906001600160f81b031916908160001a90535080610f8181611693565b915050610f17565b50505b9392505050565b60608151600003610fb75760408051600080825260208201909252905b5092915050565b6000805b8351811015610ffe57838181518110610fd657610fd66115c3565b60200260200101515182610fea9190611680565b915080610ff681611693565b915050610fbb565b60008267ffffffffffffffff811115611019576110196115ad565b6040519080825280601f01601f191660200182016040528015611043576020820181803683370190505b50600092509050602081015b8551831015610c2657600086848151811061106c5761106c6115c3565b60200260200101519050600060208201905061108a83828451611376565b87858151811061109c5761109c6115c3565b602002602001015151836110b09190611680565b9250505082806110bf90611693565b93505061104f565b805160009081036110da57506000919050565b6020820151805160001a9060c08210156110f8575060009392505050565b5060019392505050565b8051600090810361111557506000919050565b6000806111258460200151611187565b84602001516111349190611680565b905060008460000151856020015161114c9190611680565b90505b8082101561117e5761116082611208565b61116a9083611680565b91508261117681611693565b93505061114f565b50909392505050565b8051600090811a60808110156111a05750600092915050565b60b88110806111bb575060c081108015906111bb575060f881105b156111c95750600192915050565b60c08110156111f6576111de600160b861181d565b6111eb9060ff16826116ac565b610f8c906001611680565b6111de600160f861181d565b50919050565b80516000908190811a60808110156112235760019150610fb0565b60b8811015611249576112376080826116ac565b611242906001611680565b9150610fb0565b60c08110156112765760b78103600185019450806020036101000a85510460018201810193505050610fb0565b60f881101561128a5761123760c0826116ac565b60019390930151602084900360f7016101000a900490920160f5190192915050565b60008060006112be8460200151611187565b905060008185602001516112d29190611680565b905060008286600001516112e691906116ac565b9196919550909350505050565b8060000361130057505050565b602081106113385782518252611317602084611680565b9250611324602083611680565b91506113316020826116ac565b9050611300565b8015611371576000600161134d8360206116ac565b611359906101006117fd565b61136391906116ac565b845184518216911916178352505b505050565b8282825b602081106113b25781518352611391602084611680565b925061139e602083611680565b91506113ab6020826116ac565b905061137a565b905182516020929092036101000a6000190180199091169116179052505050565b803563ffffffff811681146113e757600080fd5b919050565b600080604083850312156113ff57600080fd5b611408836113d3565b9150611416602084016113d3565b90509250929050565b60005b8381101561143a578181015183820152602001611422565b50506000910152565b6000815180845261145b81602086016020860161141f565b601f01601f19169290920160200192915050565b602081526000610f8c6020830184611443565b60006020828403121561149457600080fd5b81356001600160a01b0381168114610f8c57600080fd5b6000602082840312156114bd57600080fd5b813560ff81168114610f8c57600080fd5b6000602082840312156114e057600080fd5b610f8c826113d3565b6000602082840312156114fb57600080fd5b5035919050565b600181811c9082168061151657607f821691505b60208210810361120257634e487b7160e01b600052602260045260246000fd5b634e487b7160e01b600052601160045260246000fd5b67ffffffffffffffff818116838216019080821115610fb057610fb0611536565b63ffffffff818116838216019080821115610fb057610fb0611536565b600063ffffffff8083168181036115a3576115a3611536565b6001019392505050565b634e487b7160e01b600052604160045260246000fd5b634e487b7160e01b600052603260045260246000fd5b60608152600c60608201526b72656d6f746552656d6f766560a01b608082015282602082015260a060408201526000610c7560a0830184611443565b60608152600c60608201526b72656d6f74654465706c6f7960a01b608082015282602082015260a060408201526000610c7560a0830184611443565b6000835161166381846020880161141f565b83519083019061167781836020880161141f565b01949350505050565b8082018082111561096f5761096f611536565b6000600182016116a5576116a5611536565b5060010190565b8181038181111561096f5761096f611536565b60ff818116838216019081111561096f5761096f611536565b634e487b7160e01b600052601260045260246000fd5b6000826116fd576116fd6116d8565b500490565b808202811582820484141761096f5761096f611536565b600181815b8085111561175457816000190482111561173a5761173a611536565b8085161561174757918102915b93841c939080029061171e565b509250929050565b60008261176b5750600161096f565b816117785750600061096f565b816001811461178e5760028114611798576117b4565b600191505061096f565b60ff8411156117a9576117a9611536565b50506001821b61096f565b5060208310610133831016604e8410600b84101617156117d7575081810a61096f565b6117e18383611719565b80600019048211156117f5576117f5611536565b029392505050565b6000610f8c838361175c565b600082611818576118186116d8565b500690565b60ff828116828216039081111561096f5761096f61153656fea2646970667358221220cb4c03de3c125e363da9ac58dfdfa813522363859f2a7231b1b8cf23dabcc8d064736f6c63430008110033";

    public static final String FUNC_GAMETYPE = "GameType";

    public static final String FUNC_GETBUBBLEID = "GetBubbleId";

    public static final String FUNC_INTRODUCE = "Introduce";

    public static final String FUNC_NAME = "Name";

    public static final String FUNC_WEBSITE = "Website";

    public static final String FUNC_ADDIMAGE = "addImage";

    public static final String FUNC_ALLPOPULAR = "allPopular";

    public static final String FUNC_DELIMAGE = "delImage";

    public static final String FUNC_DESTROYEXECUTOR = "destroyExecutor";

    public static final String FUNC_GETBUBBLE = "getBubble";

    public static final String FUNC_GETCONTRIBUTION = "getContribution";

    public static final String FUNC_TRANSMIT = "transmit";

    public static final Event CREATE_GAME_EVENT = new Event("CreateGame",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>() {}));
    ;

    public static final Event END_GAME_EVENT = new Event("EndGame",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event GET_BUBBLE_EVENT = new Event("GetBubble",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event JOIN_GAME_EVENT = new Event("JoinGame",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event TRANSMIT_EVENT = new Event("Transmit",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));

    protected GameContract(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected GameContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<CreateGameEventResponse> getCreateGameEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CREATE_GAME_EVENT, transactionReceipt);
        ArrayList<CreateGameEventResponse> responses = new ArrayList<CreateGameEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CreateGameEventResponse typedResponse = new CreateGameEventResponse();
            typedResponse.boundId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.bubbleId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.creator = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.tokenAddress = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }
    
    public List<EndGameEventResponse> getEndGameEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(END_GAME_EVENT, transactionReceipt);
        ArrayList<EndGameEventResponse> responses = new ArrayList<EndGameEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            EndGameEventResponse typedResponse = new EndGameEventResponse();
            typedResponse.boundId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }
    
    public List<GetBubbleEventResponse> getGetBubbleEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(GET_BUBBLE_EVENT, transactionReceipt);
        ArrayList<GetBubbleEventResponse> responses = new ArrayList<GetBubbleEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            GetBubbleEventResponse typedResponse = new GetBubbleEventResponse();
            typedResponse.bubbleId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }
    
    public List<JoinGameEventResponse> getJoinGameEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(JOIN_GAME_EVENT, transactionReceipt);
        ArrayList<JoinGameEventResponse> responses = new ArrayList<JoinGameEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            JoinGameEventResponse typedResponse = new JoinGameEventResponse();
            typedResponse.boundId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.player = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }
    
    public List<TransmitEventResponse> getTransmitEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TRANSMIT_EVENT, transactionReceipt);
        ArrayList<TransmitEventResponse> responses = new ArrayList<TransmitEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransmitEventResponse typedResponse = new TransmitEventResponse();
            typedResponse.boundId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.popular = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }
    
    public RemoteCall<String> GameType() {
        final Function function = new Function(FUNC_GAMETYPE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> GetBubbleId(BigInteger imageId) {
        final Function function = new Function(FUNC_GETBUBBLEID,
                Arrays.<Type>asList(new Uint32(imageId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> Introduce() {
        final Function function = new Function(FUNC_INTRODUCE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> Name() {
        final Function function = new Function(FUNC_NAME,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> Website() {
        final Function function = new Function(FUNC_WEBSITE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> addImage(BigInteger bubbleId) {
        final Function function = new Function(
                FUNC_ADDIMAGE,
                Arrays.<Type>asList(new Uint256(bubbleId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> allPopular() {
        final Function function = new Function(FUNC_ALLPOPULAR,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint64>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> delImage(BigInteger imageId) {
        final Function function = new Function(
                FUNC_DELIMAGE,
                Arrays.<Type>asList(new Uint32(imageId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> destroyExecutor(BigInteger imageId, BigInteger popular) {
        final Function function = new Function(
                FUNC_DESTROYEXECUTOR,
                Arrays.<Type>asList(new Uint32(imageId),
                        new Uint32(popular)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> getBubble(BigInteger size) {
        final Function function = new Function(
                FUNC_GETBUBBLE,
                Arrays.<Type>asList(new Uint8(size)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getContribution(String contributor) {
        final Function function = new Function(FUNC_GETCONTRIBUTION,
                Arrays.<Type>asList(new Address(contributor)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> transmit(BigInteger imageId, BigInteger popular) {
        final Function function = new Function(
                FUNC_TRANSMIT,
                Arrays.<Type>asList(new Uint32(imageId),
                        new Uint32(popular)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static GameContract load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new GameContract(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static GameContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new GameContract(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<GameContract> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String name, String website, String introduce, String gameType, String logicContact) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(name),
                new Utf8String(website),
                new Utf8String(introduce),
                new Utf8String(gameType),
                new Address(logicContact)));
        return deployRemoteCall(GameContract.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<GameContract> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String name, String website, String introduce, String gameType, String logicContact) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(name),
                new Utf8String(website),
                new Utf8String(introduce),
                new Utf8String(gameType),
                new Address(logicContact)));
        return deployRemoteCall(GameContract.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }


    public static class CreateGameEventResponse {
        public BigInteger boundId;

        public BigInteger bubbleId;

        public String creator;

        public String tokenAddress;
    }

    public static class EndGameEventResponse {
        public BigInteger boundId;
    }

    public static class GetBubbleEventResponse {
        public BigInteger bubbleId;
    }

    public static class JoinGameEventResponse {
        public BigInteger boundId;

        public String player;
    }

    public static class TransmitEventResponse {
        public BigInteger boundId;

        public BigInteger popular;
    }

    public List<GameTxEvent> getTxEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(CREATE_GAME_EVENT, transactionReceipt);
        ArrayList<GameTxEvent> responses = new ArrayList<>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            GameTxEvent resp = new GameTxEvent();
            resp.setLog(eventValues.getLog());
            resp.setOperator("");
            resp.setFrom((String) eventValues.getIndexedValues().get(0).getValue());
            resp.setTo((String) eventValues.getIndexedValues().get(1).getValue());
            resp.setValue((BigInteger) eventValues.getNonIndexedValues().get(0).getValue());
            responses.add(resp);
        }
        return responses;
    }

    @Data
    public
    class GameTxEvent {
        private Log log;
        private String operator;
        private String from;
        private String to;
        private BigInteger bubbleId;
        private BigInteger value;
    }
}
