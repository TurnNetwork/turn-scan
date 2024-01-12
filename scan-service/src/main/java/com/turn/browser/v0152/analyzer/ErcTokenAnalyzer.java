package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.CollectionTransaction;
import com.turn.browser.bean.ErcToken;
import com.turn.browser.bean.Receipt;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.dao.custommapper.CustomTokenMapper;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.Game;
import com.turn.browser.dao.entity.Token;
import com.turn.browser.dao.mapper.GameMapper;
import com.turn.browser.dao.mapper.TokenMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.enums.ErcTypeEnum;
import com.turn.browser.utils.AddressUtil;
import com.turn.browser.utils.CommonUtil;
import com.turn.browser.v0152.bean.ErcContractId;
import com.turn.browser.v0152.bean.ErcTxInfo;
import com.turn.browser.v0152.contract.ErcContract;
import com.turn.browser.v0152.service.ErcDetectService;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Erc Token analyzer
 */
@Slf4j
@Service
public class ErcTokenAnalyzer {

    @Resource
    private ErcDetectService ercDetectService;

    @Resource
    private ErcCache ercCache;

    @Resource
    private AddressCache addressCache;

    @Resource
    private ErcTokenInventoryAnalyzer ercTokenInventoryAnalyzer;

    @Resource
    private Erc1155TokenInventoryAnalyzer erc1155TokenInventoryAnalyzer;

    @Resource
    private ErcTokenHolderAnalyzer ercTokenHolderAnalyzer;

    @Resource
    private ErcToken1155HolderAnalyzer ercToken1155HolderAnalyzer;

    @Resource
    private CustomTokenMapper customTokenMapper;

    @Resource
    private TokenMapper tokenMapper;

    @Resource
    private GameCache gameCache;

    @Resource
    private GameMapper gameMapper;

    /**
     * Parse Token and call it when the contract is created
     *
     * @param contractAddress
     */
    public ErcToken resolveToken(String contractAddress, BigInteger blockNumber) {
        ErcToken token = new ErcToken();
        token.setTypeEnum(ErcTypeEnum.UNKNOWN);
        try {
            token.setAddress(contractAddress);
            ErcContractId contractId = ercDetectService.getContractId(contractAddress, blockNumber);
            if(ErcTypeEnum.UNKNOWN.equals(contractId.getTypeEnum())){
                return token;
            }
            BeanUtils.copyProperties(contractId, token);
            token.setTotalSupply(CommonUtil.ofNullable(() -> contractId.getTotalSupply().toPlainString()).orElse("0"));
            token.setTypeEnum(contractId.getTypeEnum());
            token.setType(contractId.getTypeEnum().name().toLowerCase());
            switch (contractId.getTypeEnum()) {
                case ERC20:
                    token.setIsSupportErc20(true);
                    token.setIsSupportErc165(false);
                    token.setIsSupportErc721(false);
                    token.setIsSupportErc721Enumeration(token.getIsSupportErc721());
                    token.setIsSupportErc721Metadata(token.getIsSupportErc721());
                    token.setIsSupportErc1155(false);
                    token.setIsSupportErc1155Metadata(token.getIsSupportErc1155());
                    ercCache.erc20AddressCache.add(contractAddress);
                    break;
                case ERC721:
                    token.setIsSupportErc20(false);
                    token.setIsSupportErc165(true);
                    token.setIsSupportErc721(true);
                    token.setIsSupportErc721Enumeration(ercDetectService.isSupportErc721Enumerable(contractAddress, blockNumber));
                    token.setIsSupportErc721Metadata(ercDetectService.isSupportErc721Metadata(contractAddress, blockNumber));
                    token.setIsSupportErc1155(false);
                    token.setIsSupportErc1155Metadata(token.getIsSupportErc1155());
                    ercCache.erc721AddressCache.add(contractAddress);
                    break;
                case ERC1155:
                    token.setIsSupportErc20(false);
                    token.setIsSupportErc165(true);
                    token.setIsSupportErc721(false);
                    token.setIsSupportErc721Enumeration(token.getIsSupportErc721());
                    token.setIsSupportErc721Metadata(token.getIsSupportErc721());
                    //
                    token.setIsSupportErc1155(true);
                    token.setIsSupportErc1155Metadata(ercDetectService.isSupportErc1155Metadata(contractAddress, blockNumber));
                    ercCache.erc1155AddressCache.add(contractAddress);
                    break;
                default:
            }

            if (token.getTypeEnum() != ErcTypeEnum.UNKNOWN) {
                // Store ERC721, ERC20 or ERC1155 Token records
                token.setTokenTxQty(0);
                token.setContractDestroyBlock(0L);
                token.setContractDestroyUpdate(false);
                // Check whether the token is legal
                checkToken(token);
                customTokenMapper.batchInsertOrUpdateSelective(Collections.singletonList(token), Token.Column.values());
                ercCache.tokenCache.put(token.getAddress(), token);
                log.info("The contract was created successfully, the contract address is [{}], and the contract type is [{}]", token.getAddress(), token.getType());
            }
        } catch (Exception e) {
            log.error("ERC contract creation, Token parsing exception", e);
        }
        return token;
    }

