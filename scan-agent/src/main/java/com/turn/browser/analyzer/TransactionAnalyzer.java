package com.turn.browser.analyzer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.turn.browser.bean.*;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.Token;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.dao.mapper.TokenMapper;
import com.turn.browser.decoder.TxInputDecodeResult;
import com.turn.browser.decoder.TxInputDecodeUtil;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.ContractTypeEnum;
import com.turn.browser.enums.ErcTypeEnum;
import com.turn.browser.enums.GameTypeEnum;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.exception.BeanCreateOrUpdateException;
import com.turn.browser.exception.BlankResponseException;
import com.turn.browser.exception.ContractInvokeException;
import com.turn.browser.param.DelegateExitParam;
import com.turn.browser.param.DelegateRewardClaimParam;
import com.turn.browser.utils.AddressUtil;
import com.turn.browser.utils.TransactionUtil;
import com.turn.browser.v0152.analyzer.*;
import com.bubble.protocol.core.methods.response.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Transaction Analyzer
 */
@Slf4j
@Component
public class TransactionAnalyzer {

    @Resource
    private TurnClient turnClient;

    @Resource
    private AddressCache addressCache;

    @Resource
    private ErcCache ercCache;

    @Resource
    private SpecialApi specialApi;

    @Resource
    private ErcTokenAnalyzer ercTokenAnalyzer;

    @Resource
    private GameContractAnalyzer gameContractAnalyzer;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private TokenMapper tokenMapper;

    @Resource
    private MicroNodeAnalyzer microNodeAnalyzer;

    @Resource
    private GameCache gameCache;

    // In the transaction parsing phase, it maintains its own list of ordinary contract addresses, and its initialization data comes from the address cache and erc cache.
    // <Common contract address, contract type enumeration>
    private static final Map<String, ContractTypeEnum> GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP = new HashMap<>();

    public static Map<String, ContractTypeEnum> getGeneralContractAddressCache() {
        return Collections.unmodifiableMap(GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP);
    }

    public static void setGeneralContractAddressCache(String key, ContractTypeEnum contractTypeEnum) {
        GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(key, contractTypeEnum);
    }

    /**
     * Use address cache to initialize ordinary contract cache information
     *
     * @param
     * @return void
     */
    private void initGeneralContractCache() {
        if (GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.isEmpty()) {
            addressCache.getEvmContractAddressCache()
                        .forEach(address -> GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(address, ContractTypeEnum.EVM));
            addressCache.getWasmContractAddressCache()
                        .forEach(address -> GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(address, ContractTypeEnum.WASM));
            ercCache.getErc20AddressCache()
                    .forEach(address -> GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(address, ContractTypeEnum.ERC20_EVM));
            ercCache.getErc721AddressCache()
                    .forEach(address -> GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(address, ContractTypeEnum.ERC721_EVM));
            ercCache.getErc1155AddressCache()
                    .forEach(address -> GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(address, ContractTypeEnum.ERC1155_EVM));
            gameCache.getGameAddressCache()
                    .forEach(address-> GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(address, ContractTypeEnum.GAME_EVM));
        }
    }

