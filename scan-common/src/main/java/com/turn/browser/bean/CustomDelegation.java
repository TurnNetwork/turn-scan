package com.turn.browser.bean;

import com.turn.browser.dao.entity.Delegation;
import lombok.Data;

import java.util.*;

/**
 * @Description: Delegate entity extension class
 */
@Data
public class CustomDelegation extends Delegation {

    public CustomDelegation() {
        super();
        Date date = new Date();
        this.setCreateTime(date);
        this.setUpdateTime(date);
    }

    /**
     * Whether the delegate is a historical type enumeration class:
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
        private static final Map <Integer, YesNoEnum> ENUMS = new HashMap <>();
        static {Arrays.asList(YesNoEnum.values()).forEach(en->ENUMS.put(en.code,en));}
        public static YesNoEnum getEnum( Integer code){
            return ENUMS.get(code);
        }
        public static boolean contains(int code){return ENUMS.containsKey(code);}
        public static boolean contains( YesNoEnum en){return ENUMS.containsValue(en);}
    }
}
