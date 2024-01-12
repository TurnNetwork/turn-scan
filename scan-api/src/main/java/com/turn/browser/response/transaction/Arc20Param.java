package com.turn.browser.response.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class Arc20Param {

    /**
     *Internal transactions from
     */
    private String innerFrom;

    /**
     * Internal transaction from type
     */
    private Integer fromType;

    /**
     *Internal transactions to
     */
    private String innerTo;

    /**
     * Internal transaction to type
     */
    private Integer toType;

    /**
     *Internal transaction value
     */
    private String innerValue;

    /**
     * Corresponding address for internal transactions
     */
    private String innerContractAddr;

    /**
     * Corresponding name of internal transactions
     */
    private String innerContractName;

    /**
     *Corresponding unit for internal transactions
     */
    private String innerSymbol;

    /**
     *Contract accuracy
     */
    private String innerDecimal;

}
