package com.turn.browser.dao.param.epoch;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.List;

/**
 * Consensus cycle change info <br/>
 * <pre>
-- 1. staking update
update `staking` 
set `is_consensus` = if(`node_id` in @validator_list, 1,  2), -- 伪代码
    `pre_cons_block_qty` = `cur_cons_block_qty`,
    `cur_cons_block_qty` = 0
where `status` = 1;

-- 2. node update
update `node` 
set `is_consensus` = if(`node_id` in @validator_list, 1,  2), 
	`stat_verifier_time` = if(`node_id` in @validator_list, `stat_verifier_time` + 1,  `stat_verifier_time`),
	`stat_expect_block_qty` = if(`node_id` in @validator_list, `stat_expect_block_qty` + @expect_block_num,  `stat_expect_block_qty`),  
where `status` = 1;
 * <pre/>
 * @author chendai
 */
@Data
@Builder
@Accessors(chain = true)
public class Consensus implements BusinessParam {
    //The expected number of blocks produced by each validator is the number of blocks produced in the consensus cycle/the number of validators in the current round.
    private BigInteger expectBlockNum;
    //Current consensus cycle validator
    private List<String> validatorList;

    @Override
    public BusinessType getBusinessType () {
        return BusinessType.CONSENSUS_EPOCH;
    }
}