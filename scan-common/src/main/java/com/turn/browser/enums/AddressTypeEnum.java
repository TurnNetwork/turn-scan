package com.turn.browser.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum AddressTypeEnum {
    ACCOUNT(1, "Account"),
    INNER_CONTRACT(2, "Internal Contract"),
    EVM_CONTRACT(3, "EVM Contract"),
    WASM_CONTRACT(4, "WASM Contract"),
    ERC20_EVM_CONTRACT(5, "ERC Contract"),
    ERC721_EVM_CONTRACT(6, "ERC Contract"),
    ERC1155_EVM_CONTRACT(7, "ERC Contract"),
    GAME_EVM_CONTRACT(8, "Game Contract");

    private int code;
    private String desc;

    AddressTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }

    private static final Map<Integer, AddressTypeEnum> ENUMS = new HashMap<>();
    static {
        Arrays.asList(AddressTypeEnum.values()).forEach(en -> ENUMS.put(en.code, en));
    }

    public static AddressTypeEnum getEnum(Integer code) {
        return ENUMS.get(code);
    }

    public static boolean contains(int code) {
        return ENUMS.containsKey(code);
    }

    public static boolean contains(AddressTypeEnum en) {
        return ENUMS.containsValue(en);
    }
}
