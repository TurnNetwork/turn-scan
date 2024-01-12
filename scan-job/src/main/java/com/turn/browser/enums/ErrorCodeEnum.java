/*
 * Copyright (c) 2018. juzhen.io. All rights reserved.
 */

package com.turn.browser.enums;

/**
 * 错误枚举统计
 */
public enum ErrorCodeEnum {

    DEFAULT(-1, "System Error"),
    PARAM_VALID_ERROR(1, "Request parameter error"),
    SYSTEM_CONFIG_ERROR(2, "System configuration error"),
    REPEAT_SUBMIT (3,"Repeated submission"),
    RECORD_NOT_EXIST(4,"Record does not exist"),
    RECORD_DELETED(5,"The id record has been deleted"),
    BLOCKCHAIN_ERROR(6,"Synchronization chain information exception"),
    TX_ERROR(7,"Exception in synchronized transaction information"),
    PENDINGTX_ERROR(8,"Synchronous pending transaction exception"),
    PENDINGTX_REPEAT(9,"pengding transaction update exception"),
    NODE_ERROR(10,"Synchronization node information exception"),
    STOMP_ERROR(11,"Push statistics abnormal");


	
    private int code;
    private String desc;

    ErrorCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
