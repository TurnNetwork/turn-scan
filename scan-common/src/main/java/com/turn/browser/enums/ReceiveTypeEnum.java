package com.turn.browser.enums;

/**
 * @Description:
 */
public enum ReceiveTypeEnum {
    CONTRACT(0, "contract"),
    ACCOUNT(1, "account");

    private int code;
    private String desc;

    ReceiveTypeEnum ( int code, String desc) {
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
