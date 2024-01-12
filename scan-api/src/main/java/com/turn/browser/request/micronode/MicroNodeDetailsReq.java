package com.turn.browser.request.micronode;


import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Micronode details request object
 */
public class MicroNodeDetailsReq {

    @NotBlank(message = "{nodeId not null}")
    @Size(min = 128, max = 130)
    private String nodeId;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        if (StringUtils.isBlank(nodeId)) return;
        this.nodeId = nodeId;
    }

}