package com.turn.browser.bootstrap;

import com.bubble.protocol.core.methods.response.BubbleBlock;
import com.turn.browser.bean.ReceiptResult;
import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * Self-check event
 */
@Data
public class BootstrapEvent {

    private String traceId;

    // The Future of the current native block
    private CompletableFuture<BubbleBlock> blockCF;

    // Future of all transaction receipts in the current native block
    private CompletableFuture<ReceiptResult> receiptCF;

    // Callback after processing the block
    private Callback callback;

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

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * Release object reference
     */
    public void releaseRef() {
        blockCF = null;
        receiptCF = null;
        callback = null;
    }

}
