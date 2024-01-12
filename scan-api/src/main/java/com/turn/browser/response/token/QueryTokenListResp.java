package com.turn.browser.response.token;

import com.turn.browser.bean.CustomToken;
import com.turn.browser.utils.ConvertUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Contract list data response message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QueryTokenListResp {

    private String address;

    private String type;

    private String name;

    private String symbol;

    private Integer decimal;

    private BigDecimal totalSupply;

    private String icon;

    private String webSite;

    private String details;

    private Date createTime;

    private Integer holder;

    public static QueryTokenListResp fromToken(CustomToken token) {
        return QueryTokenListResp.builder()
                .address(token.getAddress()).name(token.getName()).type(token.getType())
                .symbol(token.getSymbol()).decimal(token.getDecimal())
                .totalSupply(ConvertUtil.convertByFactor(token.getTotalSupply() == null ? BigDecimal.ZERO : new BigDecimal(token.getTotalSupply()), token.getDecimal() == null ? 0 : token.getDecimal()))
                .createTime(token.getCreateTime())
                .details(token.getDetails()).holder(token.getHolder())
                .build();
    }

}
