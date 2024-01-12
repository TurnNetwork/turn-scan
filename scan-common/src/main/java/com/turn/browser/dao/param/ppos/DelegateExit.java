package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
@Accessors(chain = true)
public class DelegateExit implements BusinessParam {
    @Data
    @Builder
    public static class DelegateBalance{
        //Current hesitation amount
        private BigDecimal delegateHes;
        //Current locked amount
        private BigDecimal delegateLocked;
        //Amount to be received
        private BigDecimal delegateReleased;
    }
    @Data
    @Builder
    public static class NodeDecrease{
        //Current hesitation amount
        private BigDecimal delegateHes;
        //Current locked amount
        private BigDecimal delegateLocked;
        //Amount to be received
        private BigDecimal delegateReleased;
    }
    //node id
    private String nodeId;
    //Real exit commission amount
    private BigDecimal realRefundAmount;
    //Node pledge is fast and high
    private BigInteger stakingBlockNumber;
    //Latest commission threshold
    private BigDecimal minimumThreshold;
    //Exchange at block height
    private BigInteger blockNumber;
    //transaction sender
    private String txFrom;
    //The remaining amount in the order record
    @Builder.Default
    private DelegateBalance balance= DelegateBalance.builder().build();
    @Builder.Default
    private NodeDecrease decrease= NodeDecrease.builder().build();
    //Is the current history
    private int codeIsHistory;
    //Whether the node exits
    private boolean codeNodeIsLeave;
    //Rewards obtained when all exit
    private BigDecimal delegateReward;


    @Override
    public BusinessType getBusinessType() {
        return BusinessType.DELEGATE_EXIT;
    }
}
