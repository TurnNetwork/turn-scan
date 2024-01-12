package com.turn.browser.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum InternalAddressType {
    FUND_ACCOUNT(0, "Foundation Account"),
    RESTRICT_CONTRACT(1, "Lock contract address"),
    STAKE_CONTRACT(2, "Pledge Contract"),
    INCENTIVE_CONTRACT(3, "Incentive Pool Contract"),
    DELEGATE_CONTRACT(6, "Delegation Reward Pool Contract"),
    OTHER(100, "Other");

    private int code;
    private String desc;

    InternalAddressType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }

    private static final Map<Integer, InternalAddressType> ENUMS = new HashMap<>();
    static {
        Arrays.asList(InternalAddressType.values()).forEach(en -> ENUMS.put(en.code, en));
    }

    public static InternalAddressType getEnum(Integer code) {
        return ENUMS.get(code);
    }

    public static boolean contains(int code) {
        return ENUMS.containsKey(code);
    }

    public static boolean contains(InternalAddressType en) {
        return ENUMS.containsValue(en);
    }
}
