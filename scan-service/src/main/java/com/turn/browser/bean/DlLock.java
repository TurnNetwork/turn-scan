package com.turn.browser.bean;

import com.bubble.utils.Numeric;

import java.math.BigInteger;

/**
 * List locked in delegate lock
 */
public class DlLock {

    /**
     *Locked amount
     */
    private BigInteger lockBalance;

    /**
     *Amount of free money
     */
    private BigInteger freeBalance;

    /**
     * Unlock billing cycle
     */
    private BigInteger epoch;

    public BigInteger getLockBalance() {
        return lockBalance;
    }

    public void setLockBalance(String lockBalance) {
        this.lockBalance = Numeric.decodeQuantity(lockBalance);
    }

    public BigInteger getFreeBalance() {
        return freeBalance;
    }

    public void setFreeBalance(String freeBalance) {
        this.freeBalance = Numeric.decodeQuantity(freeBalance);
    }

    public BigInteger getEpoch() {
        return epoch;
    }

    public void setEpoch(BigInteger epoch) {
        this.epoch = epoch;
    }

}
