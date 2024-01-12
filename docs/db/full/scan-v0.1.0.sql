-- Full script
CREATE DATABASE IF NOT EXISTS `scan_turn` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `scan_turn`;

DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`
(
    `address` varchar(42) NOT NULL COMMENT 'address',
    `type` int(11) NOT NULL COMMENT 'Address type: 1 account, 2 built-in contract, 3EVM contract, 4WASM contract, 5Erc20, 6Erc721',
    `balance` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Balance (AAA)',
    `restricting_balance` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Locked balance (AAA)',
    `staking_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Amount of pledge (AAA)',
    `delegate_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Delegated amount (AAA)',
    `redeemed_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Pledge amount in redemption (AAA)',
    `candidate_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of delegated verifiers',
    `delegate_hes` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Unlocked delegate (AAA)',
    `delegate_locked` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Locked delegate (AAA)',
    `delegate_released` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'The commission amount to be redeemed (AAA, the user needs to actively initiate a redemption transaction)',
    `contract_name` varchar(125) NOT NULL DEFAULT '' COMMENT 'Contract name',
    `contract_create` varchar(125) NOT NULL DEFAULT '' COMMENT 'Contract creator address',
    `contract_createHash` varchar(72) NOT NULL DEFAULT '0' COMMENT 'Create the transaction Hash of the contract',
    `contract_destroy_hash` varchar(72) DEFAULT '' COMMENT 'Destroy the transaction Hash of the contract',
    `contract_bin` longtext COMMENT 'Contract bincode data (contract code queried through web3j)',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `have_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Have received commission reward',
    `tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of transactions',
    `transfer_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of transfer transactions',
    `delegate_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of delegated transactions',
    `staking_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of staking transactions',
    `proposal_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of governance transactions',
    `erc1155_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of transactions corresponding to erc1155 token',
    `erc721_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of transactions corresponding to erc721 token',
    `erc20_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of transactions corresponding to erc20 token',
    PRIMARY KEY (`address`),
    KEY `type` (`type`) USING BTREE
);

DROP TABLE IF EXISTS `block_node`;
CREATE TABLE `block_node`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `node_id` varchar(130) NOT NULL COMMENT 'node id',
    `node_name` varchar(64) NOT NULL DEFAULT '' COMMENT 'Node name (pledge node name)',
    `staking_consensus_epoch` int(11) NOT NULL DEFAULT '0' COMMENT 'Consensus cycle identifier',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    KEY `staking_consensus_epoch` (`staking_consensus_epoch`) USING BTREE
);

DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`
(
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `module` varchar(64) NOT NULL COMMENT 'Parameter module name',
    `name` varchar(128) NOT NULL COMMENT 'Parameter name',
    `init_value` varchar(255) NOT NULL COMMENT 'System initial value',
    `stale_value` varchar(255) NOT NULL COMMENT 'old value',
    `value` varchar(255) NOT NULL COMMENT 'new value',
    `range_desc` varchar(255) NOT NULL COMMENT 'Parameter value range description',
    `active_block` bigint(20) NOT NULL COMMENT 'Active block high',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `delegation`;
CREATE TABLE `delegation`
(
    `delegate_addr` varchar(42) NOT NULL COMMENT 'Delegated transaction address',
    `staking_block_num` bigint(20) NOT NULL COMMENT 'Latest staking transaction block high',
    `node_id` varchar(130) NOT NULL COMMENT 'node id',
    `delegate_hes` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Unlocked delegation amount (AAA)',
    `delegate_locked` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Locked delegation amount (AAA)',
    `delegate_released` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Amount to be withdrawn (AAA)',
    `is_history` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether it is history:\r\n1Yes,\r\n2No',
    `sequence` bigint(20) NOT NULL COMMENT 'Sequence number when first commissioned: blockNum*100000+tx_index',
    `cur_delegation_block_num` bigint(20) NOT NULL COMMENT 'Latest commissioned transaction block number',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`delegate_addr`, `staking_block_num`, `node_id`),
    KEY `node_id` (`node_id`) USING BTREE,
    KEY `staking_block_num` (`staking_block_num`) USING BTREE
);

DROP TABLE IF EXISTS `gas_estimate`;
CREATE TABLE `gas_estimate`
(
    `addr` varchar(42) NOT NULL COMMENT 'Entrusted transaction address',
    `node_id` varchar(130) NOT NULL COMMENT 'node id',
    `sbn` bigint(20) NOT NULL COMMENT 'Latest pledge transaction block high',
    `epoch` bigint(20) NOT NULL DEFAULT '0' COMMENT 'The commission is not calculated period',
    PRIMARY KEY (`addr`, `node_id`, `sbn`)
);

DROP TABLE IF EXISTS `gas_estimate_log`;
CREATE TABLE `gas_estimate_log`
(
    `seq` bigint(20) NOT NULL COMMENT 'serial number',
    `json` longtext NOT NULL,
    PRIMARY KEY (`seq`)
);

DROP TABLE IF EXISTS `n_opt_bak`;
CREATE TABLE `n_opt_bak`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment id',
    `node_id` varchar(130) NOT NULL COMMENT 'node id',
    `type` int(11) NOT NULL COMMENT 'Operation type: 1 create, 2 modify, 3 exit, 4 proposal, 5 vote, 6 double signing, 7 low block rate, 11 unlock',
    `tx_hash` varchar(72) DEFAULT NULL COMMENT 'Transaction hash',
    `b_num` bigint(20) DEFAULT NULL COMMENT 'Transaction block number',
    `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'time',
    `desc` varchar(2500) DEFAULT NULL COMMENT 'Operation description',
    `cre_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `upd_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    KEY `node_id` (`node_id`) USING BTREE,
    KEY `tx_hash` (`tx_hash`) USING BTREE,
    KEY `block_number` (`b_num`) USING BTREE
);

