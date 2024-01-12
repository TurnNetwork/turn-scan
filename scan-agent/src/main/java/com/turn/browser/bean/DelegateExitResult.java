package com.turn.browser.bean;

import com.turn.browser.dao.param.ppos.DelegateExit;
import com.turn.browser.elasticsearch.dto.DelegationReward;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
public class DelegateExitResult {
    private DelegateExit delegateExit;
    private DelegationReward delegationReward;
}
