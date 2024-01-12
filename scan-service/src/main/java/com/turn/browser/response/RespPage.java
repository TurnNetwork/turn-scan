package com.turn.browser.response;

import com.github.pagehelper.Page;

import java.util.Collections;
import java.util.List;

/**
 * Return paging objects uniformly
 */
public class RespPage<T> {
    /**
     *Related error messages
     */
    private String errMsg = "";
    /**
     * Success (0), failure will be determined by the relevant failure code
     */
    private int code = 0;
    /**
     *Total
     */
    private long totalCount;
    /**
     * Show total
     */
    private long displayTotalCount;
    /**
     * total pages
     */
    private long totalPages;
    /**
     *Response data
     */
    private List<T> data = Collections.emptyList();

    /**
     * Initialization data
     */
    public void init(Page<?> page, List<T> data) {
        this.setTotalCount(page.getTotal());
        this.setTotalPages(page.getPages());
        this.setDisplayTotalCount(page.getTotal());
        this.setData(data);
    }

    /**
     * Initialization data
     */
    public void init(List<T> data, long totalCount, long displayTotalCount, long totalPages) {
        this.setTotalCount(totalCount);
        this.setTotalPages(totalPages);
        this.setDisplayTotalCount(displayTotalCount);
        this.setData(data);
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getDisplayTotalCount() {
        return displayTotalCount;
    }

    public void setDisplayTotalCount(long displayTotalCount) {
        this.displayTotalCount = displayTotalCount;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

}
