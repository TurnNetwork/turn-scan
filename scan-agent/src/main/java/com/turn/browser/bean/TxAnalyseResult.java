package com.turn.browser.bean;

import com.turn.browser.elasticsearch.dto.DelegationReward;
import com.turn.browser.elasticsearch.dto.NodeOpt;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@Accessors(chain = true)
public class TxAnalyseResult {
    private List<NodeOpt> nodeOptList;
    private List<DelegationReward> delegationRewardList;
    private int proposalQty;
}
