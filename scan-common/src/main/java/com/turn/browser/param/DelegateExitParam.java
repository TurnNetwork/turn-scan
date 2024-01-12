package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * txType=1005
 */
@Data
@Accessors(chain = true)
public class DelegateExitParam extends TxParam {

    /**
     * Represents the unique identifier of a certain pledge of a certain node
     */
    private BigInteger stakingBlockNum;

    /**
     * The pledged node ID (also called the candidate’s node ID)
     */
    private String nodeId;

    public DelegateExitParam setNodeId(String nodeId) {
        this.nodeId = HexUtil.prefix(nodeId);
        return this;
    }

    /**
     * Amount of holding reduction entrustment (based on the smallest unit, 1TURN = 10**18 AAA)
     */
    private BigDecimal amount;

    /**
     * The actual entrusted amount for holding reduction (based on the smallest unit, 1TURN = 10**18 AAA)
     */
    private BigDecimal realAmount;

    /**
     * The number of rewards corresponding to the commission amount (based on the smallest unit, 1TURN = 10**18 AAA)
     */
    private BigDecimal reward;

    /**
     * The name of the pledged node (with a length limit, indicating the name of the node)
     */
    private String nodeName;

    /**
     * status code
     */
    String decodedStatus;

    /**
     * Profit from entrustment (new fields for entrustment locking)
     */
    private BigDecimal delegateIncome;

    /**
     * The canceled commission fee will be returned to the user's balance (new field for commission lock)
     */
    private BigDecimal released;

    /**
     * The canceled commission fee will be returned to the user's lock account (new field for commission lock)
     */
    private BigDecimal restrictingPlan;

    /**
     * The canceled commission funds are transferred to the locking period and come from the balance (new field for commission locking)
     */
    private BigDecimal lockReleased;

    /**
     * The canceled commission funds are transferred to the lock-up period and come from the lock-up account (new field for commission lock-in)
     */
    private BigDecimal lockRestrictingPlan;

}