DROP TABLE IF EXISTS `network_stat`;
CREATE TABLE `network_stat`
(
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `cur_number` bigint(20) NOT NULL COMMENT 'Current block number',
    `cur_block_hash` varchar(66) NOT NULL DEFAULT '' COMMENT 'Current block Hash',
    `node_id` varchar(130) NOT NULL COMMENT 'Node ID',
    `node_name` varchar(256) NOT NULL,
    `tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of transactions',
    `cur_tps` int(11) NOT NULL DEFAULT '0' COMMENT 'Current transaction TPS',
    `max_tps` int(11) NOT NULL DEFAULT '0' COMMENT 'Maximum transaction TPS',
    `issue_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Current issue amount (AAA)',
    `turn_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Current circulation (AAA)',
    `available_staking` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Available total staking amount (AAA)',
    `staking_delegation_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Total number of real-time pledge commissions (AAA)',
    `staking_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Total number of real-time pledges (AAA)',
    `doing_proposal_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of proposals in progress',
    `proposal_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Total number of proposals',
    `address_qty` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'Number of addresses',
    `block_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Current block reward (AAA)',
    `staking_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Current staking reward (AAA)',
    `settle_staking_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Total staking reward in the current settlement cycle',
    `add_issue_begin` bigint(20) NOT NULL COMMENT 'The starting block number of the current issuance cycle',
    `add_issue_end` bigint(20) NOT NULL COMMENT 'The end block number of the current issuance cycle',
    `next_settle` bigint(20) NOT NULL COMMENT 'The number of remaining blocks before the next settlement cycle',
    `node_opt_seq` bigint(20) NOT NULL COMMENT 'Node operation record latest sequence number',
    `issue_rates` text COMMENT 'Additional issuance ratio',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `avg_pack_time` bigint(20) NOT NULL DEFAULT '0' COMMENT 'Average block packing time',
    `erc1155_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of transactions corresponding to erc1155 token',
    `erc721_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of transactions corresponding to erc721 token',
    `erc20_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of transactions corresponding to erc20 token',
    `year_num` int(11) DEFAULT '1' COMMENT 'year',PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `node`;
CREATE TABLE `node`
(
    `node_id` varchar(130) NOT NULL COMMENT 'node id',
    `stat_slash_multi_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of multi-sign reports',
    `stat_slash_low_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of low block rate reports',
    `stat_block_qty` bigint(20) NOT NULL DEFAULT '0' COMMENT 'Statistics of block numbers at nodes',
    `stat_expect_block_qty` bigint(20) NOT NULL DEFAULT '0' COMMENT 'The number of blocks expected by the node',
    `stat_verifier_time` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of rounds of consensus verification',
    `is_recommend` int(11) NOT NULL DEFAULT '2' COMMENT 'Official recommendation: 1 Yes\r\n2: No',
    `total_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Total number of valid pledge commissions (AAA)',
    `staking_block_num` bigint(20) NOT NULL COMMENT 'Block number when staking',
    `staking_tx_index` int(11) NOT NULL COMMENT 'Index for initiating staking transactions',
    `staking_hes` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Deposit during the hesitation period (AAA)',
    `staking_locked` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Pledge deposit during the locking period (AAA)',
    `staking_reduction` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Refunding the pledge (AAA)',
    `staking_reduction_epoch` int(11) NOT NULL DEFAULT '0' COMMENT 'Settlement cycle identifier',
    `node_name`                    varchar(256)   NOT NULL,
    `node_icon` varchar(255) DEFAULT '' COMMENT 'Node avatar (associated with external_id, obtained by third-party software)',
    `external_id` varchar(255) NOT NULL DEFAULT '' COMMENT 'Third-party social software associated id',
    `external_name` varchar(128) DEFAULT NULL COMMENT 'Third-party social software associated user name',
    `staking_addr` varchar(42) NOT NULL COMMENT 'Account address that initiated staking',
    `benefit_addr` varchar(42) NOT NULL DEFAULT '' COMMENT 'Benefit address',
    `annualized_rate` double(64, 2) NOT NULL DEFAULT '0.00' COMMENT 'Estimated annualized rate',
    `program_version` int(11) NOT NULL DEFAULT '0' COMMENT 'program version',
    `big_version` int(11) NOT NULL DEFAULT '0' COMMENT 'Big program version',
    `web_site` varchar(255) NOT NULL DEFAULT '' COMMENT 'Node's third-party home page',
    `details` varchar(256) NOT NULL,
    `join_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Join time',
    `leave_time` timestamp NULL DEFAULT NULL COMMENT 'leave time',
    `leave_num` bigint(20) DEFAULT NULL COMMENT 'Block high in exit',
    `status` int(11) NOT NULL DEFAULT ''1'' COMMENT ''Node status: 1 candidate, 2 exiting, 3 exited, 4 locked'',
    `is_consensus` int(11) NOT NULL DEFAULT ''2'' COMMENT ''Whether the consensus cycle validator is: 1 yes, 2 no'',
    `is_settle` int(11) NOT NULL DEFAULT ''2'' COMMENT ''Whether the settlement cycle verifier: 1 yes, 2 no'',
    `is_init` int(11) NOT NULL DEFAULT ''2'' COMMENT ''Whether it is a built-in candidate during chain initialization: 1 yes, 2 no'',
    `stat_delegate_value` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''Effective delegation amount (AAA)'',
    `stat_delegate_released` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''Delegated amount to be withdrawn (AAA)'',
    `stat_valid_addrs` int(11) NOT NULL DEFAULT ''0'' COMMENT ''Number of valid delegation addresses'',
    `stat_invalid_addrs` int(11) NOT NULL DEFAULT ''0'' COMMENT ''Number of delegate addresses to be extracted'',
    `stat_block_reward_value` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''Block reward statistics (incentive pool) (AAA)'',
    `stat_staking_reward_value` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''Staking reward statistics (incentive pool) (AAA)'',
    `stat_fee_reward_value` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''Block reward statistics (handling fee) (AAA)'',
    `predict_staking_reward` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''The staking reward expected to be obtained in the current settlement cycle'',
    `annualized_rate_info` longtext COMMENT ''Income and pledge information of recent settlement cycles'',' ||
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `reward_per` int(11) NOT NULL DEFAULT '0' COMMENT 'Delegation reward ratio',
    `next_reward_per` int(11) NOT NULL DEFAULT '0' COMMENT 'Next settlement cycle commission reward ratio',
    `next_reward_per_mod_epoch` int(11) DEFAULT '0' COMMENT '[Next settlement cycle commission reward ratio] Modify the settlement cycle',
    `have_dele_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'All pledges have received delegation rewards',
    `pre_dele_annualized_rate` double(64, 2) NOT NULL DEFAULT '0.00' COMMENT 'Estimated commission rate of return in the previous participation cycle',
    `dele_annualized_rate` double(64, 2) NOT NULL DEFAULT '0.00' COMMENT 'Estimated commission rate of return',
    `total_dele_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'The total delegation reward of the current pledge',
    `pre_total_dele_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'The total delegation reward accumulation field of all historical pledge records (total_dele_reward will be accumulated to this field when staking is withdrawn)',
    `exception_status` int(11) NOT NULL DEFAULT '1' COMMENT '1 is normal, 2 is abnormal due to low block production, 3 is double-signed, 4 is punished due to low block production (for example, the validator is elected in two consecutive cycles, but in the first The block production rate in one cycle is low), 5 were punished for double signing',
    `un_stake_freeze_duration` int(11) NOT NULL COMMENT 'The number of settlement cycles theoretically locked for unstaking',
    `un_stake_end_block` bigint(20) DEFAULT NULL COMMENT 'Unstake the last block frozen: the largest of the theoretical end block and the voting end block',
    `zero_produce_freeze_duration` int(11) DEFAULT NULL COMMENT 'Zero block production node lock settlement cycle number',
    `zero_produce_freeze_epoch` int(11) DEFAULT NULL COMMENT 'The settlement period when the zero block is locked',
    `low_rate_slash_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Node zero block generation times',
    `node_settle_statis_info` text COMMENT 'Block statistics information of node settlement cycle',
    `node_apr` text COMMENT 'node apr',
    PRIMARY KEY (`node_id`),
    KEY `node_id` (`node_id`) USING BTREE,
    KEY `status` (`status`),
    KEY `staking_addr` (`staking_addr`),
    KEY `benefit_addr` (`benefit_addr`),
    KEY `list` (`status`, `is_settle`, `big_version`, `total_value`, `staking_block_num`, `staking_tx_index`),
    KEY `list2` (`big_version`, `total_value`, `staking_block_num`, `staking_tx_index`)
);

DROP TABLE IF EXISTS `proposal`;
CREATE TABLE `proposal`
(
    `hash` varchar(72) NOT NULL COMMENT 'Proposal transaction hash',
    `type` int(11) NOT NULL COMMENT 'Proposal type: 1 text proposal, 2 upgrade proposal, 3 parameter proposal, 4 cancellation proposal',
    `node_id` varchar(130) NOT NULL COMMENT 'Submit proposal verifier (node ​​ID)',
    `node_name` varchar(256) NOT NULL,
    `url` varchar(255) NOT NULL COMMENT 'Proposal URL',
    `new_version` varchar(64) DEFAULT NULL COMMENT 'New proposal version',
    `end_voting_block` bigint(20) NOT NULL COMMENT 'Proposal end block',
    `active_block` bigint(20) DEFAULT NULL COMMENT 'Proposal effective block',
    `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Proposal time',
    `yeas` bigint(20) NOT NULL DEFAULT '0' COMMENT 'yes vote',
    `nays` bigint(20) NOT NULL DEFAULT '0' COMMENT 'no vote',
    `abstentions` bigint(20) NOT NULL DEFAULT '0' COMMENT 'abstention vote',
    `accu_verifiers` bigint(20) NOT NULL DEFAULT '0' COMMENT 'Total number of validators eligible to vote during the entire voting period',
    `status` int(11) NOT NULL DEFAULT '1' COMMENT 'Proposal status: 1 voting, 2 passed, 3 failed, 4 pre-upgrade, 5 effective, 6 canceled',
    `pip_num` varchar(128) NOT NULL COMMENT 'pip number (requires assembly of PIP-number)',
    `pip_id` varchar(128) NOT NULL COMMENT 'proposal id',
    `topic` varchar(255) DEFAULT NULL COMMENT 'Proposal topic',
    `description` varchar(255) DEFAULT NULL COMMENT 'Proposal description',
    `canceled_pip_id` varchar(255) DEFAULT NULL COMMENT 'Cancelled proposal id',
    `canceled_topic` varchar(255) DEFAULT NULL COMMENT 'The topic of the canceled proposal',
    `block_number` bigint(20) NOT NULL COMMENT 'The block where the proposal transaction is located',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `completion_flag` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether the proposal related data is supplemented with completion flag: 1 yes, 2 no',
    `module` varchar(64) DEFAULT NULL COMMENT 'Parameter module (parameter proposal exclusive attribute)',
    `name` varchar(128) DEFAULT NULL COMMENT 'Parameter name (parameter proposal exclusive attribute)',
    `stale_value` varchar(255) DEFAULT NULL COMMENT 'Original parameter value',
    `new_value` varchar(255) DEFAULT NULL COMMENT 'Parameter value (parameter proposal exclusive attribute)',
    PRIMARY KEY (`hash`),
    KEY `type` (`type`) USING BTREE
);

