package com.turn.browser.utils;


/**
 * Address tool class
 */
public class AddressUtil {

    /**
     * 0 address
     */
    public static final String TO_ADDR_ZERO = "0x0000000000000000000000000000000000000000";

    /**
     * Determine whether the address is 0 address
     *
     * @param addr
     * @return boolean
     * @date 2021/2/9
     */
    public static boolean isAddrZero(String addr) {
        if (TO_ADDR_ZERO.equalsIgnoreCase(addr)) {
            return true;
        }
        return false;
    }

    /**
     * Determine whether from or to is a 0 address, as long as one of them is a 0 address, it is true
     *
     * @param from
     * @param to
     * @return boolean
     * @date 2021/2/9
     */
    public static boolean isAddrZero(String from, String to) {
        return isAddrZero(from) || isAddrZero(to);
    }

}