package com.turn.browser.enums;

/**
 * @Description:
 */
public enum ErcTypeEnum {
    UNKNOWN("unknown"),
    ERC20("erc20"),
    ERC721("erc721"),
    ERC1155("erc1155");

    private String desc;

    ErcTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * Get enumeration by name
     *
     * @param name
     * @return com.turn.browser.v0151.enums.ErcTypeEnum
     */
    public static ErcTypeEnum getErcTypeEnum(String name) {
        for (ErcTypeEnum e : ErcTypeEnum.values()) {
            if (e.getDesc().equals(name)) {
                return e;
            }
        }
        return ErcTypeEnum.UNKNOWN;
    }

}