DROP TABLE IF EXISTS `rp_plan`;
CREATE TABLE `rp_plan`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `address` varchar(42) NOT NULL DEFAULT '0' COMMENT 'Publish lock plan address',
    `epoch` decimal(25, 0) NOT NULL COMMENT 'Lock position planning period',
    `amount` decimal(65, 0) NOT NULL COMMENT 'Amount to be released on the block (AAA)',
    `number` bigint(20) NOT NULL COMMENT 'The block where the lock-up plan is located',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    KEY `number index` (`number`) USING BTREE
);

DROP TABLE IF EXISTS `slash`;
CREATE TABLE `slash`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `slash_data` longtext NOT NULL COMMENT 'Report evidence',
    `node_id` varchar(130) NOT NULL COMMENT 'NodeId',
    `tx_hash` varchar(128) NOT NULL COMMENT 'Transaction hash',
    `time` datetime NOT NULL COMMENT 'time',
    `setting_epoch` int(16) NOT NULL COMMENT 'Round up by (block_number/number of blocks produced in each settlement cycle)',
    `staking_block_num` bigint(20) NOT NULL COMMENT 'The block height of the staking transaction',
    `slash_rate` decimal(65, 2) NOT NULL DEFAULT '0' COMMENT 'Double signing penalty ratio',
    `slash_report_rate` decimal(65, 2) NOT NULL DEFAULT '0' COMMENT 'The proportion of penalties allocated to whistleblowers',
    `benefit_address` varchar(255) NOT NULL COMMENT 'Transaction sender',
    `code_remain_redeem_amount` decimal(65, 2) NOT NULL DEFAULT '0' COMMENT 'The remaining pledge amount after the double-signing penalty, because the node is set to exit after the double-signing penalty, and all amounts will be moved to the pending redemption field ',
    `code_reward_value` decimal(65, 2) NOT NULL DEFAULT '0' COMMENT 'Amount of reward',
    `code_status` int(1) DEFAULT NULL COMMENT 'Node status: 1 candidate, 2 exiting, 3 exited, 4 locked',
    `code_staking_reduction_epoch` int(16) DEFAULT NULL COMMENT 'Currently exiting',
    `code_slash_value` decimal(65, 2) NOT NULL DEFAULT '0' COMMENT 'Amount of penalty',
    `un_stake_freeze_duration` int(16) NOT NULL COMMENT 'The number of settlement cycles required to unstake',
    `un_stake_end_block` bigint(20) NOT NULL COMMENT 'Unstake the last block frozen: the largest of the theoretical end block and the voting end block',
    `block_num` bigint(20) NOT NULL COMMENT 'Double signed block',
    `is_quit` int(11) NOT NULL DEFAULT '1' COMMENT 'Whether to exit: 1 yes, 2 no',
    `is_handle` tinyint(1) NOT NULL COMMENT 'Whether it has been processed, 1-yes, 0-no',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`)
) COMMENT ='Punishment Record Table';

DROP TABLE IF EXISTS `staking`;
CREATE TABLE `staking`
(
    `node_id` varchar(130) NOT NULL COMMENT 'Pledge node address',
    `staking_block_num` bigint(20) NOT NULL COMMENT 'Staking block height',
    `staking_tx_index` int(11) NOT NULL COMMENT 'Index for initiating staking transactions',
    `staking_hes` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Deposit during the hesitation period (AAA)',
    `staking_locked` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Pledge deposit during the locking period (AAA)',
    `staking_reduction` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Refunding the pledge (AAA)',
    `staking_reduction_epoch` int(11) NOT NULL DEFAULT '0' COMMENT 'The number of settlement cycles when canceling the pledge',
    `node_name` varchar(256) NOT NULL,
    `node_icon` varchar(255) DEFAULT '' COMMENT 'Node avatar (associated with external_id, obtained by third-party software)',
    `external_id` varchar(255) NOT NULL DEFAULT '' COMMENT 'Third-party social software associated id',
    `external_name` varchar(128) DEFAULT NULL COMMENT 'Third-party social software associated user name',
    `staking_addr` varchar(42) NOT NULL COMMENT 'Account address that initiated staking',
    `benefit_addr` varchar(42) NOT NULL DEFAULT '' COMMENT 'Benefit address',
    `annualized_rate` double(64, 2) NOT NULL DEFAULT '0.00' COMMENT 'Estimated annualized rate',
    `program_version` varchar(10) NOT NULL DEFAULT '0' COMMENT 'program version',
    `big_version` varchar(10) NOT NULL DEFAULT '0' COMMENT 'Big program version',
    `web_site` varchar(255) NOT NULL DEFAULT '' COMMENT 'Node third-party home page',
    `details` varchar(256) NOT NULL,
    `join_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Join time',
    `leave_time` timestamp NULL DEFAULT NULL COMMENT 'leave time',
    `leave_num` bigint(20) DEFAULT NULL COMMENT 'Block high in exit',
    `status` int(11) NOT NULL DEFAULT '1' COMMENT 'Node status: 1 candidate, 2 exiting, 3 exited, 4 locked',
    `is_consensus` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether the consensus cycle validator is: 1 yes, 2 no',
    `is_settle` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether the settlement cycle verifier: 1 yes, 2 no',
    `is_init` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether it is a built-in candidate during chain initialization: 1 yes, 2 no',
    `stat_delegate_hes` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Unlocked delegate (AAA)',
    `stat_delegate_locked` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Locked delegate (AAA)',
    `stat_delegate_released` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Delegation to be extracted (AAA)',
    `block_reward_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Block reward statistics (incentive pool) (AAA)',
    `predict_staking_reward` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''The staking reward expected to be obtained in the current settlement cycle'',
    `staking_reward_value` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''Staking reward (incentive pool) (AAA)'',
    `fee_reward_value` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''Block reward statistics (handling fee) (AAA)'',
    `cur_cons_block_qty` bigint(20) NOT NULL DEFAULT ''0'' COMMENT ''Number of blocks produced in the current consensus cycle'',
    `pre_cons_block_qty` bigint(20) NOT NULL DEFAULT ''0'' COMMENT ''Number of blocks produced in the last consensus cycle'',
    `annualized_rate_info` longtext COMMENT ''Income and pledge information of recent settlement cycles'',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''Creation time'',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''update time'',
    `reward_per` int(11) NOT NULL COMMENT ''Delegation reward ratio'',
    `next_reward_per` int(11) NOT NULL DEFAULT ''0'' COMMENT ''Next settlement cycle commission reward ratio'',
    `next_reward_per_mod_epoch` int(11) DEFAULT ''0'' COMMENT ''[Next settlement cycle commission reward ratio] Modify the settlement cycle'',
    `have_dele_reward` decimal(65, 0) NOT NULL DEFAULT ''0'' COMMENT ''The node''s current pledge has received the delegation reward'',
    `pre_dele_annualized_rate` double(64, 2) NOT NULL DEFAULT ''0.00'' COMMENT ''Estimated commission rate of return in the previous participation cycle'',
    `dele_annualized_rate` double(64, 2) NOT NULL DEFAULT ''0.00'' COMMENT ''Estimated commission rate of return'',
    `total_dele_reward` decimal(65, 0) DEFAULT ''0'' COMMENT ''The total delegation reward currently pledged by the node'',
    `exception_status` int(11) NOT NULL DEFAULT ''1'' COMMENT ''1 is normal, 2 is abnormal due to low block production, 3 is double-signed, 4 is punished due to low block production (for example, the validator is elected in two consecutive cycles, but in the first The block production rate in one cycle is low), 5 were punished for double signing'',
    `un_stake_freeze_duration` int(11) NOT NULL COMMENT ''The number of settlement cycles theoretically locked for unstaking'',
    `un_stake_end_block` bigint(20) DEFAULT NULL COMMENT ''Unstake the last block frozen: the largest of the theoretical end block and the voting end block'',
    `zero_produce_freeze_duration` int(11) DEFAULT NULL COMMENT ''Zero block production node lock settlement cycle number'',
    `zero_produce_freeze_epoch` int(11) DEFAULT NULL COMMENT ''The settlement period when the zero block is locked'',
    `low_rate_slash_count` int(11) NOT NULL DEFAULT ''0'' COMMENT ''Node zero block generation times'',
    `node_settle_statis_info` text COMMENT ''Block statistics information of node settlement cycle'',
    `node_apr` text COMMENT ''node apr'',
    PRIMARY KEY (`node_id`, `staking_block_num`),
    KEY `staking_addr` (`staking_addr`) USING BTREE
);

DROP TABLE IF EXISTS `staking_history`;
CREATE TABLE `staking_history`
(
    `node_id` varchar(130) NOT NULL COMMENT 'Pledge node address',
    `staking_block_num` bigint(20) NOT NULL COMMENT 'Staking block height',
    `staking_tx_index` int(11) NOT NULL COMMENT 'Index for initiating staking transactions',
    `staking_hes` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Deposit during the hesitation period (AAA)',
    `staking_locked` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Pledge deposit during the locking period (AAA)',
    `staking_reduction` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Refunding the pledge (AAA)',
    `staking_reduction_epoch` int(11) NOT NULL DEFAULT '0' COMMENT 'Settlement cycle identifier',
    `node_name` varchar(256) NOT NULL,
    `node_icon` varchar(255) DEFAULT '' COMMENT 'Node avatar (associated with external_id, obtained by third-party software)',
    `external_id` varchar(255) NOT NULL DEFAULT '' COMMENT 'Third-party social software associated id',
    `external_name` varchar(128) DEFAULT NULL COMMENT 'Third-party social software associated user name',
    `staking_addr` varchar(42) NOT NULL COMMENT 'Account address that initiated staking',
    `benefit_addr` varchar(42) NOT NULL DEFAULT '' COMMENT 'Benefit address',
    `annualized_rate` double(64, 2) NOT NULL DEFAULT '0.00' COMMENT 'Estimated annualized rate',   `program_version`           varchar(10)    NOT NULL DEFAULT '0' COMMENT '程序版本',
    `big_version` varchar(10) NOT NULL DEFAULT '0' COMMENT 'Big program version',
    `web_site` varchar(255) NOT NULL DEFAULT '' COMMENT 'Node third-party home page',
    `details` varchar(256) NOT NULL,
    `join_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Join time',
    `leave_time` timestamp NULL DEFAULT NULL COMMENT 'leave time',
    `status` int(11) NOT NULL DEFAULT '1' COMMENT 'Node status: 1 candidate, 2 exiting, 3 exited, 4 locked',
    `is_consensus` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether the consensus cycle validator is: 1 yes, 2 no',
    `is_settle` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether the settlement cycle verifier: 1 yes, 2 no',
    `is_init` int(11) NOT NULL DEFAULT '2' COMMENT 'Whether it is a built-in candidate during chain initialization: 1 yes, 2 no',
    `stat_delegate_hes` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Unlocked delegate (AAA)',
    `stat_delegate_locked` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Locked delegate (AAA)',
    `stat_delegate_released` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Delegation to be extracted (AAA)',
    `block_reward_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Block reward statistics (incentive pool) (AAA)',
    `fee_reward_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Block reward statistics (handling fee) (AAA)',
    `staking_reward_value` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Staking reward (incentive pool) (AAA)',
    `predict_staking_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'The staking reward expected to be obtained in the current settlement cycle',
    `cur_cons_block_qty` bigint(20) NOT NULL DEFAULT '0' COMMENT 'Number of blocks produced in the current consensus cycle',
    `pre_cons_block_qty` bigint(20) NOT NULL DEFAULT '0' COMMENT 'Number of blocks produced in the last consensus cycle',
    `annualized_rate_info` longtext COMMENT 'Income and pledge information of recent settlement cycles',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `reward_per` int(11) NOT NULL DEFAULT '0' COMMENT 'Delegation reward ratio',
    `next_reward_per` int(11) NOT NULL DEFAULT '0' COMMENT 'Next settlement cycle commission reward ratio',
    `next_reward_per_mod_epoch` int(11) DEFAULT '0' COMMENT '[Next settlement cycle commission reward ratio] Modify the settlement cycle',
    `have_dele_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'The commission reward has been claimed (the initial value is equal to the [delegation reward claimed] of the previous historical pledge record)',
    `pre_dele_annualized_rate` double(64, 2) DEFAULT '0.00' COMMENT 'Estimated commission rate of return in the previous participation cycle',
    `dele_annualized_rate` double(64, 2) NOT NULL DEFAULT '0.00' COMMENT 'Estimated commission rate of return',
    `total_dele_reward` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'The total delegation reward of the node (the [total delegation reward of the node] of the previous historical pledge record + the reward obtained by the real-time query of the current pledge)',
    PRIMARY KEY (`node_id`, `staking_block_num`),
    KEY `staking_addr` (`staking_addr`) USING BTREE
);