    /**
     * token verification---constrain verification based on mysql defined fields
     *
     * @param token contract
     */
    private void checkToken(ErcToken token) {
        // 1. Verify the contract name, which can be null and the constraint is 64
        if (StrUtil.isNotEmpty(token.getName())) {
            // Verify contract name length, default is 64
            if (CommonUtil.ofNullable(() -> token.getName().length()).orElse(0) > 64) {
                String name = StrUtil.fillAfter(StrUtil.sub(token.getName(), 0, 61), '.', 64);
                log.warn("The name of the token[{}] is too long (default 64 bits), it will be automatically intercepted, old value [{}], new value [{}]", token.getAddress(), token.getName( ), name);
                token.setName(name);
            }
        }
        // 2. Verify the contract symbol, which can be nul and is constrained to 64 bits.
        if (StrUtil.isNotEmpty(token.getSymbol())) {
            // Verify contract symbol, default is 64
            if (CommonUtil.ofNullable(() -> token.getSymbol().length()).orElse(0) > 64) {
                String symbol = StrUtil.fillAfter(StrUtil.sub(token.getSymbol(), 0, 61), '.', 64);
                log.warn("The contract symbol of the token[{}] is too long (default is 64 bits) and will be automatically intercepted, old value [{}], new value [{}]", token.getAddress(), token.getSymbol (), symbol);
                token.setSymbol(symbol);
            }
        }
    }

    /**
     * Parse the transaction from the transaction receipt event
     *
     * @param token token
     * @param tx transaction
     * @param eventList event list
     * @return java.util.List<com.turn.browser.elasticsearch.dto.ErcTx> erc transaction list
     */
    private List<ErcTx> resolveErcTxFromEvent(Token token, CollectionTransaction tx, List<ErcContract.ErcTxEvent> eventList, Long seq) {
        List<ErcTx> txList = new ArrayList<>();
        eventList.forEach(event -> {
            // Convert parameters to set internal transactions
            ErcTx ercTx = ErcTx.builder()
                               .seq(seq)
                               .bn(tx.getNum())
                               .hash(tx.getHash())
                               .bTime(tx.getTime())
                               .txFee(tx.getCost())
                               .fromType(addressCache.getTypeData(event.getFrom()))
                               .toType(addressCache.getTypeData(event.getTo()))
                               .operator(event.getOperator())
                               .from(event.getFrom())
                               .to(event.getTo())
                               .tokenId(event.getTokenId().toString())
                               .value(event.getValue().toString())
                               .name(token.getName())
                               .symbol(token.getSymbol())
                               .decimal(token.getDecimal())
                               .contract(token.getAddress())
                               .build();
            txList.add(ercTx);
            addAddressCache(event.getFrom(), event.getTo());
        });
        return txList;
    }

