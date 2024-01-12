package com.turn.browser.dao.custommapper;

import com.turn.browser.dao.entity.Token1155Inventory;
import com.turn.browser.dao.entity.TokenHolder;
import com.turn.browser.dao.entity.TokenInventory;
import com.turn.browser.param.sync.*;
import com.turn.browser.task.bean.TokenHolderNum;
import com.turn.browser.task.bean.TokenHolderType;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyncTokenInfoMapper {

    /**
     * Synchronize the number of Token transactions
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void syncTokenTxCount(@Param("addressParams") List<AddressTokenQtyUpdateParam> addressParams, @Param("tokenParams") List<Erc20TokenTxCountUpdateParam> tokenParams, @Param("tokenAddressParams") List<Erc20TokenAddressRelTxCountUpdateParam> tokenAddressParams, @Param("networkParam") NetworkStatTokenQtyUpdateParam networkParam);

    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateTotalSupply(@Param("totalSupplyParams") List<TotalSupplyUpdateParam> totalSupplyParams);

    /**
     * Update total supply
     *
     * @param totalSupplyParams list that needs to be updated in batches
     * @return void
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateTokenTotalSupply(@Param("totalSupplyParams") List<TotalSupplyUpdateParam> totalSupplyParams);

    /**
     * Update address token balance
     *
     * @param list list that needs to be updated in batches
     * @return void
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateAddressBalance(@Param("list") List<TokenHolder> list);

    /**
     * Update token_inventory
     *
     * @param list list that needs to be updated in batches
     * @return void
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateToken721Inventory(@Param("list") List<TokenInventory> list);

    /**
     * Update token_inventory
     *
     * @param list list that needs to be updated in batches
     * @return void
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateToken1155Inventory(@Param("list") List<Token1155Inventory> list);

    /**
     * Update token_inventory
     *
     * @param list list that needs to be updated in batches
     * @return void
     * @date 2021/1/18
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateTokenInventory(@Param("list") List<TokenInventory> list);

    /**
     * Query the user statistics of the contract address
     *
     * @return java.util.List<com.turn.browser.task.bean.TokenHolderNum>
     */
    List<TokenHolderNum> findTokenHolder();

    /**
     * Update the number of holders corresponding to the token
     *
     * @param list list that needs to be updated in batches
     * @return void
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void updateTokenHolder(@Param("list") List<TokenHolderNum> list);

    /**
     * Query token_holder and return the contract type
     *
     * @return java.util.List<com.turn.browser.task.bean.TokenHolderType>
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    List<TokenHolderType> findTokenHolderType();

}