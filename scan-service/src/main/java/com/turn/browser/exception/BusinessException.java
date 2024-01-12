package com.turn.browser.exception;


import com.turn.browser.enums.ErrorCodeEnum;
import lombok.Data;

import java.util.Locale;

/**
 * Business Exception
 */
@Data
public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = -2838902301689578334L;
	private int errorCode = -1;
	private String errorMessage;
	private Locale locale;

	public BusinessException(String msg) {
		this(msg, Locale.getDefault());
	}

	public BusinessException(String msg, Locale locale) {
		this.errorMessage = msg;
		this.locale = locale;
	}

	public BusinessException(Integer errorCode, String msg) {
		this(errorCode, msg, Locale.getDefault());
	}

	public BusinessException(Integer errorCode, String msg, Locale locale) {
		this.errorCode = errorCode;
		this.errorMessage = msg;
		this.locale = locale;
	}

	public BusinessException(ErrorCodeEnum errorCodeEnum) {
		this(errorCodeEnum, Locale.getDefault());
	}

	public BusinessException( ErrorCodeEnum errorCodeEnum, Locale locale) {
		this.errorCode = errorCodeEnum.getCode();
		this.errorMessage = errorCodeEnum.getDesc();
		this.locale = locale;
	}

	/**
	 *Construction.
	 *
	 * @param msg error message
	 * @param t previous exception
	 */
	public BusinessException(Integer errorCode, String msg, Throwable t) {
		this(errorCode, msg, Locale.getDefault(), t);
	}

	public BusinessException(Integer errorCode, String msg, Locale locale, Throwable t) {
		this.setStackTrace(t.getStackTrace());

		this.errorCode = errorCode;
		this.errorMessage = msg;
		this.locale = locale;
	}
}
