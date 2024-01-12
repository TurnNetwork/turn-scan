package com.turn.browser.enums;

/**
 * Business type enumeration
 */
public enum BusinessType {
    // Pledge: create, edit, increase holdings, exit
    STAKE_CREATE,STAKE_MODIFY,STAKE_INCREASE,STAKE_EXIT,
    // Delegation: delegation, cancellation
    DELEGATE_CREATE,DELEGATE_EXIT,
    // Proposal: text, version, upgrade, vote, cancellation, parameters
    PROPOSAL_TEXT,PROPOSAL_VERSION,PROPOSAL_UPGRADE,PROPOSAL_VOTE,PROPOSAL_CANCEL,PROPOSAL_PARAMETER,PROPOSAL_SLASH,
    // Penalty: Report
    REPORT,
    // version statement
    VERSION_DECLARE,
    //Create a lock-up plan
    RESTRICTING_CREATE,
    // new block
    NEW_BLOCK,
    // Election, consensus cycle, settlement cycle
    ELECTION_EPOCH,CONSENSUS_EPOCH,SETTLE_EPOCH,
    // Network statistics, address statistics
    NETWORK_STATISTIC, ADDRESS_STATISTIC,
    // Receive award
    CLAIM_REWARD,
    //Contract creation
    CONTRACT_CREATE,
    //Contract execution
    CONTRACT_EXEC
}