    /**
     * Parse the transaction from the transaction receipt event
     *
     * @param token:
     * @param tx:
     * @param eventList:
     * @param seq:
     * @return: java.util.List<com.turn.browser.elasticsearch.dto.ErcTx>
     */
    private List<ErcTx> resolveErc1155TxFromEvent(Token token, CollectionTransaction tx, List<ErcContract.ErcTxEvent> eventList, AtomicLong seq) {
        List<ErcTx> txList = new ArrayList<>();
        eventList.forEach(event -> {
            // Convert parameters to set internal transactions
            ErcTx ercTx = ErcTx.builder()
                               .seq(seq.incrementAndGet())
                               .bn(tx.getNum())
                               .hash(tx.getHash())
                               .bTime(tx.getTime())
                               .txFee(tx.getCost())
                               .fromType(addressCache.getTypeData(event.getFrom()))
                               .toType(addressCache.getTypeData(event.getTo()))
                               .operator(event.getOperator())
                               .from(event.getFrom())
                               .to(event.getTo())
                               .tokenId(event.getTokenId().toString())
                               .value(event.getValue().toString())
                               .name(token.getName())
                               .symbol(token.getSymbol())
                               .decimal(token.getDecimal())
                               .contract(token.getAddress())
                               .build();
            txList.add(ercTx);
            addAddressCache(event.getFrom(), event.getTo());
        });
        return txList;
    }

    /**
     * The from and to addresses of the real transaction are added to the address cache and then stored in the database.
     *
     * @param from:
     * @param to:
     */
    private void addAddressCache(String from, String to) {
        if (StrUtil.isNotBlank(from)) {
            Address fromAddress = addressCache.getAddress(from);
            if (fromAddress == null && !AddressUtil.isAddrZero(from)) {
                fromAddress = addressCache.createDefaultAddress(from);
                addressCache.addAddress(fromAddress);
            }
        }
        if (StrUtil.isNotBlank(to)) {
            Address toAddress = addressCache.getAddress(to);
            if (toAddress == null && !AddressUtil.isAddrZero(to)) {
                toAddress = addressCache.createDefaultAddress(to);
                addressCache.addAddress(toAddress);
            }
        }
    }

    /**
     * Get transaction information
     *
     * @param txList transaction list
     */
    private String getErcTxInfo(List<ErcTx> txList) {
        List<ErcTxInfo> infoList = new ArrayList<>();
        txList.forEach(tx -> {
            ErcTxInfo eti = new ErcTxInfo();
            BeanUtils.copyProperties(tx, eti);
            infoList.add(eti);
        });
        return JSON.toJSONString(infoList);
    }

