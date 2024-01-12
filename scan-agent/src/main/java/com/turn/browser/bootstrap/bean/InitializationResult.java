package com.turn.browser.bootstrap.bean;

/**
 * @description: Initialization result
 **/

public class InitializationResult {
    // Block number that has been collected
    private Long collectedBlockNumber;

    public Long getCollectedBlockNumber () {
        return collectedBlockNumber;
    }

    public void setCollectedBlockNumber ( Long collectedBlockNumber ) {
        this.collectedBlockNumber = collectedBlockNumber;
    }
}
