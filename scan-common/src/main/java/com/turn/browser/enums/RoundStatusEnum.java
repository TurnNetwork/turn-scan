package com.turn.browser.enums;

/**
 * @Description:
 */
public enum RoundStatusEnum {
    START(1, "start"),
    END(0, "end");

    private int code;
    private String desc;

    RoundStatusEnum(int code, String desc) {
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
