package com.turn.browser.enums;


/**
 *  Returns the front-end micro-node status enumeration
 */
public enum MicroNodeStatusEnum {
	ALL("all", 0),
	CANDIDATE("candidate" ,1),
	EXITED("exited",2);

	private String name;

	private Integer code;

	MicroNodeStatusEnum(String name, Integer code) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public Integer getCode() {
		return code;
	}

	public static MicroNodeStatusEnum getEnumByCodeValue(int code){
		MicroNodeStatusEnum[] allEnums = values();
		for(MicroNodeStatusEnum microNodeStatusEnum : allEnums){
			if(microNodeStatusEnum.getCode()==code)
				return microNodeStatusEnum;
		}
		return null;
	}

	public static MicroNodeStatusEnum getEnumByName(String name){
		MicroNodeStatusEnum[] allEnums = values();
		for(MicroNodeStatusEnum microNodeStatusEnum : allEnums){
			if(microNodeStatusEnum.getName().equalsIgnoreCase(name))
				return microNodeStatusEnum;
		}
		return null;
	}
}
