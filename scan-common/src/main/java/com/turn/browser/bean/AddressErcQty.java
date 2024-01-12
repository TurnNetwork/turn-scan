package com.turn.browser.bean;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AddressErcQty {

    /**
     * address
     */
    private String address;

    /**
     *Token erc20 transaction number
     */
    private long erc20TxQty;

    /**
     *Token erc721 transaction number
     */
    private long erc721TxQty;
    /**
     *Token erc1155 transaction number
     */
    private long erc1155TxQty;
}
