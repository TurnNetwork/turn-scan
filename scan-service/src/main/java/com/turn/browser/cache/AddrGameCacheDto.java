package com.turn.browser.cache;

import com.turn.browser.dao.entity.AddrGame;
import com.turn.browser.elasticsearch.dto.Transaction;
import com.turn.browser.response.RespPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Game table cache dto
 */
public class AddrGameCacheDto {

	public AddrGameCacheDto() {
		this.addrGameList = new ArrayList<>();
	}
	/**
	 * Transaction construction initial method
	 * @param page
	 */
	public AddrGameCacheDto(List<AddrGame> addrGameList, RespPage page) {
		this.addrGameList = addrGameList;
		this.page = page;
	}
	private List<AddrGame> addrGameList;

	private RespPage page;

	public List<AddrGame> getAddrGameList() {
		return addrGameList;
	}

	public void setAddrGameList(List<AddrGame> addrGameList) {
		this.addrGameList = addrGameList;
	}

	public RespPage getPage() {
		return page;
	}

	public void setPage(RespPage page) {
		this.page = page;
	}
}