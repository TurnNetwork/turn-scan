package com.turn.browser.dao.custommapper;

import com.github.pagehelper.Page;
import com.turn.browser.bean.*;
import com.turn.browser.dao.entity.Token1155Holder;
import com.turn.browser.dao.entity.Token1155HolderKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomToken1155HolderMapper {

    /**
     * Query based on unique index
     *
     * @param token1155HolderKey:
     * @return: com.turn.browser.dao.entity.Token1155Holder
     */
    Token1155Holder selectByUK(@Param("token1155HolderKey") Token1155HolderKey token1155HolderKey);

    /**
     * Add or update in batches
     *
     * @param list:
     * @param selective:
     * @return: int
     */
    int batchInsertOrUpdateSelective1155(@Param("list") List<Token1155Holder> list, @Param("selective") Token1155Holder.Column... selective);

    /**
     * Update token holder balances in batches
     *
     * @param list:
     * @return: int
     */
    int batchUpdate(@Param("list") List<Token1155Holder> list);

    /**
     * Find destroyed contracts
     *
     * @param type:
     * @return: java.util.List<com.turn.browser.bean.Erc1155ContractDestroyBean>
     */
    List<Erc1155ContractDestroyBean> findDestroyContract(@Param("type") Integer type);

    /**
     * Find the holder under the contract
     *
     * @param contract:
     * @return: com.github.pagehelper.Page<com.turn.browser.bean.Token1155HolderListBean>
     */
    Page<Token1155HolderListBean> findToken1155HolderList(@Param("contract") String contract);

    /**
     * Inventory list
     *
     * @param key:
     * @return: com.github.pagehelper.Page<com.turn.browser.bean.TokenIdListBean>
     */
    Page<TokenIdListBean> findTokenIdList(@Param("key") Token1155HolderKey key);

    /**
     * Query the TokenHolderList of erc1155
     *
     * @param tokenAddress
     * @param address
     * @return com.github.pagehelper.Page<com.turn.browser.bean.CustomTokenHolder>
     * @author dexin.y@digquant.com
     */
    Page<CustomTokenHolder> selectListByERC1155(@Param("tokenAddress") String tokenAddress, @Param("address") String address);

    Page<CustomTokenHolder> findErc1155TokenHolder(@Param("address") String address);

    Page<TokenHolderCount> findToken1155Holder();

}