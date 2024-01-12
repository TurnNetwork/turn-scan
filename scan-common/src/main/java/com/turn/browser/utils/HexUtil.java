package com.turn.browser.utils;

/**
 * @Description:
 */
public class HexUtil {
    private HexUtil(){}
    /**
     * Add "0x" prefix to hex string
     * @param hexVal
     * @return
     */
    public static String prefix(String hexVal){
        if(hexVal.startsWith("0x")) return hexVal;
        return "0x"+hexVal;
    }
    
    /**
     * concatenate strings later
     * @method String val
     * @param val
     * @return
     */
    public static String append(String val){
    	StringBuffer sBuffer = new StringBuffer("\t");
        sBuffer.append(val);
        return sBuffer.append("\t").toString();
    }
}
