package com.turn.browser.config;

import com.turn.browser.constant.Browser;
import com.turn.browser.request.PageReq;
import org.apache.commons.lang3.StringUtils;
/**
 * Get the dto of message
 */
public class MessageDto extends PageReq{

	private String userNo;
	private String key;
    private String queryStatus;
	
    /**
     * Assemble key
     * @method getMessageKey
     * @return
     */
	public String getMessageKey () {
		StringBuilder sb = new StringBuilder();
		/**
		 * If null, fill in the blanks
		 * Delimiter splicing
		 */
		if(this.getPageNo() != null) {
			sb.append(this.getPageNo());
		}
		sb.append(Browser.PEAD_SPILT);
		if(this.getPageSize() != null) {
			sb.append(this.getPageSize());
		}
		sb.append(Browser.PEAD_SPILT);
		if(StringUtils.isNotBlank(this.getKey())) {
			sb.append(this.getKey());
		}
		sb.append(Browser.PEAD_SPILT);
		if(StringUtils.isNotBlank(this.getQueryStatus())) {
			sb.append(this.getQueryStatus());
		}
		return sb.toString();
	}
	
	/**
	 * parse key
	 * @method analysisKey
	 * @param data
	 * @return
	 */
	public MessageDto analysisKey (String data) {
		String[] message = data.split(Browser.OPT_SPILT);
		/**
		 * When the key length is greater than 2, it is considered a legitimate request.
		 */
		if(message.length > 2) {
			this.setPageNo(Integer.parseInt(message[0]));
			this.setPageSize(Integer.parseInt(message[1]));
			this.setKey(message[2]);
			this.setQueryStatus(message[3]);
		}
		return this;
	}
	
	/**
	 * parse key
	 * @method analysisKey
	 * @param data  User number|Page number|Number of pages|Query status|Fuzzy query key
	 * @return
	 */
	public MessageDto analysisData (String data) {
		/**
		 * Set parameter value based on delimiter
		 */
		String[] message = data.split(Browser.HTTP_SPILT);
		if(message.length > 0) {
			this.setUserNo(message[0]);
		}
		if(message.length > 1) {
			this.setPageNo(Integer.parseInt(message[1]));
		}
		if(message.length > 2) {
			this.setPageSize(Integer.parseInt(message[2]));
		}
		if(message.length > 3) {
			this.setQueryStatus(message[3]);
		}
		if(message.length > 4) {
			this.setKey(message[4]);
		}
		return this;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getQueryStatus() {
		return queryStatus;
	}

	public void setQueryStatus(String queryStatus) {
		this.queryStatus = queryStatus;
	}
	
}
