package com.turn.browser.exception;

/**
 * 	Unified data return exception handling
 *  @file ResponseException.java
 */
public class ResponseException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResponseException(String msg){
        super(msg);
    }
}
