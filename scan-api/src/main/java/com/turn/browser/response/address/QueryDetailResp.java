package com.turn.browser.response.address;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.turn.browser.bean.LockDelegate;
import com.turn.browser.config.json.CustomLatSerializer;

/**
 * The object returned by the query address
 */
public class QueryDetailResp {

    /**
     * Address details 1: Account 2: Built-in contract 3: EVM contract 4: WASM
     */
    private Integer type;

    /**
     * Balance (unit: turn)
     */
    private BigDecimal balance;

    /**
     * Locked balance (unit: turn)
     */
    private BigDecimal restrictingBalance;

    /**
     * Amount pledged
     */
    private BigDecimal stakingValue;

    /**
     * The amount entrusted
     */
    private BigDecimal delegateValue;

    /**
     * Amount in redemption
     */
    private BigDecimal redeemedValue;

    /**
     * Total number of transactions
     */
    private Integer txQty;

    /**
     * whether is erc20
     */
    private boolean hasErc20 = false;

    /**
     * Total number of token erc20 transactions
     */
    private Integer erc20TxQty;

    /**
     * whether is erc721
     */
    private boolean hasErc721 = false;

    /**
     * Total number of token erc721 transactions
     */
    private Integer erc721TxQty;

    /**
     * whether is erc1155
     */
    private boolean hasErc1155 = false;

    /**
     * Total number of token erc1155 transactions
     */
    private Integer erc1155TxQty;

    /**
     * Total number of transfer transactions
     */
    private Integer transferQty;

    /**
     * Total number of entrusted transactions
     */
    private Integer delegateQty;

    /**
     * Total number of validator transactions
     */
    private Integer stakingQty;

    /**
     * Total number of governance transactions
     */
    private Integer proposalQty;

    /**
     * Delegated validator
     */
    private Integer candidateCount;

    /**
     * Unlocked delegate（turn）
     */
    private BigDecimal delegateHes;

    /**
     * Locked delegate（turn）
     */
    private BigDecimal delegateLocked;

    /**
     * unLock delegate（turn）
     */
    private BigDecimal delegateUnlock;

    /**
     * unReleased delegate（turn）
     */
    private BigDecimal delegateReleased;

    /**
     * Claim delegate（turn）
     */
    private BigDecimal delegateClaim;

    /**
     * withdrawn delegate（turn）
     */
    private BigDecimal haveReward;

    /**
     * contract Name
     */
    private String contractName;

    /**
     * Contract creator address
     */
    private String contractCreate;

    /**
     * Contract creation hash
     */
    private String contractCreateHash;

    /**
     * contract Bin
     */
    private String contractBin;

    /**
     * Whether to lock
     */
    private Integer isRestricting;

    /**
     * Whether to Destroy
     */
    private Integer isDestroy;

    /**
     * Contract destruction hash
     */
    private String destroyHash;

    /**
     * token Symbol
     */
    private String tokenSymbol;

    /**
     * token Name
     */
    private String tokenName;

    /**
     * Delegate freezing plan
     */
    private List<LockDelegate> lockDelegateList;

    /**
     * Unfrozen entrust amount/entrust to be withdrawn (turn)
     */
    private String unLockBalance;

    /**
     * Unfrozen commission amount/entrust to be redeemed(turn)
     */
    private String lockBalance;

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getRestrictingBalance() {
        return this.restrictingBalance;
    }

