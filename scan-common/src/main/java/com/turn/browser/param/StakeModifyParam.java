package com.turn.browser.param;

import com.turn.browser.utils.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;

/**
 * txType=1001
 */
@Data
@Builder
@AllArgsConstructor
@Accessors(chain = true)
public class StakeModifyParam extends TxParam{

    /**
     * Income account used to receive block rewards and staking rewards
     */
    private String benefitAddress;

    /**
     * The pledged node ID (also called the candidate’s node ID)
     */
    private String nodeId;
    public void setNodeId(String nodeId){
        this.nodeId= HexUtil.prefix(nodeId);
    }

    /**
     * External Id (with length limit, used to pull the ID of the node description to a third party)
     */
    private String externalId;

    /**
     * The name of the pledged node (with a length limit, indicating the name of the node)
     */
    private String nodeName;

    /**
     * The third-party home page of the node (with a length limit, indicating the home page of the node)
     */
    private String website;

    /**
     * Description of the node (with a length limit, indicating the description of the node)
     */
    private String details;

    /**
     *blockNumber
     */
    private BigInteger blockNumber;

    private String perNodeName;

    /**
     * delegate reward ratio
     */
    private Integer delegateRewardPer;
}
