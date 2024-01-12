package com.turn.browser.enums;


/**
 * @Description:
 */
public enum GameEventTypeEnum {
    CREATE_GAME_EVENT("getCreateGameEvents"),
    JOIN_GAME_EVENT("getJoinGameEvents"),
    END_GAME_EVENT("getEndGameEvents");

    private String desc;

    GameEventTypeEnum(String desc) {
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
    public static GameEventTypeEnum getErcTypeEnum(String name) {
        for (GameEventTypeEnum e : GameEventTypeEnum.values()) {
            if (e.getDesc().equals(name)) {
                return e;
            }
        }
        return null;
    }

}