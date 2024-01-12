package com.turn.browser.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Amount conversion tools
 */
public class EnergonUtil {
    private EnergonUtil(){}
    /**Default precision number */
    private static final int DEFAULT_SHARP_NUM = 8;
    public static String format(Object number){
        return format(number,DEFAULT_SHARP_NUM);
    }
    /**
     * Amount conversion
     * @method format
     * @param number
     * @param sharpNum
     * @return
     */
    public static String format(Object number,Integer sharpNum){
        if(!(number instanceof Number)) throw new NumberFormatException("The param is not a Number!");
        DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
        StringBuilder pattern = new StringBuilder(".");
        for(int i=0;i<sharpNum;i++) pattern.append("#");
        nf.applyPattern(pattern.toString());
        String result = nf.format(number);
        if(".0".equals(result)) {
            return "0";
        }else
        if(result.endsWith(".0")) {
            return result.replace(".0","");
        } else
        if(result.startsWith(".")) {
            return "0"+result;
        }
        return result;
    }


}
