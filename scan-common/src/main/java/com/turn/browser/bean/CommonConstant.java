package com.turn.browser.bean;

/**
 * 公共常数
 */
public class CommonConstant {

    /**
     * Link ID name
     */
    public static final String TRACE_ID = "trace-id";

    /**
     *0.16.0 version number
     */
    public static final String V0160_VERSION = "0.16.0";

    /**
     * alaya mainnet chain id
     */
    public static final long ALAYA_CHAIN_ID = 201018;

    /**
     * Request parameter link ID name
     */
    public static final String REQ_TRACE_ID = "traceId";

    /**
     * How many settlement periods are used for the calculation of the 24-hour block rate?
     */
    public static final int BLOCK_RATE_SETTLE_EPOCH_NUM = 7;

    /**
     * Statistics of APR for 8 settlement cycles
     */
    public static final int BLOCK_APR_EPOCH_NUM = 8;

    /**
     * Number of retries, would rather lose data than hinder the main process of chasing blocks
     */
    public static final int reTryNum = 3;

}
