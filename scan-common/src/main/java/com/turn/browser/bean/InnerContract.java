package com.turn.browser.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class InnerContract {

    /**
     * Corresponding enumeration ContractDescEnum name
     */
    private String contractDescEnumName;

    private String contractName;

    private String creator;

    private String contractHash;

}