DROP TABLE IF EXISTS `token`;
CREATE TABLE `token`
(`address` varchar(64) NOT NULL COMMENT 'Contract address',
 `type` varchar(64) NOT NULL COMMENT 'Contract type erc20 erc721',
 `name` varchar(64) DEFAULT NULL COMMENT 'Contract name',
 `symbol` varchar(64) DEFAULT NULL COMMENT 'Contract symbol',
 `total_supply` varchar(128) DEFAULT NULL COMMENT 'Total supply',
 `decimal` int(11) DEFAULT NULL COMMENT 'Contract precision',
 `is_support_erc165` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the erc165 interface is supported: 0-not supported 1-supported',
 `is_support_erc20` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the erc20 interface is supported: 0-not supported 1-supported',
 `is_support_erc721` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the erc721 interface is supported: 0-not supported 1-supported',
 `is_support_erc721_enumeration` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the erc721 enumeration interface is supported: 0-not supported 1-supported',
 `is_support_erc721_metadata` tinyint(1) NOT NULL COMMENT 'Whether the metadata interface is supported: 0-not supported 1-supported',
 `is_support_erc1155` tinyint(1) DEFAULT 0 COMMENT 'Whether the erc1155 interface is supported: 0-not supported 1-supported',
 `is_support_erc1155_metadata` tinyint(1) DEFAULT 0 COMMENT 'Whether the erc1155 metadata interface is supported: 0-not supported 1-supported',
 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
 `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
 `token_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'The number of transactions corresponding to the token',
 `holder` int(11) NOT NULL DEFAULT '0' COMMENT 'The number of holders corresponding to the token',
 `contract_destroy_block` bigint(20) DEFAULT NULL COMMENT 'Contract destruction block high',
 `contract_destroy_update` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the destroyed contract has been updated, 1 means yes, 0 means no, the default is 0',
 PRIMARY KEY (`address`),
 UNIQUE KEY `token_address` (`address`)
);

