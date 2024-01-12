package com.turn.browser.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class RedeemDelegationParm extends TxParam {

    /**
     * status code
     */
    private String status;

    /**
     * The commission fee successfully received will be returned to the balance
     */
    private BigDecimal released;

    /**
     * The commission fee successfully received will be returned to the lock account
     */
    private BigDecimal restrictingPlan;

    /**
     * Successfully received commission funds will be returned to the balance + back to the lock account
     */
    private BigDecimal value;
}
