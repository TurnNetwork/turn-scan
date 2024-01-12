package com.turn.browser.bean;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AddressQty {

    /**
     * address
     */
    private String address;

    /**
     *Total number of transactions
     */
    private long txQty;

    /**
     *Total number of transfer transactions
     */
    private long transferQty;

    /**
     *Total number of entrusted transactions
     */
    private long delegateQty;

    /**
     *Total number of pledge transactions
     */
    private long stakingQty;

    /**
     *Total number of governance transactions
     */
    private long proposalQty;

}
