package com.turn.browser.bean;

import com.bubble.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

/**
 * @Description:
 */
public class RestrictingBalance {

    private String account;

    private BigInteger freeBalance;

    private BigInteger lockBalance;

    private BigInteger pledgeBalance;

    /**
     * The commission locks the balance to be withdrawn
     */
    private BigInteger dlFreeBalance;

    /**
     * The locked portion of the entrusted lock to be withdrawn
     */
    private BigInteger dlRestrictingBalance;

    /**
     * List locked in delegate lock
     */
    private List<DlLock> dlLocks;

    public void setFreeBalance(String freeBalance) {
        this.freeBalance = Numeric.decodeQuantity(freeBalance);
    }

    public void setLockBalance(String lockBalance) {
        this.lockBalance = Numeric.decodeQuantity(lockBalance);
    }

    public void setPledgeBalance(String pledgeBalance) {
        this.pledgeBalance = Numeric.decodeQuantity(pledgeBalance);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigInteger getFreeBalance() {
        return freeBalance;
    }

    public BigInteger getLockBalance() {
        return lockBalance;
    }

    public BigInteger getPledgeBalance() {
        return pledgeBalance;
    }

    public BigInteger getDlFreeBalance() {
        return dlFreeBalance;
    }

    public void setDlFreeBalance(String dlFreeBalance) {
        this.dlFreeBalance = Numeric.decodeQuantity(dlFreeBalance);
    }

    public BigInteger getDlRestrictingBalance() {
        return dlRestrictingBalance;
    }

    public void setDlRestrictingBalance(String dlRestrictingBalance) {
        this.dlRestrictingBalance = Numeric.decodeQuantity(dlRestrictingBalance);
    }

    public List<DlLock> getDlLocks() {
        return dlLocks;
    }

    public void setDlLocks(List<DlLock> dlLocks) {
        this.dlLocks = dlLocks;
    }

}
