package com.turn.browser.bean;

import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.elasticsearch.dto.Transaction;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CollectionEvent {

    private String traceId;

    // Block information
    private Block block;

    // transaction list
    private List<Transaction> transactions = new ArrayList<>();

    private EpochMessage epochMessage;

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

    public EpochMessage getEpochMessage() {
        return epochMessage;
    }

    public void setEpochMessage(EpochMessage epochMessage) {
        this.epochMessage = epochMessage;
    }

    /**
     * Release object reference
     */
    public void releaseRef() {
        block = null;
        transactions = null;
        epochMessage = null;
    }

}
