package com.turn.browser.dao.param.epoch;

import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @Description: Settlement cycle switching parameters are stored in the database
 */
@Data
@Builder
@Accessors(chain = true)
public class Settle implements BusinessParam {
    //Current settlement cycle validator
    private Set<String> curVerifierSet;
    //Verifier of last round of settlement cycle
    private Set<String> preVerifierSet;
    //Staking rewards (AAA)
    private BigDecimal stakingReward;
    //Settlement cycle
    private int settingEpoch;
    //The number of rounds to unlock the pledged amount
    private int stakingLockEpoch;
    //Candidate, exiting, list
    private List<Staking> stakingList;

    private List<String> exitNodeList;

    @Override
    public BusinessType getBusinessType () {
        return BusinessType.SETTLE_EPOCH;
    }
}