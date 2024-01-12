package com.turn.browser.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CreateStakeParam extends TxParam {

    private String nodeId;

    private BigInteger amount;

    private String beneficiary;

    private String name;

    private String details;

    private String electronURI;

    private String rpcUri;

    private String p2pURI;

    private String version;

    private Integer isOperator;

}
