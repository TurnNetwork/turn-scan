package com.turn.browser.request.home;

import javax.validation.constraints.NotBlank;

/**
 * Home page search request
 */
public class QueryNavigationRequest {
    @NotBlank(message = "{parameter not null}")
    private String parameter;

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
    
}