package com.turn.browser.config;

/**
 * Manual authentication value
 */
public class RecaptchaDto {

	private String challenge_ts;
	
	private Long sore;
	
	private String hostname;
	
	private Boolean success;

	public String getChallenge_ts() {
		return challenge_ts;
	}

	public void setChallenge_ts(String challenge_ts) {
		this.challenge_ts = challenge_ts;
	}

	public Long getSore() {
		return sore;
	}

	public void setSore(Long sore) {
		this.sore = sore;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
	
	
}
