package com.turn.browser.response.bubble;


import java.util.Date;

/**
 * 活跃微节点列表返回对象
 *
 */
public class BubbleListResp {

    private Long bubbleId;

    private Integer status;

    private Date createTime;

    public Long getBubbleId() {
        return bubbleId;
    }

    public void setBubbleId(Long bubbleId) {
        this.bubbleId = bubbleId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
