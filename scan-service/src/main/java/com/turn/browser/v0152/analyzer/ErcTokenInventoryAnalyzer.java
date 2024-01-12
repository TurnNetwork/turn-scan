package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.turn.browser.dao.custommapper.CustomTokenInventoryMapper;
import com.turn.browser.dao.entity.TokenInventory;
import com.turn.browser.dao.entity.TokenInventoryExample;
import com.turn.browser.dao.entity.TokenInventoryKey;
import com.turn.browser.dao.entity.TokenInventoryWithBLOBs;
import com.turn.browser.dao.mapper.TokenInventoryMapper;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.service.erc.ErcServiceImpl;
import com.turn.browser.utils.AddressUtil;
import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ErcTokenInventoryAnalyzer {

    @Resource
    private TokenInventoryMapper tokenInventoryMapper;

    @Resource
    private CustomTokenInventoryMapper customTokenInventoryMapper;

    @Resource
    private ErcServiceImpl ercServiceImpl;

    /**
     * parse Token inventory
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void analyze(String txHash, List<ErcTx> txList, BigInteger blockNumber) {
        List<TokenInventoryWithBLOBs> insertOrUpdate = new ArrayList<>();
        List<TokenInventoryKey> delTokenInventory = new ArrayList<>();
        if (CollUtil.isNotEmpty(txList)) {
            txList.forEach(tx -> {
                String tokenAddress = tx.getContract();
                String tokenId = tx.getTokenId();
                // Verify whether the tokenid length meets the storage standards
                if (CommonUtil.ofNullable(() -> tokenId.length()).orElse(0) > 128) {
                    // Only print logs and cannot block the process by throwing exceptions
                    log.warn("The current transaction [{}]token[{}] does not meet the contract standards, tokenId[{}] is too long and only supports 128 bits", txHash, tokenAddress, tokenId);
                } else {
                    TokenInventoryExample example = new TokenInventoryExample();
                    example.createCriteria().andTokenAddressEqualTo(tokenAddress).andTokenIdEqualTo(tokenId);
                    List<TokenInventoryWithBLOBs> tokenInventoryWithBLOBs = tokenInventoryMapper.selectByExampleWithBLOBs(example);
                    TokenInventoryWithBLOBs tokenInventory;
                    // If not empty, add 1 to the number of transactions
                    if (CollUtil.isNotEmpty(tokenInventoryWithBLOBs) && tokenInventoryWithBLOBs.size() == 1) {
                        tokenInventory = CollUtil.getFirst(tokenInventoryWithBLOBs);
                        tokenInventory.setTokenTxQty(tokenInventory.getTokenTxQty() + 1);
                    } else {
                        //If it is empty, create a new object
                        tokenInventory = new TokenInventoryWithBLOBs();
                        tokenInventory.setTokenAddress(tokenAddress);
                        tokenInventory.setTokenId(tokenId);
                        tokenInventory.setTokenTxQty(1);
                        tokenInventory.setRetryNum(0);
                        tokenInventory.setCreateTime(new Date());
                        tokenInventory.setUpdateTime(new Date());
                        String tokenURI = ercServiceImpl.getTokenURI(tokenAddress, new BigInteger(tokenId), blockNumber);
                        if (StrUtil.isNotBlank(tokenURI)) {
                            tokenInventory.setTokenUrl(tokenURI);
                        } else {
                            log.warn("The tokenUrl of the contract [{}]tokenId[{}] obtained by the current block height [{}] is empty, please contact the administrator for processing", blockNumber, tokenAddress, tokenId);
                        }
                    }
                    if (tx.getTo().equalsIgnoreCase(tokenInventory.getOwner())) {
                        int tokenOwnerTxQty = tokenInventory.getTokenOwnerTxQty() == null ? 0 : tokenInventory.getTokenOwnerTxQty();
                        tokenInventory.setTokenOwnerTxQty(tokenOwnerTxQty + 1);
                    } else {
                        tokenInventory.setTokenOwnerTxQty(1);
                    }
                    tokenInventory.setOwner(tx.getTo());
                    insertOrUpdate.add(tokenInventory);
                    // If the to address is the 0 address during the contract transaction, the TokenInventory record needs to be cleared
                    if (StrUtil.isNotBlank(tx.getTo()) && AddressUtil.isAddrZero(tx.getTo())) {
                        TokenInventoryKey tokenInventoryKey = new TokenInventoryKey();
                        tokenInventoryKey.setTokenId(tx.getTokenId());
                        tokenInventoryKey.setTokenAddress(tx.getContract());
                        delTokenInventory.add(tokenInventoryKey);
                    }
                }
            });
            if (CollUtil.isNotEmpty(insertOrUpdate)) {
                customTokenInventoryMapper.batchInsertOrUpdateSelective(insertOrUpdate, TokenInventory.Column.values());
                log.info("The current transaction [{}] added erc721 inventory [{}] successfully", txHash, insertOrUpdate.size());
            }
            if (CollUtil.isNotEmpty(delTokenInventory)) {
                customTokenInventoryMapper.burnAndDelTokenInventory(delTokenInventory);
                log.info("Current transaction [{}] deleted erc721 inventory [{}] successfully", txHash, delTokenInventory.size());
            }
        }
    }

}
