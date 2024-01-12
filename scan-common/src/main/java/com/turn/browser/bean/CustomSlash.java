package com.turn.browser.bean;

import com.turn.browser.dao.entity.Slash;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Penalty entity expansion class
 */
@Data
public class CustomSlash extends Slash {

    public CustomSlash(){
        super();
        Date date = new Date();
        this.setCreateTime(date);
        this.setUpdateTime(date);
    }

    /**
     * Report success or failure type enumeration class:
     * 1. Success
     * 2.Failure
     */
    public enum StatusEnum{
        FAILURE(1, "Failure"),
        SUCCESS(2, "Success")
        ;
        private int code;
        private String desc;
        StatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode(){return code;}
        public String getDesc(){return desc;}
        private static final Map <Integer, StatusEnum> ENUMS = new HashMap <>();
        static {
            Arrays.asList(StatusEnum.values()).forEach(en->ENUMS.put(en.code,en));}
        public static StatusEnum getEnum( Integer code){
            return ENUMS.get(code);
        }
        public static boolean contains(int code){return ENUMS.containsKey(code);}
        public static boolean contains(StatusEnum en){return ENUMS.containsValue(en);}
    }

    /**
     * Whether to exit the type enumeration class:
     * 1.Yes
     * 2.No
     */

    public enum YesNoEnum{
        YES(1, "Yes"),
        NO(2, "No")
        ;
        private int code;
        private String desc;
        YesNoEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode(){return code;}
        public String getDesc(){return desc;}
        private static final Map<Integer, CustomSlash.YesNoEnum> ENUMS = new HashMap<>();
        static {Arrays.asList(CustomSlash.YesNoEnum.values()).forEach(en->ENUMS.put(en.code,en));}
        public static CustomSlash.YesNoEnum getEnum( Integer code){
            return ENUMS.get(code);
        }
        public static boolean contains(int code){return ENUMS.containsKey(code);}
        public static boolean contains( CustomSlash.YesNoEnum en){return ENUMS.containsValue(en);}
    }
}