    /**
     * Transaction analysis
     *
     * @param collectionBlock
     * @param rawTransaction
     * @param receipt
     * @return com.turn.browser.bean.CollectionTransaction
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public CollectionTransaction analyze(Block collectionBlock, Transaction rawTransaction, Receipt receipt) throws
                                                                                                             BeanCreateOrUpdateException,
                                                                                                             ContractInvokeException,
                                                                                                             BlankResponseException {
        CollectionTransaction result = CollectionTransaction.newInstance()
                                                            .updateWithBlock(collectionBlock)
                                                            .updateWithRawTransaction(rawTransaction);
        log.info("Current block [{}] transaction [{}] parsing begins...", collectionBlock.getNum(), result.getHash());
        // Use address cache to initialize ordinary contract cache information
        initGeneralContractCache();

        // ============Transaction information that needs to be supplemented by decoding============
        ComplementInfo ci = new ComplementInfo();

        // Newly created contract processing
        if (CollUtil.isNotEmpty(receipt.getContractCreated())) {
            receipt.getContractCreated().forEach(contract -> {
                // Solidity type erc20 or 721 token detection and entry
                ErcToken ercToken = ercTokenAnalyzer.resolveToken(contract.getAddress(),
                                                                  BigInteger.valueOf(collectionBlock.getNum()));

                // solidity or wasm
                TxInputDecodeResult txInputDecodeResult = TxInputDecodeUtil.decode(result.getInput());
                // check business contract
                if(ErcTypeEnum.UNKNOWN.equals(ercToken.getTypeEnum())){
                    GameContract gameContract = gameContractAnalyzer.resolveGameContract(contract.getAddress());
                    if (gameContract.getTypeEnum() == GameTypeEnum.GAME && txInputDecodeResult.getTypeEnum() == com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.EVM_CONTRACT_CREATE) {
                        GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(contract.getAddress(), ContractTypeEnum.GAME_EVM);
                    }
                }

                // Update address type in memory
                ContractTypeEnum contractTypeEnum;
                if (ercToken.getTypeEnum() == ErcTypeEnum.ERC20 && txInputDecodeResult.getTypeEnum() == com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.EVM_CONTRACT_CREATE) {
                    contractTypeEnum = ContractTypeEnum.ERC20_EVM;
                } else if (ercToken.getTypeEnum() == ErcTypeEnum.ERC721 && txInputDecodeResult.getTypeEnum() == com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.EVM_CONTRACT_CREATE) {
                    contractTypeEnum = ContractTypeEnum.ERC721_EVM;
                } else if (ercToken.getTypeEnum() == ErcTypeEnum.ERC1155 && txInputDecodeResult.getTypeEnum() == com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.EVM_CONTRACT_CREATE) {
                    contractTypeEnum = ContractTypeEnum.ERC1155_EVM;
                } else if (txInputDecodeResult.getTypeEnum() == com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.WASM_CONTRACT_CREATE) {
                    contractTypeEnum = ContractTypeEnum.WASM;
                } else {
                    contractTypeEnum = ContractTypeEnum.EVM;
                }
                GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.put(contract.getAddress(), contractTypeEnum);
                log.info("The contract type of the current transaction [{}] contract address [{}] is [{}]",
                         result.getHash(),
                         contract.getAddress(),
                         contractTypeEnum);
                if (!AddressUtil.isAddrZero(contract.getAddress())) {
                    // 补充address
                    addressCache.updateFirst(contract.getAddress(), contractTypeEnum);
                } else {
                    log.error("The address {} is a 0 address and is not loaded into the address cache.", contract.getAddress());
                }
            });
        }

        // Process transaction information
        String inputWithoutPrefix = StringUtils.isNotBlank(result.getInput()) ? result.getInput()
                                                                                      .replace("0x", "") : "";
        if (InnerContractAddrEnum.getAddresses()
                                 .contains(result.getTo()) && StringUtils.isNotBlank(inputWithoutPrefix)) {
            // If the to address is a built-in contract address, decode the transaction input
            TransactionUtil.resolveInnerContractInvokeTxComplementInfo(result, receipt.getLogs(), ci);


            //Micronode transaction processing
            microNodeAnalyzer.resolveTx(result, ci, receipt.getStatus());
            log.info("The current transaction [{}] is a built-in contract, from[{}], to[{}], decode transaction input",
                     result.getHash(),
                     result.getFrom(),
                     result.getTo());
        } else {
            // When the to address is empty or contractAddress has a value, it means that the transaction is to create a contract.
            if (StringUtils.isBlank(result.getTo())) {
                TransactionUtil.resolveGeneralContractCreateTxComplementInfo(result,
                                                                             receipt.getContractAddress(),
                                                                             turnClient,
                                                                             ci,
                                                                             log,
                                                                             GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.get(
                                                                                     receipt.getContractAddress()));
                result.setTo(receipt.getContractAddress());
                log.info("The current transaction [{}] is to create a contract, from[{}], to[{}], type is [{}], toType[{}], contractType is [{}]",
                         result.getHash(),
                         result.getFrom(),
                         result.getTo(),
                         ci.getType(),
                         ci.getToType(),
                         ci.getContractType());
            } else {
                if (GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.containsKey(result.getTo()) && inputWithoutPrefix.length() >= 8) {
                    // If it is a normal contract call (EVM||WASM)
                    ContractTypeEnum contractTypeEnum = GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.get(result.getTo());
                    TransactionUtil.resolveGeneralContractInvokeTxComplementInfo(result,
                                                                                 turnClient,
                                                                                 ci,
                                                                                 contractTypeEnum,
                                                                                 log);
                    // Whether the transaction called by the ordinary contract is successful only depends on the status of the receipt, not the status in the log.
                    result.setStatus(receipt.getStatus());
                    if (result.getStatus() == com.turn.browser.elasticsearch.dto.Transaction.StatusEnum.SUCCESS.getCode()) {
                        // bubble creation contract processing
                        List<Long> bubbleIds = TransactionUtil.handleBubbleId(receipt.getLogs());
                        if(CollUtil.isNotEmpty(bubbleIds)){
                            bubbleIds.forEach(x-> microNodeAnalyzer.createBubble(x));
                        }else {
                            // The ordinary contract call is successful and the successful proxy PPOS virtual transaction list is obtained.
                            List<com.turn.browser.elasticsearch.dto.Transaction> successVirtualTransactions = TransactionUtil.processVirtualTx(
                                    collectionBlock,
                                    specialApi,
                                    turnClient,
                                    result,
                                    receipt,
                                    log);
                            // Link successful virtual transactions to current ordinary contract transactions
                            result.setVirtualTransactions(successVirtualTransactions);
                            TransactionUtil.handleTexasHoldem(result,
                                    receipt,
                                    turnClient.getWeb3jWrapper().getWeb3j(),
                                    log);
                            // Process game contracts
                            TransactionUtil.handleGameContract(result,
                                    receipt,
                                    turnClient.getWeb3jWrapper().getWeb3j(),
                                    log,ci);
                        }

                    }
                    log.info("The current transaction [{}] is a normal contract call, from[{}], to[{}], type is [{}], toType[{}], and the number of virtual transactions is [{}]",
                             result.getHash(),
                             result.getFrom(),
                             result.getTo(),
                             ci.getType(),
                             ci.getToType(),
                             result.getVirtualTransactions().size());
                } else {
                    BigInteger value = StringUtils.isNotBlank(result.getValue()) ? new BigInteger(result.getValue()) : BigInteger.ZERO;
                    if (value.compareTo(BigInteger.ZERO) >= 0) {
                        // If the input is empty and the value is greater than 0, it is an ordinary transfer.
                        TransactionUtil.resolveGeneralTransferTxComplementInfo(result, ci, addressCache);
                        log.info("The current transaction [{}] is an ordinary transfer, from[{}], to[{}], and the transfer amount is [{}]",
                                 result.getHash(),
                                 result.getFrom(),
                                 result.getTo(),
                                 value);
                    }
                }
            }
        }

        if (ci.getType() == null) {
            throw new BeanCreateOrUpdateException("The transaction type is empty and an unknown transaction was encountered.:[blockNumber=" + result.getNum() + ",txHash=" + result.getHash() + "]");
        }
        if (ci.getToType() == null) {
            throw new BeanCreateOrUpdateException("To address is empty:[blockNumber=" + result.getNum() + ",txHash=" + result.getHash() + "]");
        }

        // By default, the status field is taken as the status of whether the transaction is successful or not.
        int status = receipt.getStatus();
        if (InnerContractAddrEnum.getAddresses()
                                 .contains(result.getTo())
                && ci.getType() != com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.STAKING_TOKEN.getCode()
                && ci.getType() != com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.TRANSFER.getCode()) {
            // If the recipient is a built-in contract and is not a transfer, the status in the log is used as the status of whether the transaction is successful or not.
            status = receipt.getLogStatus();
        }

        // transaction information
        result.setGasUsed(receipt.getGasUsed().toString())
              .setCost(result.decimalGasUsed().multiply(result.decimalGasPrice()).toString())
              .setFailReason(receipt.getFailReason())
              .setStatus(status)
              .setSeq(result.getNum() * 100000 + result.getIndex())
              .setInfo(ci.getInfo())
              .setType(ci.getType())
              .setToType(ci.getToType())
              .setContractAddress(receipt.getContractAddress())
              .setContractType(ci.getContractType())
              .setBin(ci.getBinCode())
              .setMethod(ci.getMethod());
        ercTokenAnalyzer.resolveTx(collectionBlock, result, receipt);
        if(ci.getContractType() != null && ContractTypeEnum.GAME_EVM.getCode() == ci.getContractType()){
            gameContractAnalyzer.resolveTx(result);
        }

        // Cumulative total number of transactions
        collectionBlock.setTxQty(collectionBlock.getTxQty() + 1);
        // Accumulate the number of specific business transactions
        switch (result.getTypeEnum()) {
            case TRANSFER: // For transfer transactions, add one to the number of transfer transactions from the address
                collectionBlock.setTranQty(collectionBlock.getTranQty() + 1);
                break;
            case STAKE_CREATE:// Create a validator
            case STAKE_INCREASE:// Increase own stake
            case STAKE_MODIFY:// Edit validator
            case STAKE_EXIT:// Exit validator
            case REPORT:// Report validator
                collectionBlock.setSQty(collectionBlock.getSQty() + 1);
                break;
            case DELEGATE_CREATE:// Initiate a commission
                collectionBlock.setDQty(collectionBlock.getDQty() + 1);
                break;
            case DELEGATE_EXIT:// Revoke delegation
                if (status == Receipt.SUCCESS) {
                    // The info backfill will be parsed only after successfully receiving the transaction.
                    //Set the delegation reward withdrawal amount
                    DelegateExitParam param = result.getTxParam(DelegateExitParam.class);
                    BigDecimal reward = new BigDecimal(TransactionUtil.getDelegateReward(receipt.getLogs()));
                    param.setReward(reward);
                    result.setInfo(param.toJSONString());
                }
                collectionBlock.setDQty(collectionBlock.getDQty() + 1);
                break;
            case CLAIM_REWARDS: // Receive entrust rewards
                DelegateRewardClaimParam param = DelegateRewardClaimParam.builder()
                                                                         .rewardList(new ArrayList<>())
                                                                         .build();
                if (status == Receipt.SUCCESS) {
                    // The info backfill will be parsed only after successfully receiving the transaction.
                    param = result.getTxParam(DelegateRewardClaimParam.class);
                }
                result.setInfo(param.toJSONString());
                collectionBlock.setDQty(collectionBlock.getDQty() + 1);
                break;
            case PROPOSAL_TEXT:// creates a text proposal
            case PROPOSAL_UPGRADE:// Create an upgrade proposal
            case PROPOSAL_PARAMETER:// Create parameter proposal
            case PROPOSAL_VOTE:// proposal voting
            case PROPOSAL_CANCEL:// Cancel proposal
            case VERSION_DECLARE:// version declaration
                collectionBlock.setPQty(collectionBlock.getPQty() + 1);
                break;
            default:
        }
        // Accumulate the current transaction fee to the txFee of the current block
        String txFee = collectionBlock.decimalTxFee().add(result.decimalCost()).toString();
        log.info("Current block [{}] transaction [{}]: block accumulated handling fee [{}] = accumulated handling fee [{}] + transaction cost [{}] (gas fuel [{}] * gas price [{} ])",
                 collectionBlock.getNum(),
                 result.getHash(),
                 txFee,
                 collectionBlock.decimalTxFee(),
                 result.decimalCost(),
                 result.decimalGasUsed(),
                 result.decimalGasPrice());
        collectionBlock.setTxFee(txFee);
        // Accumulate the energy limit of the current transaction to the txGasLimit of the current block
        collectionBlock.setTxGasLimit(collectionBlock.decimalTxGasLimit().add(result.decimalGasLimit()).toString());
        proxyContract(result.getHash());
        return result;
    }

    /**
     * Special contract modifications will only take effect on the mainnet
     *
     * @param :
     * @return: void
     */
    private void proxyContract(String txHash) {
        // The first transaction after creating the contract will set the 721 attribute of the contract.
        if ("0x908a9f487a1c9d39a17afe1a868705eec9b0ec899998eb7036412634388755ad".equalsIgnoreCase(txHash)) {
            Address address = addressMapper.selectByPrimaryKey("lat1w9ys9726hyhqk9yskffgly08xanpzdgqvp2sz6");
            if (ObjectUtil.isNotNull(address)) {
                Address newAddress = new Address();
                newAddress.setAddress(address.getAddress());
                newAddress.setType(6);
                addressMapper.updateByPrimaryKeySelective(newAddress);
                Token token = new Token();
                token.setAddress("lat1w9ys9726hyhqk9yskffgly08xanpzdgqvp2sz6");
                token.setType("erc721");
                token.setName("QPassport");
                token.setSymbol("QPT");
                token.setTotalSupply("0");
                token.setDecimal(0);
                token.setIsSupportErc165(true);
                token.setIsSupportErc20(false);
                token.setIsSupportErc721(true);
                token.setIsSupportErc721Enumeration(true);
                token.setIsSupportErc721Metadata(true);
                token.setIsSupportErc1155(false);
                token.setIsSupportErc1155Metadata(false);
                token.setTokenTxQty(0);
                token.setHolder(0);
                token.setContractDestroyBlock(0L);
                token.setContractDestroyUpdate(false);
                tokenMapper.insertSelective(token);
                // reset cache
                GENERAL_CONTRACT_ADDRESS_2_TYPE_MAP.clear();
                ercCache.init();
                addressCache.getEvmContractAddressCache().remove("lat1w9ys9726hyhqk9yskffgly08xanpzdgqvp2sz6");
            } else {
                log.error("The agency contract address cannot be found{}", "lat1w9ys9726hyhqk9yskffgly08xanpzdgqvp2sz6");
            }
        }
    }

}
