package com.turn.browser.response.home;

import java.util.List;

/**
 * Home page verifier list return object
 */
public class StakingListNewResp {
	private Boolean isRefresh;
	private List<StakingListResp> dataList;
	public Boolean getIsRefresh() {
		return isRefresh;
	}
	public void setIsRefresh(Boolean isRefresh) {
		this.isRefresh = isRefresh;
	}
	public List<StakingListResp> getDataList() {
		return dataList;
	}
	public void setDataList(List<StakingListResp> dataList) {
		this.dataList = dataList;
	}
	
}
