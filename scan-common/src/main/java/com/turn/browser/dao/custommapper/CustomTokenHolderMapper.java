package com.turn.browser.dao.custommapper;

import com.github.pagehelper.Page;
import com.turn.browser.bean.CustomTokenHolder;
import com.turn.browser.bean.TokenHolderCount;
import com.turn.browser.dao.entity.TokenHolder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomTokenHolderMapper {

    Page<CustomTokenHolder> selectListByParams(@Param("tokenAddress") String tokenAddress,
                                               @Param("address") String address,
                                               @Param("type") String type);

    Page<CustomTokenHolder> selectERC721Holder(@Param("tokenAddress") String tokenAddress);

    /**
     * Query the number of erc721 tokens
     *
     * @param tokenAddress
     * @param address
     * @param type
     * @return com.github.pagehelper.Page<com.turn.browser.bean.CustomTokenHolder>
     */
    Page<CustomTokenHolder> findErc721TokenHolder(@Param("tokenAddress") String tokenAddress,
                                                  @Param("address") String address,
                                                  @Param("type") String type);

    int batchInsertOrUpdateSelective(@Param("list") List<TokenHolder> list, @Param("selective") TokenHolder.Column... selective);

    /**
     * Update token holder balances in batches
     *
     * @param list
     * @return int
     */
    int batchUpdate(@Param("list") List<TokenHolder> list);

    /**
     * Query the number of holders corresponding to the token
     *
     * @param
     * @return java.util.List<com.turn.browser.bean.TokenHolderCount>
     */
    List<TokenHolderCount> findTokenHolderCount();

    /**
     * Query the TokenHolderList of erc721
     *
     * @param tokenAddress
     * @param address
     * @return com.github.pagehelper.Page<com.turn.browser.bean.CustomTokenHolder>
     */
    Page<CustomTokenHolder> selectListByERC721(@Param("tokenAddress") String tokenAddress, @Param("address") String address);

    /**
     * Get the token holder with a balance of 0
     *
     * @param type
     * @return
     */
    List<TokenHolder> getZeroBalanceTokenHolderList(@Param("type") String type,
                                                    @Param("offset") int offset,
                                                    @Param("limit") int limit,
                                                    @Param("orderby") String orderby);

}