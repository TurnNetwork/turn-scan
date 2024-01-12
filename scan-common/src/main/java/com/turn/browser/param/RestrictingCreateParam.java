package com.turn.browser.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * txType=4000
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class RestrictingCreateParam extends TxParam{

    /**
     * txType=4000
     */
    @Data
    @Builder
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class RestrictingPlan {
        /**
         * Indicates the multiple of the settlement period. The product of the number of blocks produced in each settlement cycle represents the release of locked funds at the target block height. Epoch * The number of blocks per cycle must be at least greater than the maximum irreversible block height
         */
        private BigInteger epoch;

        /**
         * Indicates the amount to be released on the target block
         */
        private BigDecimal amount;
    }

    /**
     * Lock the position and release it to the account
     */
    private String account;

    /**
     * Detailed plan for locking up positions
     */
    private List<RestrictingPlan> plans;
}
