package com.turn.browser.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.methods.response.BubbleGetTransactionReceipt;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import com.bubble.tx.gas.DefaultGasProvider;
import com.turn.browser.bean.*;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.cache.GameContractCache;
import com.turn.browser.cache.PPosInvokeContractInputCache;
import com.turn.browser.cache.TexasHoldemCache;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.contract.TexasHoldem;
import com.turn.browser.decoder.PPOSTxDecodeResult;
import com.turn.browser.decoder.PPOSTxDecodeUtil;
import com.turn.browser.decoder.ppos.AbstractPPOSDecoder;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.enums.ContractDescEnum;
import com.turn.browser.enums.ContractTypeEnum;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.exception.BeanCreateOrUpdateException;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.ContractInvokeException;
import com.turn.browser.param.DelegateExitParam;
import com.turn.browser.param.DelegateRewardClaimParam;
import com.turn.browser.param.TxParam;
import com.bubble.contracts.dpos.dto.common.ErrorCode;
import com.bubble.protocol.core.DefaultBlockParameter;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.protocol.core.methods.response.BubbleGetCode;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;
import com.turn.browser.contract.GameContract;
import com.turn.browser.v0152.analyzer.MicroNodeAnalyzer;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * virtual trading tools
 */
public class TransactionUtil {

    /**
     * vrf inner contract
     */
    private static final String VRF_ADDRESS = "0x3000000000000000000000000000000000000001";

    private static final String CREATE_BUBBLE_TOPIC = "0x41d67600a097ed7727e5bd0998b066339106a5df476ed1b9930da13aa8a4e63d";
    /**
     * Generate a virtual PPOS transaction list based on the input information for calling PPOS within the contract
     *
     * @param block The block information where the contract calls the transaction
     * @param parentTx The contract calls the transaction itself
     * @param invokeContractInput Input information for calling PPOS operation inside the contract
     * @return
     */
    public static List<Transaction> getVirtualTxList(Block block,
                                                     Transaction parentTx,
                                                     PPosInvokeContractInput invokeContractInput) {
        List<Transaction> transactionList = new ArrayList<>();
        if (invokeContractInput == null) {
            return transactionList;
        }
        // If it is a VRF inner contract, skip it
        if (invokeContractInput.getTo().equalsIgnoreCase(VRF_ADDRESS)) {
            return transactionList;
        }
        List<TransData> trans = invokeContractInput.getTransDatas();
        for (int i = 0; i < trans.size(); i++) {
            TransData tran = trans.get(i);
            PPOSTxDecodeResult result = PPOSTxDecodeUtil.decode(tran.getInput(), Collections.emptyList());
            if (result.getTypeEnum() == null) {
                continue;
            }
            Transaction tx = new Transaction();
            BeanUtils.copyProperties(parentTx, tx);
            tx.setStatus(parentTx.getStatus());
            tx.setFrom(invokeContractInput.getFrom());
            tx.setTo(invokeContractInput.getTo());
            tx.setToType(Transaction.ToTypeEnum.INNER_CONTRACT.getCode());
            tx.setHash(parentTx.getHash() + "-" + i);
            tx.setType(result.getTypeEnum().getCode());
            tx.setIndex(i);
            tx.setInput(tran.getInput());
            tx.setInfo(result.getParam().toJSONString());
            tx.setSeq((long) i);
            transactionList.add(tx);

            if (Integer.parseInt(tran.getCode()) > 0) {
                // The virtual transaction failed and the transaction status code was set to failed.
                tx.setStatus(Transaction.StatusEnum.FAILURE.getCode());
            }
        }
        return transactionList;
    }

    /**
     * Obtain the entrustment income withdrawn when the entrustment is released from real transactions
     */
    public static BigInteger getDelegateReward(List<Log> logs) {
        if (logs == null || logs.isEmpty()) {
            return BigInteger.ZERO;
        }
        return getDelegateReward(logs.get(0));
    }

