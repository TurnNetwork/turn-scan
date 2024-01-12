package com.turn.browser.bean;

import com.bubble.utils.Numeric;

import java.math.BigInteger;

/**
 * @Description:
 */
public class RestrictingBalance {
    private String account;
    private BigInteger freeBalance;
    private BigInteger lockBalance;
    private BigInteger pledgeBalance;

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

}
