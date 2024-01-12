package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @description: Create pledge/create validator storage parameters
 **/
@Data
@Slf4j
@Builder
@Accessors(chain = true)
public class StakeCreate implements BusinessParam {
    //NodeId
    private String nodeId;
    //Pledge deposit during the hesitation period (AAA)
    private BigDecimal stakingHes;
    //Node name (pledge node name)
    private String nodeName;
    //Third-party social software associated id
    private String externalId;
    //Income address
    private String benefitAddr;
    //Program Version
    private String programVersion;
    //Large program version
    private String bigVersion;
    //The third-party home page of the node
    private String webSite;
    //Node description
    private String details;
    //Whether it is a built-in candidate during chain initialization: 1 yes, 2 no
    private int isInit;
    //Pledge block height
    private BigInteger stakingBlockNum;
    //Index for initiating pledge transactions
    private int stakingTxIndex;
    //Pledge address
    private String stakingAddr;
    //Add time
    private Date joinTime;
    //Pledge transaction hash
    private String txHash;
    //Delegation reward ratio
    private int delegateRewardPer;
    //The number of settlement cycles required to release the pledge
    private int unStakeFreezeDuration;
    //Unlock the last block frozen by the pledge: the largest of the theoretical end block and the voting end block
    private BigInteger unStakeEndBlock;
    //The number of settlement cycle rounds when the pledge is created
    private int settleEpoch;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.STAKE_CREATE;
    }
}
