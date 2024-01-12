package com.turn.browser.dao.param.epoch;

import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.List;

/**
 * @Description: Election cycle switching parameters are stored in the database
 */
@Data
@Builder
@Accessors(chain = true)
public class Election implements BusinessParam {
    /*++++++++++Low block rate locked part++++++++++++*/
    //List of punishments required
    private List <Staking> lockedNodeList;
    //Settlement cycle
    private int settingEpoch;
    //The number of settlement cycles that need to be locked for zero block generation
    private int zeroProduceFreezeDuration;
    //The settlement period when the node is punished for low block production
    private int zeroProduceFreezeEpoch;

    /*++++++++++Exit part with low block rate+++++++++++*/
    //List of punishments required
    private List <Staking> exitingNodeList;
    //The number of settlement cycles required to release the pledge
    private int unStakeFreezeDuration;
    //Unlock the last block frozen by the pledge: the largest of the theoretical end block and the voting end block
    private BigInteger unStakeEndBlock;

    @Override
    public BusinessType getBusinessType () {
        return BusinessType.ELECTION_EPOCH;
    }
}