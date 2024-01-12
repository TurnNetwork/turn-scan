package com.turn.browser.exception;


/**
 * The contract call was successful, but the result was empty
 * @Description:
 */
public class BlankResponseException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public BlankResponseException(String msg){
		super(msg);
	}
}
