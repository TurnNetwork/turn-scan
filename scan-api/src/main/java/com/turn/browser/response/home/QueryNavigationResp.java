package com.turn.browser.response.home;

/** Home page query return object */
public class QueryNavigationResp {
	private String type;
	private QueryNavigationStructResp struct;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public QueryNavigationStructResp getStruct() {
		return struct;
	}
	public void setStruct(QueryNavigationStructResp struct) {
		this.struct = struct;
	}

}
