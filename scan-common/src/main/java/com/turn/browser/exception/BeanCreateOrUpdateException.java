package com.turn.browser.exception;

/**
 * Business Bean instance creation or update exception
 * @Description:
 */
public class BeanCreateOrUpdateException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BeanCreateOrUpdateException(String msg){
        super(msg);
    }
}
