package com.turn.browser.dao.param.ppos;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import com.turn.browser.param.claim.Reward;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * // TODO: Define the warehousing parameters of the reward collection business
 * @Description: Claim Reward
 */
@Data
@Builder
@Accessors(chain = true)
public class DelegateRewardClaim implements BusinessParam {
    //Reward information list
    private List<Reward> rewardList;
    //recipient address
    private String address;

    @Override
    public BusinessType getBusinessType() {
        return BusinessType.CLAIM_REWARD;
    }
}
