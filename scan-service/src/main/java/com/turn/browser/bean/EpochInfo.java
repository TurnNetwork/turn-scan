package com.turn.browser.bean;

import com.bubble.utils.Numeric;

import java.math.BigDecimal;

/**
 * Billing cycle information
 */
public class EpochInfo {

    /**
     * Block reward--Abandoned
     */
    private BigDecimal packageReward;

    /**
     *Settlement cycle pledge rewards--Abandoned
     */
    private BigDecimal stakingReward;

    /**
     *Current issuance cycle
     */
    private BigDecimal yearNum;

    /**
     * The starting block number of the current issuance cycle
     */
    private BigDecimal yearStartNum;

    /**
     * The end block number of the current issuance cycle
     */
    private BigDecimal yearEndNum;

    /**
     * Number of settlement cycles remaining in the current issuance cycle
     */
    private BigDecimal remainEpoch;

    /**
     *Average block time
     */
    private BigDecimal avgPackTime;

    /**
     * Block reward for the current settlement cycle
     */
    private BigDecimal curPackageReward;

    /**
     * Staking rewards for the current settlement cycle
     */
    private BigDecimal curStakingReward;

    /**
     * Block reward in the next settlement cycle
     */
    private BigDecimal nextPackageReward;

    /**
     * Staking rewards in the next settlement cycle
     */
    private BigDecimal nextStakingReward;

    public BigDecimal getPackageReward() {
        return packageReward;
    }

    public void setPackageReward(String packageReward) {
        this.packageReward = new BigDecimal(Numeric.decodeQuantity(packageReward));
    }

    public BigDecimal getStakingReward() {
        return stakingReward;
    }

    public void setStakingReward(String stakingReward) {
        this.stakingReward = new BigDecimal(Numeric.decodeQuantity(stakingReward));
    }

    public BigDecimal getYearNum() {
        return yearNum;
    }

    public void setYearNum(BigDecimal yearNum) {
        this.yearNum = yearNum;
    }

    public BigDecimal getYearStartNum() {
        return yearStartNum;
    }

    public void setYearStartNum(BigDecimal yearStartNum) {
        this.yearStartNum = yearStartNum;
    }

    public BigDecimal getYearEndNum() {
        return yearEndNum;
    }

    public void setYearEndNum(BigDecimal yearEndNum) {
        this.yearEndNum = yearEndNum;
    }

    public BigDecimal getRemainEpoch() {
        return remainEpoch;
    }

    public void setRemainEpoch(BigDecimal remainEpoch) {
        this.remainEpoch = remainEpoch;
    }

    public BigDecimal getAvgPackTime() {
        return avgPackTime;
    }

    public void setAvgPackTime(BigDecimal avgPackTime) {
        this.avgPackTime = avgPackTime;
    }

    public BigDecimal getCurPackageReward() {
        return curPackageReward;
    }

    public void setCurPackageReward(String curPackageReward) {
        this.curPackageReward = new BigDecimal(Numeric.decodeQuantity(curPackageReward));
    }

    public BigDecimal getCurStakingReward() {
        return curStakingReward;
    }

    public void setCurStakingReward(String curStakingReward) {
        this.curStakingReward = new BigDecimal(Numeric.decodeQuantity(curStakingReward));
    }

    public BigDecimal getNextPackageReward() {
        return nextPackageReward;
    }

    public void setNextPackageReward(String nextPackageReward) {
        this.nextPackageReward = new BigDecimal(Numeric.decodeQuantity(nextPackageReward));
    }

    public BigDecimal getNextStakingReward() {
        return nextStakingReward;
    }

    public void setNextStakingReward(String nextStakingReward) {
        this.nextStakingReward = new BigDecimal(Numeric.decodeQuantity(nextStakingReward));
    }

}