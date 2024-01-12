package com.turn.browser.bean;

import java.math.BigDecimal;

/**
 * @Description: profit and cost beans
 */
public class PeriodValueElement {
    private Long period;
    private BigDecimal value;

    public Long getPeriod() {
        return period;
    }

    public PeriodValueElement setPeriod(Long period) {
        this.period = period;
        return this;
    }

    public BigDecimal getValue() {
        return value;
    }

    public PeriodValueElement setValue(BigDecimal value) {
        this.value = value;
        return this;
    }
}
