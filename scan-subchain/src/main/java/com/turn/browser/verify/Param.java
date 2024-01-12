package com.turn.browser.verify;

import java.io.Serializable;

public interface Param extends Serializable {

    /**
     * Get version number
     * @return Return version number
     */
    String fatchVersion();

    /**
     * Get business parameters
     * @return Return business parameters
     */
    String fatchData();

    /**
     * Get timestamp
     * @return returns timestamp
     */
    String fatchTimestamp();

    /**
     * Get signature string
     * @return return signature string
     */
    String fatchSign();

    /**
     * Get the formatting type
     * @return Return formatting type
     */
    String fatchFormat();

}