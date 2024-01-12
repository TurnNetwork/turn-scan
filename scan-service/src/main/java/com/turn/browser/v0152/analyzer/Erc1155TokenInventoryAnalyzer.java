package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.turn.browser.dao.custommapper.CustomToken1155InventoryMapper;
import com.turn.browser.dao.entity.Token1155Inventory;
import com.turn.browser.dao.entity.Token1155InventoryExample;
import com.turn.browser.dao.entity.Token1155InventoryKey;
import com.turn.browser.dao.entity.Token1155InventoryWithBLOBs;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.service.erc.ErcServiceImpl;
import com.turn.browser.utils.AddressUtil;
import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Erc1155,1155 token Inventory service
 */
@Slf4j
@Service
public class Erc1155TokenInventoryAnalyzer {

    @Resource
    private CustomToken1155InventoryMapper customToken1155InventoryMapper;

    @Resource
    private ErcServiceImpl ercServiceImpl;


    /**
     * Parse Token inventory
     * Overall logic
     * 1, determine whether the token has been recorded, if the number of records or transactions increases by 1
     * 2. If it does not exist, it will be stored in the warehouse, and then the number of transactions will be +1
     */
    public void analyze(String txHash, List<ErcTx> txList, BigInteger blockNumber) {
        List<Token1155InventoryWithBLOBs> insertOrUpdate = new ArrayList<>();
        List<Token1155InventoryKey> delToken1155InventoryKey = new ArrayList<>();
        if (CollUtil.isNotEmpty(txList)) {
            txList.forEach(tx -> {
                String tokenAddress = tx.getContract();
                String tokenId = tx.getTokenId();
                // Verify whether the tokenid length meets the storage standards
                if (CommonUtil.ofNullable(tokenId::length).orElse(0) > 128) {
                    // Only print logs and cannot block the process by throwing exceptions
                    log.warn("The current transaction [{}]token[{}] does not meet the contract standards, tokenId[{}] is too long and only supports 128 bits", txHash, tokenAddress, tokenId);
                } else {
                    // 1. Determine whether the receiving address exists. If it exists, add the balance to the balance.
                    Token1155InventoryExample toExample = new Token1155InventoryExample();
                    toExample.createCriteria().andTokenAddressEqualTo(tokenAddress).andTokenIdEqualTo(tokenId);
                    Token1155InventoryKey key = new Token1155InventoryKey();
                    key.setTokenAddress(tokenAddress);
                    key.setTokenId(tokenId);
                    Token1155InventoryWithBLOBs toTokenInventory = customToken1155InventoryMapper.findOneByUK(key);
                    // If not empty, the number of transactions +1
                    if (ObjectUtil.isNotNull(toTokenInventory)) {
                        toTokenInventory.setTokenTxQty(toTokenInventory.getTokenTxQty() + 1);
                    } else {
                        toTokenInventory = new Token1155InventoryWithBLOBs();
                        toTokenInventory.setTokenAddress(tokenAddress);
                        toTokenInventory.setTokenId(tokenId);
                        toTokenInventory.setTokenTxQty(1);
                        toTokenInventory.setRetryNum(0);
                        String tokenURI = ercServiceImpl.getToken1155URI(tokenAddress, new BigInteger(tokenId), blockNumber);
                        if (StrUtil.isNotBlank(tokenURI)) {
                            toTokenInventory.setTokenUrl(tokenURI);
                        } else {
                            log.warn("The tokenUrl of the contract [{}]tokenId[{}] obtained by the current block height [{}] is empty, please contact the administrator for processing", blockNumber, tokenAddress, tokenId);
                        }
                    }

                    insertOrUpdate.add(toTokenInventory);
                    // If the to address is the 0 address during the contract transaction, the TokenInventory record needs to be cleared
                    if (StrUtil.isNotBlank(tx.getTo()) && AddressUtil.isAddrZero(tx.getTo())) {
                        Token1155InventoryKey token1155InventoryKey = new Token1155InventoryKey();
                        token1155InventoryKey.setTokenId(tx.getTokenId());
                        token1155InventoryKey.setTokenAddress(tx.getContract());
                        delToken1155InventoryKey.add(token1155InventoryKey);
                    }
                }
            });

            if (CollUtil.isNotEmpty(insertOrUpdate)) {
                customToken1155InventoryMapper.batchInsertOrUpdateSelective(insertOrUpdate, Token1155Inventory.Column.values());
                log.info("The current transaction [{}] added erc1155 inventory [{}] successfully", txHash, insertOrUpdate.size());
            }
            if (CollUtil.isNotEmpty(delToken1155InventoryKey)) {
                customToken1155InventoryMapper.burnAndDelTokenInventory(delToken1155InventoryKey);
                log.info("Current transaction [{}] deleted erc721 inventory [{}] successfully", txHash, delToken1155InventoryKey.size());
            }
        }
    }


}
