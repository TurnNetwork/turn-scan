package com.turn.browser.enums;

/**
 * @Description:
 */
public enum GameTypeEnum {
    UNKNOWN("unknown"),
    GAME("game");

    private String desc;

    GameTypeEnum(String desc) {
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
     * @date 2021/1/19
     */
    public static GameTypeEnum getErcTypeEnum(String name) {
        for (GameTypeEnum e : GameTypeEnum.values()) {
            if (e.getDesc().equals(name)) {
                return e;
            }
        }
        return GameTypeEnum.UNKNOWN;
    }

}