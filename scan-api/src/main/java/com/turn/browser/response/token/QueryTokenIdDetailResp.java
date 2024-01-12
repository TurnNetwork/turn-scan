package com.turn.browser.response.token;

import com.turn.browser.bean.CustomToken1155Inventory;
import com.turn.browser.bean.CustomTokenInventory;
import com.turn.browser.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Contract list data response message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QueryTokenIdDetailResp {

    private String address;

    private String contract;

    private String tokenId;

    private String balance;

    private String image;

    private String name;

    private Integer txCount;


    private String tokenName;

    private String symbol;

    public static QueryTokenIdDetailResp copy(CustomTokenInventory source) {
        return QueryTokenIdDetailResp.builder()
                                     .address(CommonUtil.ofNullable(() -> source.getOwner()).orElse(""))
                                     .contract(CommonUtil.ofNullable(() -> source.getTokenAddress()).orElse(""))
                                     .tokenId(CommonUtil.ofNullable(() -> source.getTokenId().toString()).orElse(""))
                                     .image(CommonUtil.ofNullable(() -> source.getImage()).orElse(""))
                                     .txCount(CommonUtil.ofNullable(() -> source.getTokenTxQty()).orElse(0))
                                     .name(CommonUtil.ofNullable(() -> source.getName()).orElse(""))
                                     .tokenName(CommonUtil.ofNullable(() -> source.getTokenName()).orElse(""))
                                     .symbol(CommonUtil.ofNullable(() -> source.getSymbol()).orElse(""))
                                     .build();
    }

    public static QueryTokenIdDetailResp copy(CustomToken1155Inventory source) {
        return QueryTokenIdDetailResp.builder()
                                     .address(CommonUtil.ofNullable(() -> source.getOwner()).orElse(""))
                                     .contract(CommonUtil.ofNullable(() -> source.getTokenAddress()).orElse(""))
                                     .tokenId(CommonUtil.ofNullable(() -> source.getTokenId().toString()).orElse(""))
                                     .balance(CommonUtil.ofNullable(() -> source.getBalance().toString()).orElse(""))
                                     .image(CommonUtil.ofNullable(() -> source.getImage()).orElse(""))
                                     .txCount(CommonUtil.ofNullable(() -> source.getTokenTxQty()).orElse(0))
                                     .name(CommonUtil.ofNullable(() -> source.getName()).orElse(""))
                                     .tokenName(CommonUtil.ofNullable(() -> source.getTokenName()).orElse(""))
                                     .symbol(CommonUtil.ofNullable(() -> source.getSymbol()).orElse(""))
                                     .build();
    }

}