DROP TABLE IF EXISTS `token_holder`;
CREATE TABLE `token_holder`
(
    `token_address` varchar(64) NOT NULL COMMENT 'Contract address',
    `address` varchar(64) NOT NULL COMMENT 'User address',
    `balance` varchar(128) DEFAULT NULL COMMENT 'Address token balance, ERC20 is the amount, ERC721 is the tokenId number',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `token_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of transactions corresponding to erc721 token',
    PRIMARY KEY (`token_address`, `address`)
);

DROP TABLE IF EXISTS `tx_bak`;
CREATE TABLE `tx_bak`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `hash` varchar(72) NOT NULL COMMENT 'Transaction hash',
    `b_hash` varchar(72) DEFAULT NULL COMMENT 'block hash',
    `num` bigint(20) DEFAULT NULL COMMENT 'block height',
    `index` int(10) DEFAULT NULL COMMENT 'Transaction index',
    `time` timestamp NULL DEFAULT NULL COMMENT 'Transaction time',
    `nonce` varchar(255) DEFAULT NULL COMMENT 'random value',
    `status` int(1) DEFAULT NULL COMMENT 'Status, 1. Success, 2. Failure',
    `gas_price` varchar(255) DEFAULT NULL COMMENT 'gas price',
    `gas_used` varchar(255) DEFAULT NULL COMMENT 'gas cost',
    `gas_limit` varchar(255) DEFAULT NULL COMMENT 'gas limit',
    `from` varchar(42) DEFAULT NULL COMMENT 'from address',
    `to` varchar(42) DEFAULT NULL COMMENT 'to address',
    `value` varchar(255) DEFAULT NULL COMMENT 'value',
    `type` int(10) DEFAULT NULL COMMENT 'Transaction type',
    `cost` varchar(50) DEFAULT NULL COMMENT 'cost',
    `to_type` int(4) DEFAULT NULL COMMENT 'to address type',
    `seq` bigint(20) DEFAULT NULL COMMENT 'seq',
    `cre_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `upd_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `input` longtext,
    `info` longtext,
    `erc1155_tx_info` longtext COLLATE utf8mb4_unicode_ci COMMENT 'erc1155 transaction list information',
    `erc721_tx_info` longtext COMMENT 'erc721 transaction list information',
    `erc20_tx_info` longtext COMMENT 'erc20 transaction list information',
    `transfer_tx_info` longtext COMMENT 'Internal transfer transaction list information',
    `ppos_tx_info` longtext COMMENT 'ppos call transaction list information',
    `fail_reason` longtext,
    `contract_type` int(10) DEFAULT NULL COMMENT 'Contract type',
    `method` longtext,
    `contract_address` varchar(42) DEFAULT NULL COMMENT 'Contract address',
    PRIMARY KEY (`id`),
    KEY `idx_time` (`time`)
) COMMENT ='Transaction backup table';

DROP TABLE IF EXISTS `token_inventory`;
CREATE TABLE `token_inventory`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment id',
    `token_address` varchar(64) NOT NULL COMMENT 'Contract address',
    `token_id` varchar(128) NOT NULL COMMENT 'token id',
    `owner` varchar(64) NOT NULL COMMENT 'token id corresponds to the holder address',
    `name` varchar(256) DEFAULT NULL COMMENT 'Identifies the asset to which this NFT represents',
    `description` longtext COMMENT 'Describes the asset to which this NFT represents',
    `image` varchar(256) DEFAULT NULL COMMENT 'A URI pointing to a resource with mime type image/* representing the asset to which this NFT represents. Consider making any images at a width between 320 and 1080 pixels and aspect ratio between 1.91: 1 and 4:5 inclusive.',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `token_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'The number of transactions corresponding to tokenaddress and tokenid',
    `token_owner_tx_qty` int(11) DEFAULT '0' COMMENT 'The number of owner transactions corresponding to the tokenaddress and tokenid',
    `small_image` varchar(256) DEFAULT NULL COMMENT 'Thumbnail',
    `medium_image` varchar(256) DEFAULT NULL COMMENT 'medium thumbnail',
    `token_url` longtext COMMENT 'url',
    `retry_num` int(10) NOT NULL DEFAULT '0' COMMENT 'Number of retries',
    PRIMARY KEY (`id`),
    UNIQUE KEY `token_address` (`token_address`, `token_id`)
);

