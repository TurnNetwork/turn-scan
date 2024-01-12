
package com.turn.browser.param.evidence;

import lombok.Data;

@Data
public class Prepare {

    private int epoch;
    private int viewNumber;
    private String blockHash;
    private int blockNumber;
    private int blockIndex;
    private ValidateNode validateNode;
    private String signature;


}