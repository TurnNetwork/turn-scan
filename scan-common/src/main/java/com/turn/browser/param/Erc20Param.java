package com.turn.browser.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class Erc20Param extends TxParam{
    private String innerFrom; //internal transaction from
    private String innerTo; //internal transaction to
    private String innerValue; //internal transaction value
    private String innerContractAddr; //Internal transaction corresponding address
    private String innerContractName; //Corresponding name of internal transaction
    private String innerSymbol; //Internal transaction corresponding unit
    private String innerDecimal; // Contract precision
}
