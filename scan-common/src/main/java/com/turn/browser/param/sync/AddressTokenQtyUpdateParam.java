package com.turn.browser.param.sync;

import lombok.Data;

/**
 * Address table tokenQty update parameters
 */
@Data
public class AddressTokenQtyUpdateParam {
    private String address;
    private Integer tokenQty;
}