DROP TABLE IF EXISTS `tx_erc_20_bak`;
CREATE TABLE `tx_erc_20_bak`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `seq` bigint(20) NOT NULL COMMENT 'serial number ID',
    `name` varchar(64) NOT NULL COMMENT 'Contract name',
    `symbol` varchar(64) DEFAULT NULL COMMENT 'unit',
    `decimal` int(20) DEFAULT NULL COMMENT 'precision',
    `contract` varchar(42) NOT NULL COMMENT 'Contract address',
    `hash` varchar(72) NOT NULL COMMENT 'Transaction Hash',
    `from` varchar(42) NOT NULL COMMENT 'from address',
    `from_type` int(1) NOT NULL COMMENT 'Sender type',
    `to` varchar(42) NOT NULL COMMENT 'to address',
    `to_type` int(1) NOT NULL COMMENT 'Receiver type',
    `value` varchar(255) NOT NULL COMMENT 'Transaction value',
    `bn` bigint(20) DEFAULT NULL COMMENT 'block height',
    `b_time` datetime DEFAULT NULL COMMENT 'block time',
    `tx_fee` varchar(255) DEFAULT NULL COMMENT 'handling fee',
    `remark` longtext COMMENT 'note',
    PRIMARY KEY (`id`)
) COMMENT ='erc20 transaction backup table';

DROP TABLE IF EXISTS `tx_erc_721_bak`;
CREATE TABLE `tx_erc_721_bak`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `seq` bigint(20) NOT NULL COMMENT 'serial number ID',
    `name` varchar(64) NOT NULL COMMENT 'Contract name',
    `symbol` varchar(64) DEFAULT NULL COMMENT 'unit',
    `decimal` int(20) DEFAULT NULL COMMENT 'precision',
    `contract` varchar(42) NOT NULL COMMENT 'Contract address',
    `token_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'tokenId',
    `hash` varchar(72) NOT NULL COMMENT 'Transaction Hash',
    `from` varchar(42) NOT NULL COMMENT 'from address',
    `from_type` int(1) NOT NULL COMMENT 'Sender type',
    `to` varchar(42) NOT NULL COMMENT 'to address',
    `to_type` int(1) NOT NULL COMMENT 'Receiver type',
    `value` varchar(255) NOT NULL COMMENT 'Transaction value',
    `bn` bigint(20) DEFAULT NULL COMMENT 'block height',
    `b_time` datetime DEFAULT NULL COMMENT 'block time',
    `tx_fee` varchar(255) DEFAULT NULL COMMENT 'handling fee',
    `remark` longtext COMMENT 'note',
    PRIMARY KEY (`id`)
) COMMENT ='erc721 transaction backup table';

DROP TABLE IF EXISTS `tx_delegation_reward_bak`;
CREATE TABLE `tx_delegation_reward_bak`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `hash` varchar(72) NOT NULL COMMENT 'Transaction hash',
    `bn` bigint(20) DEFAULT NULL COMMENT 'block height',
    `addr` varchar(42) DEFAULT NULL COMMENT 'address',
    `time` timestamp NULL DEFAULT NULL COMMENT 'time',
    `cre_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `upd_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `extra` longtext,
    `extra_clean` longtext,
    PRIMARY KEY (`id`)
) COMMENT ='Reward Backup Table';

DROP TABLE IF EXISTS `vote`;
CREATE TABLE `vote`
(
    `hash` varchar(72) NOT NULL COMMENT 'Voting transaction Hash (if this value contains "-", it means that the voting operation is performed through an ordinary contract agent, and the one before the "-" sign is the contract transaction hash)',
    `node_id` varchar(130) NOT NULL COMMENT 'Vote validator (node ID)',
    `node_name` varchar(128) NOT NULL COMMENT 'Voting validator name',
    `option` int(11) NOT NULL COMMENT 'Voting options: 1 support, 2 oppose, 3 abstain',
    `proposal_hash` varchar(72) NOT NULL COMMENT 'Proposal Transaction Hash',
    `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Proposal time',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`hash`),
    KEY `verifier` (`node_id`) USING BTREE
);