    /**
     * General Delegation Reward Log Data Analysis
     *
     * @param log
     * @return
     */
    private static BigInteger getDelegateReward(Log log) {
        if (log == null) {
            return BigInteger.ZERO;
        }
        String logData = log.getData();
        if (null == logData || "".equals(logData)) {
            return BigInteger.ZERO;
        }

        RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(logData));
        List<RlpType> rlpList = ((RlpList) (rlp.getValues().get(0))).getValues();
        String decodedStatus = new String(((RlpString) rlpList.get(0)).getBytes());
        int statusCode = Integer.parseInt(decodedStatus);

        if (statusCode != ErrorCode.SUCCESS) {
            return BigInteger.ZERO;
        }

        return ((RlpString) (RlpDecoder.decode(((RlpString) rlpList.get(1)).getBytes())).getValues()
                                                                                        .get(0)).asPositiveBigInteger();
    }

    /**
     * Process virtual transactions
     *
     * @param block
     * @param specialApi
     * @param turnClient
     * @param contractInvokeTx
     * @param contractInvokeTxReceipt
     * @param logger
     * @return
     * @throws ContractInvokeException
     * @throws BlankResponseException
     */
    public static List<Transaction> processVirtualTx(Block block,
                                                     SpecialApi specialApi,
                                                     TurnClient turnClient,
                                                     CollectionTransaction contractInvokeTx,
                                                     Receipt contractInvokeTxReceipt,
                                                     Logger logger) throws
                                                                    ContractInvokeException,
                                                                    BlankResponseException {
        if (!PPosInvokeContractInputCache.hasCache(block.getNum())) {
            // If the PPOS call contract input information of the current transaction in the block does not exist, query the special node and update the cache.
            List<PPosInvokeContractInput> inputs = specialApi.getPPosInvokeInfo(turnClient.getWeb3jWrapper()
                                                                                            .getWeb3j(),
                                                                                BigInteger.valueOf(block.getNum()));
            logger.debug("Update cache-PPos call contract input parameters：{}", JSON.toJSONString(inputs, true));
            List<PPosInvokeContractInput> ppremoveList = new ArrayList<>();
            for (PPosInvokeContractInput input : inputs) {
                List<TransData> removeList = new ArrayList<>();
                for (TransData transData : input.getTransDatas()) {
                    if (transData.getCode().length() > 6) {
                        removeList.add(transData);
                    }
                }
                input.getTransDatas().removeAll(removeList);
                if (input.getTransDatas().isEmpty()) {
                    ppremoveList.add(input);
                }
            }
            inputs.removeAll(ppremoveList);
            if (inputs.size() > 0) {
                PPosInvokeContractInputCache.update(block.getNum(), inputs);
            }
        }

        // Get the input parameters of the PPOS operation called within the current ordinary contract call transaction.
        PPosInvokeContractInput input = PPosInvokeContractInputCache.getPPosInvokeContractInput(contractInvokeTx.getHash());
        // Construct a virtual PPOS transaction list using the input data of the internal call of the ordinary contract (including successful and failed PPOS calls)
        List<Transaction> virtualTxList = getVirtualTxList(block, contractInvokeTx, input);
        if (!virtualTxList.isEmpty()) {
            for (int i = 0; i < virtualTxList.size(); i++) {
                Transaction vt = virtualTxList.get(i);
                // Transaction failed, skipped
                if (vt.getStatus() != Transaction.StatusEnum.SUCCESS.getCode()) {
                    continue;
                }
                /**
                 * When the contract acts as an agent for PPOS, the internal structure of the logs in the receipt: List- - the log of virtual transaction 1 - ... - the log of virtual transaction n - the log of contract calls
                 */
                Log log = contractInvokeTxReceipt.getLogs().get(i);
                if (vt.getTypeEnum() == Transaction.TypeEnum.DELEGATE_EXIT) {
                    // Undelegate
                    BigInteger reward;
                    try {
                        reward = getDelegateReward(log);
                    } catch (Exception e) {
                        reward = BigInteger.ZERO;
                    }
                    // Parse the info field of vt, add the reward field, reserialize it, and backfill it into vt.
                    DelegateExitParam delegateExitParam = vt.getTxParam(DelegateExitParam.class);
                    BigDecimal rewardAmount = new BigDecimal(reward);
                    delegateExitParam.setReward(rewardAmount);
                    vt.setInfo(delegateExitParam.toJSONString());
                }

                if (vt.getTypeEnum() == Transaction.TypeEnum.CLAIM_REWARDS) {
                    // Receive Delegate rewards
                    DelegateRewardClaimParam delegateRewardClaimParam = DelegateRewardClaimParam.builder()
                                                                                                .rewardList(new ArrayList<>())
                                                                                                .build();
                    List<Log> logs = Arrays.asList(log);
                    PPOSTxDecodeResult result = PPOSTxDecodeUtil.decode(vt.getInput(), logs);
                    TxParam param = result.getParam();
                    if (param != null) {
                        delegateRewardClaimParam = (DelegateRewardClaimParam) param;
                    }
                    vt.setInfo(delegateRewardClaimParam.toJSONString());
                }
            }
        }
        // Filter out successful virtual transactions
        List<Transaction> successVirtualTransactions = new ArrayList<>();
        virtualTxList.forEach(vt -> {
            if (vt.getStatus() == Transaction.StatusEnum.SUCCESS.getCode()) {
                successVirtualTransactions.add(vt);
            }
        });
        return successVirtualTransactions;
    }

    /**
     * Internal contract calls transactions and parses supplementary information
     */
    public static void resolveInnerContractInvokeTxComplementInfo(CollectionTransaction tx,
                                                                  List<Log> logs,
                                                                  ComplementInfo ci) throws
                                                                                     BeanCreateOrUpdateException {
        PPOSTxDecodeResult decodedResult;
        try {
            // Analyze transaction input and transaction receipt log information
            decodedResult = PPOSTxDecodeUtil.decode(tx.getInput(), logs);
            ci.setType(decodedResult.getTypeEnum().getCode());
            ci.setInfo(decodedResult.getParam().toJSONString());
            ci.setToType(Transaction.ToTypeEnum.INNER_CONTRACT.getCode());
            ci.setContractType(ContractTypeEnum.INNER.getCode());
            ci.setMethod(null);
            ci.setBinCode(null);
        } catch (Exception e) {
            throw new BeanCreateOrUpdateException("transaction[hash:" + tx.getHash() + "] parameter parsing error:" + e.getMessage());
        }
    }

    /**
     * Get the Bin code of the contract
     *
     * @param turnClient
     * @param contractAddress
     * @return
     * @throws BeanCreateOrUpdateException
     */
    public static String getContractBinCode(CollectionTransaction tx,
                                            TurnClient turnClient,
                                            String contractAddress,
                                            Logger logger) throws BeanCreateOrUpdateException {
        try {
            BubbleGetCode turnGetCode = turnClient.getWeb3jWrapper()
                                                      .getWeb3j()
                                                      .bubbleGetCode(contractAddress,
                                                                     DefaultBlockParameter.valueOf(BigInteger.valueOf(tx.getNum())))
                                                      .send();
            return turnGetCode.getCode();
        } catch (Exception e) {
            turnClient.updateCurrentWeb3jWrapper();
            String error = "Error getting contract code[" + contractAddress + "]:" + e.getMessage();
            logger.error("{}", error);
            throw new BeanCreateOrUpdateException(error);
        }
    }

    /**
     * Create a normal contract and parse supplementary information
     *
     * @param result
     * @param contractAddress
     * @param turnClient
     * @param ci
     * @param log
     * @param contractTypeEnum
     * @return void
     */
    public static void resolveGeneralContractCreateTxComplementInfo(CollectionTransaction result,
                                                                    String contractAddress,
                                                                    TurnClient turnClient,
                                                                    ComplementInfo ci,
                                                                    Logger log,
                                                                    ContractTypeEnum contractTypeEnum) throws
                                                                                                       BeanCreateOrUpdateException {
        ci.setInfo("");
        ci.setBinCode(TransactionUtil.getContractBinCode(result, turnClient, result.getContractAddress(), log));

        if (contractTypeEnum == ContractTypeEnum.ERC20_EVM) {
            ci.setType(com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.ERC20_CONTRACT_CREATE.getCode());
            ci.setToType(com.turn.browser.elasticsearch.dto.Transaction.ToTypeEnum.ERC20_CONTRACT.getCode());
        } else if (contractTypeEnum == ContractTypeEnum.ERC721_EVM) {
            ci.setType(com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.ERC721_CONTRACT_CREATE.getCode());
            ci.setToType(com.turn.browser.elasticsearch.dto.Transaction.ToTypeEnum.ERC721_CONTRACT.getCode());
        } else if (contractTypeEnum == ContractTypeEnum.ERC1155_EVM) {
            ci.setType(com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.ERC1155_CONTRACT_CREATE.getCode());
            ci.setToType(com.turn.browser.elasticsearch.dto.Transaction.ToTypeEnum.ERC1155_CONTRACT.getCode());
        } else if (contractTypeEnum == ContractTypeEnum.WASM) {
            ci.setType(com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.WASM_CONTRACT_CREATE.getCode());
            ci.setToType(com.turn.browser.elasticsearch.dto.Transaction.ToTypeEnum.WASM_CONTRACT.getCode());
        } else {
            ci.setType(com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.EVM_CONTRACT_CREATE.getCode());
            ci.setToType(com.turn.browser.elasticsearch.dto.Transaction.ToTypeEnum.EVM_CONTRACT.getCode());
        }
        ci.setContractType(contractTypeEnum.getCode());

    }

    /**
     * Call ordinary contracts and parse supplementary information
     *
     * @param tx
     * @param turnClient
     * @param ci
     * @param contractTypeEnum
     * @param logger
     * @return void
     * @date 2021/4/20
     */
    public static void resolveGeneralContractInvokeTxComplementInfo(CollectionTransaction tx,
                                                                    TurnClient turnClient,
                                                                    ComplementInfo ci,
                                                                    ContractTypeEnum contractTypeEnum,
                                                                    Logger logger) throws BeanCreateOrUpdateException {
        ci.setInfo("");
        String binCode = getContractBinCode(tx, turnClient, tx.getTo(), logger);
        ci.setBinCode(binCode);
        // TODO: Parse out the calling contract method name
        String txInput = tx.getInput();
        // ci.method = getGeneralContractMethod();

        ci.setContractType(contractTypeEnum.getCode());
        if (contractTypeEnum == ContractTypeEnum.EVM) {
            ci.setToType(Transaction.ToTypeEnum.EVM_CONTRACT.getCode());
        }
        if (contractTypeEnum == ContractTypeEnum.WASM) {
            ci.setToType(Transaction.ToTypeEnum.WASM_CONTRACT.getCode());
        }
        ci.setType(Transaction.TypeEnum.CONTRACT_EXEC.getCode());
        if (contractTypeEnum == ContractTypeEnum.ERC20_EVM) {
            ci.setToType(Transaction.ToTypeEnum.ERC20_CONTRACT.getCode());
            ci.setType(Transaction.TypeEnum.ERC20_CONTRACT_EXEC.getCode());
        }
        if (contractTypeEnum == ContractTypeEnum.ERC721_EVM) {
            ci.setToType(Transaction.ToTypeEnum.ERC721_CONTRACT.getCode());
            ci.setType(Transaction.TypeEnum.ERC721_CONTRACT_EXEC.getCode());
        }
        if (contractTypeEnum == ContractTypeEnum.ERC1155_EVM) {
            ci.setToType(Transaction.ToTypeEnum.ERC1155_CONTRACT.getCode());
            ci.setType(Transaction.TypeEnum.ERC1155_CONTRACT_EXEC.getCode());
        }

        if (contractTypeEnum == ContractTypeEnum.GAME_EVM) {
            ci.setToType(Transaction.ToTypeEnum.GAME_CONTRACT.getCode());
            ci.setType(Transaction.TypeEnum.GAME_CONTRACT_EXEC.getCode());
        }

        if ("0x".equals(binCode)) {
            // If the binCode attribute of the transaction is 0x, it indicates that the contract self-destruction method is used, and the transaction type is set to contract destruction.
            ci.setType(Transaction.TypeEnum.CONTRACT_EXEC_DESTROY.getCode());
        }
    }

    /**
     * Initiate ordinary transactions and parse supplementary information
     *
     * @param ci
     */
    public static void resolveGeneralTransferTxComplementInfo(CollectionTransaction tx,
                                                              ComplementInfo ci,
                                                              AddressCache addressCache) {
        ci.setType(Transaction.TypeEnum.TRANSFER.getCode());
        ci.setContractType(null);
        ci.setMethod(null);
        ci.setInfo("{}");
        ci.setBinCode(null);
        // It needs to be based on whether the to address of the transaction is what type of address
        String toAddress = tx.getTo();
        if (InnerContractAddrEnum.getAddresses().contains(toAddress)) {
            ci.setToType(Transaction.ToTypeEnum.INNER_CONTRACT.getCode());
            ci.setContractType(ContractTypeEnum.INNER.getCode());
            ci.setMethod(ContractDescEnum.getMap().get(toAddress).getContractName());
            return;
        }
        if (addressCache.isEvmContractAddress(toAddress)) {
            ci.setToType(Transaction.ToTypeEnum.EVM_CONTRACT.getCode());
            ci.setContractType(ContractTypeEnum.EVM.getCode());
            return;
        }
        if (addressCache.isWasmContractAddress(toAddress)) {
            ci.setToType(Transaction.ToTypeEnum.WASM_CONTRACT.getCode());
            ci.setContractType(ContractTypeEnum.WASM.getCode());
            return;
        }
        if (addressCache.isErc20ContractAddress(toAddress)) {
            ci.setToType(Transaction.ToTypeEnum.ERC20_CONTRACT.getCode());
            ci.setContractType(ContractTypeEnum.ERC20_EVM.getCode());
            return;
        }
        if (addressCache.isErc721ContractAddress(toAddress)) {
            ci.setToType(Transaction.ToTypeEnum.ERC721_CONTRACT.getCode());
            ci.setContractType(ContractTypeEnum.ERC721_EVM.getCode());
            return;
        }
        if (addressCache.isErc1155ContractAddress(toAddress)) {
            ci.setToType(Transaction.ToTypeEnum.ERC1155_CONTRACT.getCode());
            ci.setContractType(ContractTypeEnum.ERC1155_EVM.getCode());
            return;
        }
        ci.setToType(Transaction.ToTypeEnum.ACCOUNT.getCode());
    }

    public static void handleTexasHoldem(CollectionTransaction contractInvokeTx,
                                         Receipt receipt,
                                         Web3j web3j,
                                         Logger logger) {
        if (CollUtil.isNotEmpty(receipt.getLogs())) {
            for (Log log : receipt.getLogs()) {
                if (CollUtil.isNotEmpty(log.getTopics())) {
                    String topics = log.getTopics().get(0);
                    if (TexasHoldemCache.cache.containsKey(topics)) {
                        try {
                            TexasHoldem texasHoldem = TexasHoldem.load(contractInvokeTx.getTo(),
                                                                       web3j,
                                                                       TexasHoldemCache.readCredentials,
                                                                       new DefaultGasProvider());
                            BubbleGetTransactionReceipt bubbleGetTransactionReceipt = web3j.bubbleGetTransactionReceipt(
                                    receipt.getTransactionHash()).send();
                            TransactionReceipt transactionReceipt = bubbleGetTransactionReceipt.getTransactionReceipt()
                                                                                               .get();
                            List<Object> events = ReflectUtil.invoke(texasHoldem,
                                                                     TexasHoldemCache.cache.get(topics),
                                                                     transactionReceipt);
                            contractInvokeTx.setTexasHoldemTxInfo(JSONUtil.toJsonStr(events));
                        } catch (Exception e) {
                            logger.error(StrUtil.format("Parsing TexasHoldem transaction exception{}", receipt.getTransactionHash()), e);
                        }
                    }
                }
            }
        }
    }


    public static void handleGameContract(CollectionTransaction contractInvokeTx,
                                          Receipt receipt,
                                          Web3j web3j,
                                          Logger logger, ComplementInfo ci) {
        if (CollUtil.isNotEmpty(receipt.getLogs())) {
            for (Log log : receipt.getLogs()) {
                if (CollUtil.isNotEmpty(log.getTopics())) {
                    log.getTopics().stream().forEach(topics->{
                        if (GameContractCache.cache.containsKey(topics)) {
                            try {
                                GameContract gameContract = GameContract.load(contractInvokeTx.getTo(),
                                        web3j,
                                        TexasHoldemCache.readCredentials,
                                        new DefaultGasProvider());
                                BubbleGetTransactionReceipt bubbleGetTransactionReceipt = web3j.bubbleGetTransactionReceipt(
                                        receipt.getTransactionHash()).send();
                                TransactionReceipt transactionReceipt = bubbleGetTransactionReceipt.getTransactionReceipt()
                                        .get();
                                List<Object> events = ReflectUtil.invoke(gameContract,
                                        GameContractCache.cache.get(topics),
                                        transactionReceipt);

                                HashMap<String,String> eventMap = new HashMap<>();
                                eventMap.put(GameContractCache.cache.get(topics),JSONUtil.toJsonStr(events));

                                List<String> gameContractEventInfo = contractInvokeTx.getGameContractEventInfo();
                                if(CollUtil.isEmpty(gameContractEventInfo)){
                                    gameContractEventInfo = new ArrayList<>();
                                }

                                gameContractEventInfo.add(JSONUtil.toJsonStr(eventMap));
                                contractInvokeTx.setGameContractEventInfo(gameContractEventInfo);
                                ci.setContractType(ContractTypeEnum.GAME_EVM.getCode());
                            } catch (Exception e) {
                                logger.error(StrUtil.format("Analyzing game contract transaction exceptions{}", receipt.getTransactionHash()), e);
                            }
                    }});

                }
            }
        }
    }

    public static List<Long> handleBubbleId(List<Log> logs) {
        CopyOnWriteArrayList<Long> bubbleIdList = new CopyOnWriteArrayList();
        if(CollUtil.isNotEmpty(logs)){
            List<Log> logList = logs.stream().filter(item -> InnerContractAddrEnum.getAddresses()
                    .contains(item.getAddress()) &&
                    item.getTopics().contains(CREATE_BUBBLE_TOPIC)).collect(Collectors.toList());
            logList.forEach(x->{
                RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(x.getData()));
                List<RlpType> rlpList = ((RlpList) (rlp.getValues().get(0))).getValues();
                RlpList integersList = RlpDecoder.decode(((RlpString) rlpList.get(1)).getBytes());
                RlpString integersString = (RlpString) integersList.getValues().get(0);
                Long bubbleId = new BigInteger(1, integersString.getBytes()).longValue();
                bubbleIdList.add(bubbleId);
            });
        }
        return bubbleIdList;
    }
}
