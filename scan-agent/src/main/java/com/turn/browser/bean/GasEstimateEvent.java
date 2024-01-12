package com.turn.browser.bean;

import com.turn.browser.dao.entity.GasEstimate;
import lombok.Data;

import java.util.List;

/**
 * Gas price Estimate event
 */
@Data
public class GasEstimateEvent {

    private String traceId;

    // The unique identifier of the message to prevent repeated processing: block number * 10000 + transaction index
    private Long seq;

    // Gas Estimate list
    private List<GasEstimate> estimateList;

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public List<GasEstimate> getEstimateList() {
        return estimateList;
    }

    public void setEstimateList(List<GasEstimate> estimateList) {
        this.estimateList = estimateList;
    }

}
