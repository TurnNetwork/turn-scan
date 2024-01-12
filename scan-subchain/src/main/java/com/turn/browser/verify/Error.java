package com.turn.browser.verify;

/**
 * Define error return
 *
 * @param <T> Status code type, usually Integer or String
 */
public interface Error<T> {
    /**
     * Get error information
     * @return return error message
     */
    String getMsg();

    /**
     * Get status code
     * @return return status code
     */
    T getCode();
}
