package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import com.turn.browser.dao.custommapper.CustomTokenHolderMapper;
import com.turn.browser.dao.entity.TokenHolder;
import com.turn.browser.dao.entity.TokenHolderKey;
import com.turn.browser.dao.mapper.TokenHolderMapper;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.utils.AddressUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ErcTokenHolderAnalyzer {

    @Resource
    private TokenHolderMapper tokenHolderMapper;

    @Resource
    private CustomTokenHolderMapper customTokenHolderMapper;

    private TokenHolderKey getTokenHolderKey(String ownerAddress, ErcTx ercTx) {
        TokenHolderKey key = new TokenHolderKey();
        key.setTokenAddress(ercTx.getContract());
        key.setAddress(ownerAddress);
        return key;
    }

    /**
     * parse Token Holder
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void analyze(List<ErcTx> txList) {
        List<TokenHolder> insertOrUpdate = new ArrayList<>();
        txList.forEach(tx -> {
            resolveTokenHolder(tx.getFrom(), tx, insertOrUpdate);
            resolveTokenHolder(tx.getTo(), tx, insertOrUpdate);
        });
        if (CollUtil.isNotEmpty(insertOrUpdate)) {
            customTokenHolderMapper.batchInsertOrUpdateSelective(insertOrUpdate,
                                                                 TokenHolder.Column.excludes(TokenHolder.Column.createTime,
                                                                                             TokenHolder.Column.updateTime));
        }
    }

    private void resolveTokenHolder(String ownerAddress, ErcTx ercTx, List<TokenHolder> insertOrUpdate) {
        // 零地址不需要創建holder
        if (AddressUtil.isAddrZero(ownerAddress)) {
            log.warn("The address [{}] is a 0 address and no token holder will be created", ownerAddress);
            return;
        }
        TokenHolderKey key = getTokenHolderKey(ownerAddress, ercTx);
        TokenHolder tokenHolder = tokenHolderMapper.selectByPrimaryKey(key);
        if (tokenHolder == null) {
            tokenHolder = new TokenHolder();
            tokenHolder.setTokenAddress(key.getTokenAddress());
            tokenHolder.setAddress(key.getAddress());
            tokenHolder.setTokenTxQty(1);
            tokenHolder.setBalance("0");
        } else {
            tokenHolder.setTokenTxQty(tokenHolder.getTokenTxQty() + 1);
        }
        //TokenTxQty: The total number of user transactions for erc20, or the total number of user transactions for all tokenIds of erc721, erc1155
        log.info("The contract address [{}], the holder's address [{}], and the holder's number of transactions for this contract is [{}]",
                tokenHolder.getTokenAddress(),
                tokenHolder.getAddress(),
                tokenHolder.getTokenTxQty());
        insertOrUpdate.add(tokenHolder);
    }

}
