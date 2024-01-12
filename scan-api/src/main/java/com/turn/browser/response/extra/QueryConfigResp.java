package com.turn.browser.response.extra;

import java.util.List;

/**
 * Query configuration file return parameters
 */
public class QueryConfigResp {

	private List<ModuleConfig> config;

	public List<ModuleConfig> getConfig() {
		return config;
	}

	public void setConfig(List<ModuleConfig> config) {
		this.config = config;
	}
	
}
