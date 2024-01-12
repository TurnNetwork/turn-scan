package com.turn.browser.enums;

/**
 * Transaction status enumeration
 */
public enum TransactionStatusEnum {

    /** Transaction status definition */

    SUCCESS(1,"success"),
    FAIL(0,"failure");

    /** describe */
    private String name;
    /** error code */
    private int code;

    TransactionStatusEnum(int code, String name){
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public int getCode() {
        return code;
    }

    public static TransactionStatusEnum getEnumByCodeValue(int code){
        TransactionStatusEnum[] allEnums = values();
        for(TransactionStatusEnum enableStatus : allEnums){
            if(enableStatus.getCode()==code)
                return enableStatus;
        }
        return null;
    }
}
