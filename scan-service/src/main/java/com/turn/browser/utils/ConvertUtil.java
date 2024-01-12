package com.turn.browser.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Data conversion tools
 */
public class ConvertUtil {

    private ConvertUtil() {
    }

    public static BigInteger hexToBigInteger(String hexStr) {
        hexStr = hexStr.replace("0x", "");
        return new BigInteger(hexStr, 16);
    }

    public static String captureName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        char[] cs = name.toCharArray();
        if (cs[0] >= 'a' && cs[0] <= 'z') {
            cs[0] = (char) (cs[0] - 32);
        }
        return String.valueOf(cs);

    }

    /**
     * Precision conversion
     *
     * @param value original value
     * @param factor precision (how many 0s)
     */
    public static BigDecimal convertByFactor(BigDecimal value, int factor) {
        return new BigDecimal(EnergonUtil.format(value.divide(BigDecimal.TEN.pow(factor)).setScale(12, BigDecimal.ROUND_DOWN), 12));
    }

}
