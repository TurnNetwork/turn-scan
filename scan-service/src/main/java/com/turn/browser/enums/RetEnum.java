package com.turn.browser.enums;

/**
 * Business initialization enumeration
 */
public enum RetEnum {

    /** Business error code definition */

    RET_SUCCESS(0,"success"),
    RET_PARAM_VALLID(-1,"The request parameter is invalid"),
    RET_SYS_EXCEPTION(-100,"System exception"),
    RET_FAIL(1,"Failure");

    /** describe */
    private String name;
    /** error code */
    private int code;

    RetEnum(int code, String name){
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public int getCode() {
        return code;
    }

    public static RetEnum getEnumByCodeValue(int code){
        RetEnum[] allEnums = values();
        for(RetEnum enableStatus : allEnums){
            if(enableStatus.getCode()==code)
                return enableStatus;
        }
        return null;
    }
}
