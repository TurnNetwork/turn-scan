package com.turn.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import com.turn.browser.dao.custommapper.CustomToken1155HolderMapper;
import com.turn.browser.dao.entity.Token1155Holder;
import com.turn.browser.dao.entity.Token1155HolderKey;
import com.turn.browser.elasticsearch.dto.ErcTx;
import com.turn.browser.utils.AddressUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Erc721 token holder services
 */
@Slf4j
@Service
public class ErcToken1155HolderAnalyzer {

    @Resource
    private CustomToken1155HolderMapper customToken1155HolderMapper;

    /**
     * Parse Token Holder
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void analyze(List<ErcTx> txList) {
        List<Token1155Holder> insertOrUpdate = new ArrayList<>();
        txList.forEach(tx -> {
            resolveTokenHolder(tx.getFrom(), tx, insertOrUpdate);
            resolveTokenHolder(tx.getTo(), tx, insertOrUpdate);
        });
        if (CollUtil.isNotEmpty(insertOrUpdate)) {
            customToken1155HolderMapper.batchInsertOrUpdateSelective1155(insertOrUpdate, Token1155Holder.Column.values());
        }
    }

    /**
     * Parse
     *
     * @param ownerAddress: address
     * @param ercTx: erc transaction
     * @param insertOrUpdate: update list
     */
    private void resolveTokenHolder(String ownerAddress, ErcTx ercTx, List<Token1155Holder> insertOrUpdate) {
        // Zero address does not require creating a holder
        if (AddressUtil.isAddrZero(ownerAddress)) {
            log.warn("The address [{}] is a 0 address and no token holder will be created", ownerAddress);
            return;
        }
        Token1155HolderKey key = new Token1155HolderKey();
        key.setTokenAddress(ercTx.getContract());
        key.setAddress(ownerAddress);
        key.setTokenId(ercTx.getTokenId());
        Token1155Holder tokenHolder = customToken1155HolderMapper.selectByUK(key);
        if (tokenHolder == null) {
            tokenHolder = new Token1155Holder();
            tokenHolder.setTokenAddress(key.getTokenAddress());
            tokenHolder.setAddress(key.getAddress());
            //The balance is updated by the scheduled task and set to the default value
            tokenHolder.setBalance("0");
            tokenHolder.setTokenId(ercTx.getTokenId());
            tokenHolder.setTokenOwnerTxQty(1);
        } else {
            int tokenOwnerTxQty = tokenHolder.getTokenOwnerTxQty() == null ? 0 : tokenHolder.getTokenOwnerTxQty();
            tokenHolder.setTokenOwnerTxQty(tokenOwnerTxQty + 1);
        }
        log.info("The 1155 contract address [{}][{}], the holder's address [{}], and the holder's number of transactions for this contract is [{}]", tokenHolder.getTokenAddress(), tokenHolder .getTokenId(), tokenHolder.getAddress(), tokenHolder.getTokenOwnerTxQty());
        insertOrUpdate.add(tokenHolder);
    }

}
