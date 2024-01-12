package com.turn.browser.bootstrap;

import lombok.extern.slf4j.Slf4j;

/**
 * @description: Close queue callback
 **/
@Slf4j
public class ShutdownCallback implements Callback {
    private boolean isDone;
    private long endBlockNum;
    public void call(long handledBlockNum){
        log.info("Block ({}) synchronization completed!",handledBlockNum);
        if(handledBlockNum==endBlockNum) {
            isDone=true;
            log.info("All blocks are synchronized!");
        }
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public long getEndBlockNum() {
        return endBlockNum;
    }

    public void setEndBlockNum(long endBlockNum) {
        this.endBlockNum = endBlockNum;
    }
}
