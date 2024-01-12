package com.turn.browser.bean;

import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import com.turn.browser.elasticsearch.dto.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class ComplementEvent {

    private String traceId;

    // Block information
    private Block block;

    // transaction list
    private List<Transaction> transactions;

    // node operation list
    private List<NodeOpt> nodeOpts;

    // reward list
    private List<DelegationReward> delegationRewards;

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<NodeOpt> getNodeOpts() {
        return nodeOpts;
    }

    public void setNodeOpts(List<NodeOpt> nodeOpts) {
        this.nodeOpts = nodeOpts;
    }

    public List<DelegationReward> getDelegationRewards() {
        return delegationRewards;
    }

    public void setDelegationRewards(List<DelegationReward> delegationRewards) {
        this.delegationRewards = delegationRewards;
    }

    /**
     * Release object reference
     */
    public void releaseRef() {
        block = null;
        transactions = null;
        nodeOpts = null;
        delegationRewards = null;
    }

}
