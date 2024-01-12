package com.turn.browser.response.extra;

import java.util.List;

/**
 * Query configuration file return parameters
 */
public class ModuleConfig {

	private String module;
	
	private List<ConfigDetail> detail;

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public List<ConfigDetail> getDetail() {
		return detail;
	}

	public void setDetail(List<ConfigDetail> detail) {
		this.detail = detail;
	}
	
}
