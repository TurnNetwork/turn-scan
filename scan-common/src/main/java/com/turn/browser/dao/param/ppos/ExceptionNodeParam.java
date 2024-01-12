package com.turn.browser.dao.param.ppos;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;

@Data
@Builder
@Accessors(chain = true)
public class ExceptionNodeParam {
    //NodeId
    private String nodeId;
    //The pledge exchange is at block height
    private BigInteger stakingBlockNum;
}
