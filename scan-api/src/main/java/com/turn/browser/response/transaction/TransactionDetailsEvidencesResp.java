package com.turn.browser.response.transaction;

/**
 * Transaction details report substructure return object
 */
public class TransactionDetailsEvidencesResp {

	private String verify; //node id
	private String nodeName; //The reported node name
	public String getVerify() {
		return verify;
	}
	public void setVerify(String verify) {
		this.verify = verify;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
}
