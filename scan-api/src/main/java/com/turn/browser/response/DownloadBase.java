package com.turn.browser.response;

/**
 * Address file download object, used to pass download parameters
 */
public class DownloadBase {
    private byte [] data;
    private String filename;
    private long length;
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
}
