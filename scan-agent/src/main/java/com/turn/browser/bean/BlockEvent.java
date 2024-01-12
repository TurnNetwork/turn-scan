package com.turn.browser.bean;

import com.bubble.protocol.core.methods.response.BubbleBlock;
import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * Block collection event
 */
@Data
public class BlockEvent {

    private String traceId;

    // The Future of the current native block
    private CompletableFuture<BubbleBlock> blockCF;

    // Future of all transaction receipts in the current native block
    private CompletableFuture<ReceiptResult> receiptCF;

    // All event information related to the current block (consensus cycle switching event/settlement cycle switching event/additional issuance cycle switching event)
    private EpochMessage epochMessage;

    public CompletableFuture<BubbleBlock> getBlockCF() {
        return blockCF;
    }

    public void setBlockCF(CompletableFuture<BubbleBlock> blockCF) {
        this.blockCF = blockCF;
    }

    public CompletableFuture<ReceiptResult> getReceiptCF() {
        return receiptCF;
    }

    public void setReceiptCF(CompletableFuture<ReceiptResult> receiptCF) {
        this.receiptCF = receiptCF;
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
        blockCF = null;
        receiptCF = null;
        epochMessage = null;
    }

}
