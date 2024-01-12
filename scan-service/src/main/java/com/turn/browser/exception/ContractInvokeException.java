package com.turn.browser.exception;


/**
 * Contract call exception
 * @Description:
 */
public class ContractInvokeException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ContractInvokeException(String msg){
		super(msg);
	}
}
