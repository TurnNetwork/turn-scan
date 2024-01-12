package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

/**
 * @description: Modify the certifier storage parameters
 **/
@Data
@Slf4j
@Builder
@Accessors(chain = true)
public class StakeModify implements BusinessParam {
    //NodeId
    private String nodeId;
    //Node name
    private String nodeName;
    //Third-party social software associated id
    private String externalId;
    //Income address
    private String benefitAddr;
    //The third-party home page of the node
    private String webSite;
    //Node description
    private String details;
    //Whether it is a built-in candidate during chain initialization: 1 yes, 2 no
    private int isInit;
    //The block number where the pledge is located
    private BigInteger stakingBlockNum;
    //Delegation reward ratio
    private Integer nextRewardPer;
    //The number of settlement cycle rounds when the pledge is modified
    private int settleEpoch;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.STAKE_MODIFY;
    }
}
