package com.turn.browser.request.staking;

import com.turn.browser.utils.HexUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Validator details request object
 */
public class StakingDetailsReq {

    @NotBlank(message = "{nodeId not null}")
    @Size(min = 128, max = 130)
    private String nodeId;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        if (StringUtils.isBlank(nodeId)) return;
        this.nodeId = HexUtil.prefix(nodeId.toLowerCase());
    }

}