    public void setRestrictingBalance(BigDecimal restrictingBalance) {
        this.restrictingBalance = restrictingBalance;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingValue() {
        return this.stakingValue;
    }

    public void setStakingValue(BigDecimal stakingValue) {
        this.stakingValue = stakingValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateValue() {
        return this.delegateValue;
    }

    public void setDelegateValue(BigDecimal delegateValue) {
        this.delegateValue = delegateValue;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getRedeemedValue() {
        return this.redeemedValue;
    }

    public void setRedeemedValue(BigDecimal redeemedValue) {
        this.redeemedValue = redeemedValue;
    }

    public Integer getTxQty() {
        return this.txQty;
    }

    public void setTxQty(Integer txQty) {
        this.txQty = txQty;
    }

    public Integer getTransferQty() {
        return this.transferQty;
    }

    public void setTransferQty(Integer transferQty) {
        this.transferQty = transferQty;
    }

    public Integer getDelegateQty() {
        return this.delegateQty;
    }

    public void setDelegateQty(Integer delegateQty) {
        this.delegateQty = delegateQty;
    }

    public Integer getStakingQty() {
        return this.stakingQty;
    }

    public void setStakingQty(Integer stakingQty) {
        this.stakingQty = stakingQty;
    }

    public Integer getProposalQty() {
        return this.proposalQty;
    }

    public void setProposalQty(Integer proposalQty) {
        this.proposalQty = proposalQty;
    }

    public Integer getCandidateCount() {
        return this.candidateCount;
    }

    public void setCandidateCount(Integer candidateCount) {
        this.candidateCount = candidateCount;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateHes() {
        return this.delegateHes;
    }

    public void setDelegateHes(BigDecimal delegateHes) {
        this.delegateHes = delegateHes;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateLocked() {
        return this.delegateLocked;
    }

    public void setDelegateLocked(BigDecimal delegateLocked) {
        this.delegateLocked = delegateLocked;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateUnlock() {
        return this.delegateUnlock;
    }

    public void setDelegateUnlock(BigDecimal delegateUnlock) {
        this.delegateUnlock = delegateUnlock;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateReleased() {
        return this.delegateReleased;
    }

    public void setDelegateReleased(BigDecimal delegateReleased) {
        this.delegateReleased = delegateReleased;
    }

    public String getContractName() {
        return this.contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getContractCreate() {
        return this.contractCreate;
    }

    public void setContractCreate(String contractCreate) {
        this.contractCreate = contractCreate;
    }

    public String getContractCreateHash() {
        return this.contractCreateHash;
    }

    public void setContractCreateHash(String contractCreateHash) {
        this.contractCreateHash = contractCreateHash;
    }

    public Integer getIsRestricting() {
        return this.isRestricting;
    }

    public void setIsRestricting(Integer isRestricting) {
        this.isRestricting = isRestricting;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getDelegateClaim() {
        return this.delegateClaim;
    }

    public void setDelegateClaim(BigDecimal delegateClaim) {
        this.delegateClaim = delegateClaim;
    }

    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getHaveReward() {
        return this.haveReward;
    }

    public void setHaveReward(BigDecimal haveReward) {
        this.haveReward = haveReward;
    }

    public String getContractBin() {
        return this.contractBin;
    }

    public void setContractBin(String contractBin) {
        this.contractBin = contractBin;
    }

    public Integer getIsDestroy() {
        return this.isDestroy;
    }

    public void setIsDestroy(Integer isDestroy) {
        this.isDestroy = isDestroy;
    }

    public String getDestroyHash() {
        return this.destroyHash;
    }

    public void setDestroyHash(String destroyHash) {
        this.destroyHash = destroyHash;
    }

    public Integer getErc20TxQty() {
        return erc20TxQty;
    }

    public void setErc20TxQty(Integer erc20TxQty) {
        this.erc20TxQty = erc20TxQty;
    }

    public Integer getErc721TxQty() {
        return erc721TxQty;
    }

    public void setErc721TxQty(Integer erc721TxQty) {
        this.erc721TxQty = erc721TxQty;
    }

    public boolean isHasErc20() {
        return hasErc20;
    }

    public void setHasErc20(boolean hasErc20) {
        this.hasErc20 = hasErc20;
    }

    public boolean isHasErc721() {
        return hasErc721;
    }

    public void setHasErc721(boolean hasErc721) {
        this.hasErc721 = hasErc721;
    }

    public boolean isHasErc1155() {
        return hasErc1155;
    }

    public void setHasErc1155(boolean hasErc1155) {
        this.hasErc1155 = hasErc1155;
    }

    public Integer getErc1155TxQty() {
        return erc1155TxQty;
    }

    public void setErc1155TxQty(Integer erc1155TxQty) {
        this.erc1155TxQty = erc1155TxQty;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public List<LockDelegate> getLockDelegateList() {
        return lockDelegateList;
    }

    public void setLockDelegateList(List<LockDelegate> lockDelegateList) {
        this.lockDelegateList = lockDelegateList;
    }

    public String getUnLockBalance() {
        return unLockBalance;
    }

    public void setUnLockBalance(String unLockBalance) {
        this.unLockBalance = unLockBalance;
    }

    public String getLockBalance() {
        return lockBalance;
    }

    public void setLockBalance(String lockBalance) {
        this.lockBalance = lockBalance;
    }

}