    /**
     * Parse ERC transactions, called when the contract is called
     *
     * @param collectionBlock current block
     * @param tx transaction object
     * @param receipt transaction receipt: a transaction may contain multiple events, so there may be multiple transactions
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void resolveTx(Block collectionBlock, CollectionTransaction tx, Receipt receipt) {
        try {
            // Filter the transaction receipt log. The address cannot be empty and is in the token cache.
            List<Log> tokenLogs = receipt.getLogs()
                                         .stream()
                                         .filter(receiptLog -> StrUtil.isNotEmpty(receiptLog.getAddress()))
                                         .filter(receiptLog -> ercCache.tokenCache.containsKey(receiptLog.getAddress()))
                                         .collect(Collectors.toList());

            if (CollUtil.isEmpty(tokenLogs)) {
                return;
            }

            tokenLogs.forEach(tokenLog -> {
                ErcToken token = ercCache.tokenCache.get(tokenLog.getAddress());
                if (ObjectUtil.isNotNull(token)) {
                    List<ErcTx> txList;
                    String contractAddress = token.getAddress();
                    ErcTypeEnum typeEnum = ErcTypeEnum.valueOf(token.getType().toUpperCase());
                    TransactionReceipt transactionReceipt = new TransactionReceipt();
                    transactionReceipt.setLogs(receipt.getLogs().stream().filter(v -> v.getAddress().equalsIgnoreCase(contractAddress)).collect(Collectors.toList()));
                    transactionReceipt.setContractAddress(contractAddress);
                    List<ErcContract.ErcTxEvent> eventList;
                    switch (typeEnum) {
                        case ERC20:
                            eventList = ercDetectService.getErc20TxEvents(transactionReceipt, BigInteger.valueOf(collectionBlock.getNum()));
                            List<ErcContract.ErcTxEvent> erc20TxEventList = eventList.stream().filter(v -> ObjectUtil.equal(v.getLog(), tokenLog)).collect(Collectors.toList());
                            if (erc20TxEventList.size() > 1) {
                                log.error("Current transaction [{}] erc20 transaction receipt log parsing exception {}", tx.getHash(), tokenLog);
                                break;
                            }
                            txList = resolveErcTxFromEvent(token, tx, erc20TxEventList, collectionBlock.getSeq().incrementAndGet());
                            tx.getErc20TxList().addAll(txList);
                            ercTokenHolderAnalyzer.analyze(txList);
                            break;
                        case ERC721:
                            eventList = ercDetectService.getErc721TxEvents(transactionReceipt, BigInteger.valueOf(collectionBlock.getNum()));
                            List<ErcContract.ErcTxEvent> erc721TxEventList = eventList.stream().filter(v -> v.getLog().equals(tokenLog)).collect(Collectors.toList());
                            if (erc721TxEventList.size() > 1) {
                                log.error("Current transaction [{}] erc721 transaction receipt log parsing exception {}", tx.getHash(), tokenLog);
                                break;
                            }
                            txList = resolveErcTxFromEvent(token, tx, erc721TxEventList, collectionBlock.getSeq().incrementAndGet());
                            tx.getErc721TxList().addAll(txList);
                            ercTokenInventoryAnalyzer.analyze(tx.getHash(), txList, BigInteger.valueOf(collectionBlock.getNum()));
                            ercTokenHolderAnalyzer.analyze(txList);
                            break;
                        case ERC1155:
                            eventList = ercDetectService.getErc1155TxEvents(transactionReceipt, BigInteger.valueOf(collectionBlock.getNum()));
                            List<ErcContract.ErcTxEvent> erc1155TxEventList = eventList.stream().filter(v -> v.getLog().equals(tokenLog)).collect(Collectors.toList());
                            txList = resolveErc1155TxFromEvent(token, tx, erc1155TxEventList, collectionBlock.getSeq());
                            tx.getErc1155TxList().addAll(txList);
                            erc1155TokenInventoryAnalyzer.analyze(tx.getHash(), txList, BigInteger.valueOf(collectionBlock.getNum()));
                            ercToken1155HolderAnalyzer.analyze(txList);
                            break;
                        default:
                            break;
                    }
                    token.setUpdateTime(new Date());
                    token.setDirty(true);
                } else {
                    log.error("The Erc Token corresponding to the contract address [{}] was not found in the current transaction [{}] cache", tx.getHash(), tokenLog.getAddress());
                }
            });
            tx.setErc20TxInfo(getErcTxInfo(tx.getErc20TxList()));
            tx.setErc721TxInfo(getErcTxInfo(tx.getErc721TxList()));
            tx.setErc1155TxInfo(getErcTxInfo(tx.getErc1155TxList()));
            log.info("The current transaction [{}] has [{}] logs, among which there are [{}] token transactions, among which erc20 has [{}], among which erc721 has [{}], among which erc1155 has [{}]Pen",
                    tx.getHash(),
                    CommonUtil.ofNullable(() -> receipt.getLogs().size()).orElse(0),
                    CommonUtil.ofNullable(() -> tokenLogs.size()).orElse(0),
                    CommonUtil.ofNullable(() -> tx.getErc20TxList().size()).orElse(0),
                    CommonUtil.ofNullable(() -> tx.getErc721TxList().size()).orElse(0),
                    CommonUtil.ofNullable(() -> tx.getErc1155TxList().size()).orElse(0));

            //Contract processing for destruction
            if (tx.getType() == com.turn.browser.elasticsearch.dto.Transaction.TypeEnum.CONTRACT_EXEC_DESTROY.getCode()) {
                Token token = new Token();
                token.setAddress(tx.getTo());
                token.setContractDestroyBlock(collectionBlock.getNum());
                tokenMapper.updateByPrimaryKeySelective(token);
                log.info("Contract [{}] has been destroyed in block [{}]", receipt.getContractAddress(), collectionBlock.getNum());
            }

        } catch (Exception e) {
            log.error(StrUtil.format("Exception in parsing ERC transaction of current transaction [{}]", tx.getHash()), e);
        }
    }

}
