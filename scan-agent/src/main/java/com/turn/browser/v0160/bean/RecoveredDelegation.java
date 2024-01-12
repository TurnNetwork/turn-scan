package com.turn.browser.v0160.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecoveredDelegation {

    private String address;

    private BigInteger delegationAmount;

}
