package com.turn.browser.service.elasticsearch.bean;

import org.elasticsearch.search.sort.SortOrder;

/**
 * es transfer sort dto
 */
public class ESSortDto {

	private String sortName;
	
	private SortOrder sortOrder;
	
	public ESSortDto() {
		this.sortName = "";
		this.sortOrder = SortOrder.DESC;
	}
	
	public ESSortDto(String sortName, SortOrder sortOrder) {
		this.sortName = sortName;
		this.sortOrder = sortOrder;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
	
}
