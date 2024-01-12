package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * tyType=1003
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class StakeExitParam extends TxParam{
    /**
     * The pledged node ID (also called the candidate’s node ID)
     */
    private String nodeId;
    public void setNodeId(String nodeId){
        this.nodeId= HexUtil.prefix(nodeId);
    }

    /**
     * The name of the pledged node (with a length limit, indicating the name of the node)
     */
    private String nodeName;

    /**
     * Pledge transaction speeds up
     */
    private BigInteger stakingBlockNum;

    /**
     *Cancellation amount
     */
    private BigDecimal amount;
    /**
     * The actual exit block number of the node (the block number where the deposit is refunded)
     */
    private BigInteger withdrawBlockNum;
}
