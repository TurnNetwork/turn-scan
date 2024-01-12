package com.turn.browser.enums;


/**
 *  @file StakingStatusEnum.java
 *  @description 
 *	@author zhangrj
 *  @data 2019年8月31日
 */
public enum BubbleStatusEnum {
	ALL("all", 0),
	ACTIVE("active" ,1),
	RELEASING("releasing",2),
	RELEASED("released",3);

	private String name;

	private Integer code;

	BubbleStatusEnum(String name, Integer code) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public Integer getCode() {
		return code;
	}

	public static BubbleStatusEnum getEnumByCodeValue(int code){
		BubbleStatusEnum[] allEnums = values();
		for(BubbleStatusEnum microNodeStatusEnum : allEnums){
			if(microNodeStatusEnum.getCode()==code)
				return microNodeStatusEnum;
		}
		return null;
	}

	public static BubbleStatusEnum getEnumByName(String name){
		BubbleStatusEnum[] allEnums = values();
		for(BubbleStatusEnum microNodeStatusEnum : allEnums){
			if(microNodeStatusEnum.getName().equalsIgnoreCase(name))
				return microNodeStatusEnum;
		}
		return null;
	}
}
