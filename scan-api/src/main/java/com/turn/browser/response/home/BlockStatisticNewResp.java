package com.turn.browser.response.home;

/**
 * Home trend chart return object
 */
public class BlockStatisticNewResp {
	private Long[] x;
	private Double[] ya;
	private Long[] yb;
	public Long[] getX() {
		return x;
	}
	public void setX(Long[] x) {
		this.x = x;
	}
	public Double[] getYa() {
		return ya;
	}
	public void setYa(Double[] ya) {
		this.ya = ya;
	}
	public Long[] getYb() {
		return yb;
	}
	public void setYb(Long[] yb) {
		this.yb = yb;
	}

}
