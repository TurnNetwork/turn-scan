package com.turn.browser.dao.param.statistic;

import com.turn.browser.dao.param.BusinessParam;
import com.turn.browser.enums.BusinessType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Network statistics info<br/>
 * Directly replace the following fields when entering the database <br/>
 * <pre>
insert into `network_stat` 
	(`id`, 
	`cur_number`, 
	`node_id`, 
	`node_name`, 
	`tx_qty`, 
	`cur_tps`, 
	`max_tps`, 
	`issue_value`, 
	`turn_value`, 
	`proposal_qty`,  
	`block_reward`, 
	`staking_reward`, 
	`add_issue_begin`, 
	`add_issue_end`, 
	`next_settle`
	)
	values
	('id', 
	'cur_number', 
	'node_id', 
	'node_name', 
	'tx_qty', 
	'cur_tps', 
	'max_tps', 
	'issue_value', 
	'turn_value', 
	'proposal_qty', 
	'block_reward', 
	'staking_reward', 
	'add_issue_begin', 
	'add_issue_end', 
	'next_settle'
	)
	on duplicate key update
	`cur_number` = @, 
	`node_id` = @, 
	`node_name` = @,
	`tx_qty` = @,
	`cur_tps` = @,
	`max_tps` = @,
	`issue_value` = @, 
	`turn_value` = @,
	`proposal_qty` = @,
	`block_reward` = @,
	`staking_reward` = @,
	`add_issue_begin` = @,
	`add_issue_end` = @,
	`next_settle` = @;
 * <pre/>
 * @author chendai
 */
@Data
@Slf4j
@Builder
@Accessors(chain = true)
public class NetworkStatChange implements BusinessParam {
	/**
	 * id
	 */
    private Integer id;

	/**
	 * Current block number
	 */
	private Long curNumber;

	/**
	 * Node ID
	 */
	private String nodeId;

	/**
	 * Node name
	 */
	private String nodeName;

	/**
	 *Total number of transactions
	 */
	private Integer txQty;

	/**
	 * Current transaction TPS
	 */
	private Integer curTps;

	/**
	 *Maximum transaction TPS
	 */
	private Integer maxTps;

	/**
	 *Total number of proposals
	 */
	private Integer proposalQty;

	/**
	 * Current block reward (AAA)
	 */
	private BigDecimal blockReward;

	/**
	 * Current staking rewards (AAA)
	 */
	private BigDecimal stakingReward;

	/**
	 * The starting block number of the current issuance cycle
	 */
	private Long addIssueBegin;

	/**
	 * The end block number of the current issuance cycle
	 */
	private Long addIssueEnd;

	/**
	 * Number of blocks remaining until the next settlement cycle
	 */
	private Long nextSettle;

	/**
	 *Current circulation (AAA)
	 */
	private BigDecimal issueValue;

	/**
	 * Current circulation (AAA)
	 */
	private BigDecimal turnValue;

	/**
	 * Latest serial number of node operation record
	 */
	private Long nodeOptSeq;

	@Override
	public BusinessType getBusinessType() {
		return BusinessType.NETWORK_STATISTIC;
	}
}
