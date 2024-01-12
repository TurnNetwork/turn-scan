package com.turn.browser.bean;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenQty {

    /**
     *Contract address
     */
    private String contract;

    /**
     * Total number of token transactions = number of erc20 transactions + number of erc721 transactions
     */
    private long tokenTxQty;

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
