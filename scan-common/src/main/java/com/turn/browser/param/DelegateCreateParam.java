package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * tyType=1004
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class DelegateCreateParam extends TxParam{
    /**
     * Indicates whether to use the free amount of the account or the locked amount of the account for pledge
     * 0: Free amount
     * 1: Locked amount
     */
    private Integer type;

    /**
     * The pledged node ID (also called the candidate’s node ID)
     */
    private String nodeId;
    public void setNodeId(String nodeId){
        this.nodeId= HexUtil.prefix(nodeId);
    }

    /**
     * Amount of entrustment (based on the smallest unit, 1TURN = 10**18 AAA)
     */
    private BigDecimal amount;

    /**
     * The name of the pledged node (with a length limit, indicating the name of the node)
     */
    private String nodeName;

    /**
     * Pledge transaction speeds up
     */
    private BigInteger stakingBlockNum;
}
