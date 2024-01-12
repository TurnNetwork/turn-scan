package com.turn.browser.response.token;

import cn.hutool.core.util.StrUtil;
import com.turn.browser.dao.entity.TokenInventory;
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
public class QueryTokenIdListResp {

    private String address;

    private String contract;

    private String tokenId;

    private String image;

    private Integer txCount;

    private String balance;

    public static QueryTokenIdListResp fromToken(TokenInventory token) {
        // 默认取中等缩略图
        String image = "";
        if (StrUtil.isNotEmpty(token.getMediumImage())) {
            image = token.getMediumImage();
        } else {
            image = token.getImage();
        }
        return QueryTokenIdListResp.builder()
                                   .address(token.getOwner()).contract(token.getTokenAddress())
                                   .tokenId(token.getTokenId()).image(image)
                                   .txCount(token.getTokenTxQty())
                                   .build();
    }

}