DROP TABLE IF EXISTS `internal_address`;
CREATE TABLE `internal_address`
(
    `name` varchar(64) NOT NULL DEFAULT 'Foundation address' COMMENT 'Address name',
    `address` varchar(42) NOT NULL COMMENT 'address',
    `type` int(11) NOT NULL DEFAULT '0' COMMENT 'Address type: 0-Foundation account 1-Lock contract address 2-Pledge contract 3-Incentive pool contract 6-Delegated reward pool contract',
    `balance` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Balance (AAA)',
    `restricting_balance` decimal(65, 0) NOT NULL DEFAULT '0' COMMENT 'Locked balance (AAA)',
    `is_show` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Whether it is used for display 0-no 1-yes',
    `is_calculate` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Whether it is used for calculation 0-no 1-yes',
    `create_id` bigint(20) NOT NULL DEFAULT '1' COMMENT 'Creator',
    `create_name` varchar(64) NOT NULL DEFAULT 'admin' COMMENT 'creator name',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_id` bigint(20) NOT NULL DEFAULT '1' COMMENT 'Updater',
    `update_name` varchar(64) NOT NULL DEFAULT 'admin' COMMENT 'Updater name',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`address`),
    KEY `type` (`type`) USING BTREE
);

DROP TABLE IF EXISTS `point_log`;
CREATE TABLE `point_log`
(
    `id` int(64) NOT NULL AUTO_INCREMENT COMMENT 'primary key id',
    `type` int(1) NOT NULL COMMENT 'type,1-mysql,2-es',
    `name` varchar(255) NOT NULL COMMENT 'table name or index name',
    `desc` varchar(255) NOT NULL COMMENT 'Use description',
    `position` varchar(128) NOT NULL COMMENT 'Statistical position (MySQL is auto-incrementing id, es is seq)',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`)
) COMMENT ='Breakpoint statistics table';

DROP TABLE IF EXISTS `token_expand`;
CREATE TABLE `token_expand`
(
    `address` varchar(64) NOT NULL COMMENT 'Contract address',
    `icon` text COMMENT 'Contract icon',
    `web_site` varchar(256) DEFAULT NULL COMMENT 'Contract address',
    `details` varchar(256) DEFAULT NULL COMMENT 'Contract official website',
    `is_show_in_aton` tinyint(1) DEFAULT '0' COMMENT 'Whether to display in aton, 0-hide 1-show',
    `is_show_in_scan` tinyint(1) DEFAULT '0' COMMENT 'Whether to display in scan, 0-hide 1-show',
    `is_can_transfer` tinyint(1) DEFAULT '0' COMMENT 'Whether it can be transferred 0-cannot be transferred 1-can be transferred',
    `create_id` bigint(20) NOT NULL COMMENT 'Creator',
    `create_name` varchar(50) NOT NULL COMMENT 'creator name',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_id` bigint(20) NOT NULL COMMENT 'Updater',
    `update_name` varchar(50) NOT NULL COMMENT 'Updater name',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `is_show_in_aton_admin` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether to display in the aton management console, 1 means display, 0 does not display, the default is 0',
    `is_show_in_scan_admin` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether to display in the scan management console, 1 means display, 0 does not display, the default is 0',
    PRIMARY KEY (`address`)
);

DROP TABLE IF EXISTS `token_1155_holder`;
CREATE TABLE `token_1155_holder`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment id',
    `token_address` varchar(64) NOT NULL COMMENT 'Contract address',
    `token_id` varchar(128) NOT NULL COMMENT 'ERC1155 tokenId',
    `address` varchar(64) NOT NULL COMMENT 'User address',
    `balance` varchar(128) DEFAULT NULL COMMENT 'Address token balance, job update',
    `token_owner_tx_qty` int(11) DEFAULT '0' COMMENT 'Number of owner transactions, agent update',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_tokenAddress_tokenId_address (`token_address`, `token_id`, `address`)
);

DROP TABLE IF EXISTS `token_1155_inventory`;
CREATE TABLE `token_1155_inventory`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment id',
    `token_address` varchar(64) NOT NULL COMMENT 'Contract address',
    `token_id` varchar(128) NOT NULL COMMENT 'token id',
    `token_url` longtext COMMENT 'url',
    `decimal` int(20) DEFAULT NULL COMMENT 'precision',
    `name` varchar(256) DEFAULT NULL COMMENT 'Identifies the asset to which this NFT represents',
    `description` longtext COMMENT 'Describes the asset to which this NFT represents',
    `image` varchar(256) DEFAULT NULL COMMENT 'A URI pointing to a resource with mime type image/* representing the asset to which this NFT represents. Consider making any images at a width between 320 and 1080 pixels and aspect ratio between 1.91: 1 and 4:5 inclusive.',
    `small_image` varchar(256) DEFAULT NULL COMMENT 'Thumbnail',
    `medium_image` varchar(256) DEFAULT NULL COMMENT 'medium thumbnail',
    `token_tx_qty` int(11) NOT NULL DEFAULT '0' COMMENT 'The number of transactions of tokenId, accumulated with the contract, is the number of transactions of the contract, agent update',
    `retry_num` int(10) NOT NULL DEFAULT '0' COMMENT 'Number of retries',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tokenAddress_tokenId` (`token_address`, `token_id`)
);

