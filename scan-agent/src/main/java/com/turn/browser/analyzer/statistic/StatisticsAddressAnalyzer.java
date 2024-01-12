package com.turn.browser.analyzer.statistic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.turn.browser.analyzer.TransactionAnalyzer;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.CustomAddress;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.cache.AddressCache;
import com.turn.browser.dao.custommapper.StatisticBusinessMapper;
import com.turn.browser.dao.entity.Address;
import com.turn.browser.dao.entity.AddressExample;
import com.turn.browser.dao.mapper.AddressMapper;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.enums.ContractTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsAddressAnalyzer {

    @Resource
    private AddressCache addressCache;

    @Resource
    private StatisticBusinessMapper statisticBusinessMapper;

    @Resource
    private AddressMapper addressMapper;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void analyze(CollectionEvent event, Block block, EpochMessage epochMessage) {
        long startTime = System.currentTimeMillis();
        log.debug("block({}),transactions({}),consensus({}),settlement({}),issue({})",
                block.getNum(),
                event.getTransactions().size(),
                epochMessage.getConsensusEpochRound(),
                epochMessage.getSettleEpochRound(),
                epochMessage.getIssueEpochRound());
        List<String> addresses = addressCache.getAll().stream().map(Address::getAddress).collect(Collectors.toList());
        // Query the address information corresponding to the cache from the database
        AddressExample condition = new AddressExample();
        condition.createCriteria().andAddressIn(addresses);
        List<Address> itemFromDb = addressMapper.selectByExampleWithBLOBs(condition);
        // 0 block initialization built-in address
        if (block.getNum().compareTo(0L) == 0 && CollUtil.isEmpty(itemFromDb)) {
            addressCache.getAll().forEach(address -> {
                ContractTypeEnum contractTypeEnum = TransactionAnalyzer.getGeneralContractAddressCache().get(address.getAddress());
                if (contractTypeEnum != null) {
                    switch (contractTypeEnum) {
                        case WASM:
                            address.setType(CustomAddress.TypeEnum.WASM.getCode());
                            break;
                        case EVM:
                            address.setType(CustomAddress.TypeEnum.EVM.getCode());
                            break;
                        case ERC20_EVM:
                            address.setType(CustomAddress.TypeEnum.ERC20_EVM.getCode());
                            break;
                        case ERC721_EVM:
                            address.setType(CustomAddress.TypeEnum.ERC721_EVM.getCode());
                            break;
                        case ERC1155_EVM:
                            address.setType(CustomAddress.TypeEnum.ERC1155_EVM.getCode());
                            break;
                    }
                }
            });
            List<Address> list = CollUtil.newArrayList(addressCache.getAll());
            statisticBusinessMapper.batchInsert(list);
            log.info("Initialization of inner address into database successfully:{}", JSONUtil.toJsonStr(list));
            return;
        } else if (CollUtil.isNotEmpty(itemFromDb)) {
            // Check whether there is a transaction with a bin attribute of 0x in the transaction list, and if so, set the contract address corresponding to to
            event.getTransactions().forEach(tx -> {
                // If the bin of tx is 0x, it indicates that the transaction is a destroyed contract transaction or a call to a destroyed contract transaction, and the to address must be the contract address.
                if ("0x".equals(tx.getBin())) {
                    itemFromDb.forEach(address -> {
                        if (address.getAddress().equalsIgnoreCase(tx.getTo())) {
                            if (StringUtils.isBlank(address.getContractDestroyHash())) {
                                // If the destruction transaction address cached by the current address is empty, set value
                                address.setContractDestroyHash(tx.getHash());
                            }
                        }
                    });
                }
            });
            List<Address> newItemFromDb = new ArrayList<>();
            for (Address address : itemFromDb) {
                Boolean flag = false;
                Address indb = new Address();
                indb.setAddress(address.getAddress());
                Address addCache = addressCache.getAddress(address.getAddress());
                // contractName
                String contractName = StrUtil.emptyToDefault(address.getContractName(), addCache.getContractName());
                if (StrUtil.isNotBlank(contractName)) {
                    indb.setContractName(contractName);
                    flag = true;
                }
                // Contract creator, database value takes precedence
                String contractCreate = StrUtil.emptyToDefault(address.getContractCreate(), addCache.getContractCreate());
                if (StrUtil.isNotBlank(contractCreate)) {
                    indb.setContractCreate(contractCreate);
                    flag = true;
                }
                // The contract creates a transaction hash, and the value in the database takes precedence.
                String contractCreateHash = StrUtil.emptyToDefault(address.getContractCreatehash(), addCache.getContractCreatehash());
                if (StrUtil.isNotBlank(contractCreateHash)) {
                    indb.setContractCreatehash(contractCreateHash);
                    flag = true;
                }
                // The contract destroys the transaction hash, and the value in the database takes precedence.
                String contractDestroyHash = StrUtil.emptyToDefault(address.getContractDestroyHash(), addCache.getContractDestroyHash());
                if (StrUtil.isNotBlank(contractDestroyHash)) {
                    indb.setContractDestroyHash(contractDestroyHash);
                    flag = true;
                }
                // Contract bin code data
                String contractBin = StrUtil.emptyToDefault(address.getContractBin(), addCache.getContractBin());
                if (StrUtil.isNotBlank(contractBin)) {
                    indb.setContractBin(contractBin);
                    flag = true;
                }
                if (flag) {
                    newItemFromDb.add(indb);
                }
            }
            if (CollUtil.isNotEmpty(newItemFromDb)) {
                for (Address address : newItemFromDb) {
                    // Abnormalities often occur in batch updates. When using a single update, the server pressure is high, but the number of addresses updated each time is not large, so the pressure is not bad.
                    addressMapper.updateByPrimaryKeySelective(address);
                }
                log.info("Batch update of address information was successful, number of successes: {}, data is: {}", newItemFromDb.size(), JSONUtil.toJsonStr(newItemFromDb));
            }
        }
        // Compare the cache and data data, and retrieve the newly added addresses in the cache.
        List<String> dbList = itemFromDb.stream().map(Address::getAddress).collect(Collectors.toList());
        List<String> newAddressList = CollUtil.subtractToList(addresses, dbList);
        List<Address> newAddrList = new ArrayList<>();
        if (CollUtil.isNotEmpty(newAddressList)) {
            addressCache.getAll().forEach(address -> {
                ContractTypeEnum contractTypeEnum = TransactionAnalyzer.getGeneralContractAddressCache().get(address.getAddress());
                if (contractTypeEnum != null) {
                    switch (contractTypeEnum) {
                        case WASM:
                            address.setType(CustomAddress.TypeEnum.WASM.getCode());
                            break;
                        case EVM:
                            address.setType(CustomAddress.TypeEnum.EVM.getCode());
                            break;
                        case ERC20_EVM:
                            address.setType(CustomAddress.TypeEnum.ERC20_EVM.getCode());
                            break;
                        case ERC721_EVM:
                            address.setType(CustomAddress.TypeEnum.ERC721_EVM.getCode());
                            break;
                        case ERC1155_EVM:
                            address.setType(CustomAddress.TypeEnum.ERC1155_EVM.getCode());
                            break;
                    }
                }
            });
            newAddressList.forEach(address -> {
                newAddrList.add(addressCache.getAddress(address));
            });
        }
        if (CollUtil.isNotEmpty(newAddrList)) {
            statisticBusinessMapper.batchInsert(newAddrList);
            log.info("Added address successfully{}", JSONUtil.toJsonStr(newAddrList));
        }
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
    }

}
