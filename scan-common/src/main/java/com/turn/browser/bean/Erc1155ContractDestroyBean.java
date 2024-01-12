package com.turn.browser.bean;

import lombok.Data;

@Data
public class Erc1155ContractDestroyBean {

    /**
     *Contract address
     */
    private String tokenAddress;

    /**
     *Contract id
     */
    private String tokenId;

    /**
     *Contract address
     */
    private String address;

    /**
     * Destroyed block height
     */
    private Long contractDestroyBlock;

}
