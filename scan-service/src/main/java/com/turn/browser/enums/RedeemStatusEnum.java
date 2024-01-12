package com.turn.browser.enums;

/**
 * Exit validator frontend enumeration
 */
public enum RedeemStatusEnum {

	EXITING("exiting", 1),
	EXITED("exited" ,2);

	private String name;
	private Integer code;
	RedeemStatusEnum(String name, Integer code) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public Integer getCode() {
		return code;
	}

}