DROP TABLE IF EXISTS `tx_erc_1155_bak`;
CREATE TABLE `tx_erc_1155_bak`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `seq` bigint(20) NOT NULL COMMENT 'serial number ID',
    `contract` varchar(42) NOT NULL COMMENT 'Contract address',
    `token_id` varchar(255) NOT NULL COMMENT 'tokenId',
    `hash` varchar(72) NOT NULL COMMENT 'Transaction Hash',
    `from` varchar(42) NOT NULL COMMENT 'from address',
    `from_type` int(1) NOT NULL COMMENT 'Sender type',
    `to` varchar(42) NOT NULL COMMENT 'to address',
    `to_type` int(1) NOT NULL COMMENT 'Receiver type',
    `value` varchar(255) NOT NULL COMMENT 'Transaction value',
    `bn` bigint(20) DEFAULT NULL COMMENT 'block height',
    `b_time` datetime DEFAULT NULL COMMENT 'block time',
    `tx_fee` varchar(255) DEFAULT NULL COMMENT 'handling fee',
    `remark` longtext COMMENT 'note',
    PRIMARY KEY (`id`)
) COMMENT ='erc1155 transaction backup table';

DROP TABLE IF EXISTS `micro_node`;
CREATE TABLE `micro_node` (
                              `node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'node id',
                              `amount` decimal(65,0) DEFAULT NULL COMMENT 'Pledge amount',
                              `operation_addr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'operation address',
                              `beneficiary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Benefits account',
                              `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Node name',
                              `details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Node detailed description',
                              `electron_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'The address of the micro node providing RPC services to the outside world',
                              `p2p_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Information about establishing p2p connections between nodes',
                              `rpc_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'The address of the node providing RPC services to the outside world',
                              `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Program version',
                              `is_operator` int DEFAULT NULL COMMENT 'Whether the node operates the node: 0-no; 1-yes',
                              `node_status` int DEFAULT NULL COMMENT 'Node status: 1-candidate; 2-exited',
                              `bubble_id` bigint DEFAULT '0' COMMENT 'Bubble_id:0-bubble is not formed',
                              `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                              `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
                              PRIMARY KEY (`node_id`) USING BTREE
) COMMENT='Micronode table';



DROP TABLE IF EXISTS `micro_node_opt_bak`;
CREATE TABLE `micro_node_opt_bak` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment id',
                                      `node_id` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'node id',
                                      `type` int NOT NULL COMMENT 'Operation type: 1 create, 2 modify, 3 exit',
                                      `tx_hash` varchar(72) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Transaction hash',
                                      `b_num` bigint DEFAULT NULL COMMENT 'Transaction block number',
                                      `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'time',
                                      `cre_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                                      `upd_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
                                      PRIMARY KEY (`id`),
                                      KEY `node_id` (`node_id`) USING BTREE,
                                      KEY `tx_hash` (`tx_hash`) USING BTREE,
                                      KEY `block_number` (`b_num`) USING BTREE
) COMMENT='Micronode behavior table';

DROP TABLE IF EXISTS `addr_game`;
CREATE TABLE `addr_game` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'record id',
                             `address` varchar(42) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'address',
                             `round_id` bigint DEFAULT NULL COMMENT 'round id',
                             `game_id` bigint DEFAULT NULL COMMENT 'game id',
                             `game_contract_address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Game contract address',
                             `bubble_id` bigint DEFAULT '0' COMMENT 'Bubble_id:0-bubble is not formed',
                             `status` int DEFAULT NULL COMMENT 'Game status: 1-start; 0-end',
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                             `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
                             PRIMARY KEY (`id`) USING BTREE
) COMMENT='Address Game Table';

DROP TABLE IF EXISTS `game`;
CREATE TABLE `game` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'game id',
                        `contract_address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Contract address',
                        `name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'game name',
                        `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Game URL',
                        `introduce` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Game introduction',
                        `game_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Game type',
                        `status` int DEFAULT NULL COMMENT 'Status: 1-valid; 0-invalid',
                        `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                        `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
                        PRIMARY KEY (`id`)
) COMMENT='game table';

DROP TABLE IF EXISTS `round`;
CREATE TABLE `round` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'record id',
                         `game_id` bigint DEFAULT NULL COMMENT 'game id',
                         `round_id` bigint DEFAULT NULL COMMENT 'round id',
                         `bubble_id` bigint DEFAULT NULL COMMENT 'subnetwork id',
                         `creator` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'round creation address',
                         `token_address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Token contract address',
                         `token_symbol` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Token name',
                         `token_decimal` int DEFAULT NULL COMMENT 'Token precision',
                         `token_rpc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Token rpc address',
                         `status` int DEFAULT NULL COMMENT 'Status: 1-start; 0-end',
                         `create_time` timestamp NULL DEFAULT NULL COMMENT 'Creation time',
                         `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Creation time',
                         PRIMARY KEY (`id`)
) COMMENT='turn table';

-- Initialization data
-- There are also some foundation addresses that are manually imported by operation and maintenance.

INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (1, 1, 'n_opt_bak', 'Breakpoint record of node operation migrated to es', '0', '2021-12-01 07:50:41', '2021-12-01 07:50:41 ');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (2, 1, 'tx_bak', 'Breakpoint record of counting the number of transactions in the address table from the transaction backup table', '0', '2021-12-03 06:33:27', '2021-12-03 06 :33:27');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (3, 1, 'tx_20_bak', 'Breakpoint records from the erc20 transaction backup table to count the number of transactions in the address table and token table', '0', '2021-12-06 02:47:26', '2021- 12-06 02:47:26');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (4, 1, 'tx_721_bak', 'Breakpoint records from the erc721 transaction backup table to count the number of transactions in the address table and token table', '0', '2021-12-06 02:47:41', '2021- 12-06 02:47:41');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (5, 1, 'tx_20_bak', 'Breakpoint record of TokenHolder balance statistics from erc20 transaction backup table', '0', '2021-12-06 10:23:58', '2021-12-06 10: 25:49');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (6, 1, 'tx_721_bak', 'Breakpoint record of counting the number of TokenHolder holders from the erc721 transaction backup table', '0', '2021-12-06 10:25:34', '2021-12- 06 10:25:39');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (7, 1, 'token_inventory', 'Incremental update of token inventory information breakpoint record', '0', '2021-12-10 02:44:32', '2021-12-10 02:44:32 ');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (8, 1, 'token_1155_holder', 'Breakpoint record for counting the number of TokenHolder holders', '0', '2021-12-06 02:47:41', '2021-12-06 02:47: 41');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (9, 1, 'token_1155_inventory', 'Incremental update of token1155 inventory information breakpoint record', '0', '2021-12-10 02:44:32', '2021-12-10 02:44:32 ');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (10, 1, 'tx_1155_bak', 'Breakpoint records from the erc1155 transaction backup table to count the number of transactions in the address table and token table', '0', '2021-12-10 02:44:32', '2021- 12-10 02:44:32');