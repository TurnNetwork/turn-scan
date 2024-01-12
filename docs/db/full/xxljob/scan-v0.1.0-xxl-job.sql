#
# XXL-JOB v2.3.1-SNAPSHOT
# Copyright (c) 2015-present, xuxueli.

CREATE database if NOT EXISTS `xxl_job` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `xxl_job`;

SET NAMES utf8mb4;

CREATE TABLE `xxl_job_info` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `job_group` int(11) NOT NULL COMMENT 'Executor primary key ID',
                                `job_desc` varchar(255) NOT NULL,
                                `add_time` datetime DEFAULT NULL,
                                `update_time` datetime DEFAULT NULL,
                                `author` varchar(64) DEFAULT NULL COMMENT 'author',
                                `alarm_email` varchar(255) DEFAULT NULL COMMENT 'Alarm email',
                                `schedule_type` varchar(50) NOT NULL DEFAULT 'NONE' COMMENT 'Scheduling type',
                                `schedule_conf` varchar(128) DEFAULT NULL COMMENT 'Scheduling configuration, the value meaning depends on the scheduling type',
                                `misfire_strategy` varchar(50) NOT NULL DEFAULT 'DO_NOTHING' COMMENT 'Scheduling expiration strategy',
                                `executor_route_strategy` varchar(50) DEFAULT NULL COMMENT 'Executor routing strategy',
                                `executor_handler` varchar(255) DEFAULT NULL COMMENT 'executor task handler',
                                `executor_param` varchar(512) DEFAULT NULL COMMENT 'Executor task parameter',
                                `executor_block_strategy` varchar(50) DEFAULT NULL COMMENT 'Blocking processing strategy',
                                `executor_timeout` int(11) NOT NULL DEFAULT '0' COMMENT 'Task execution timeout, unit seconds',
                                `executor_fail_retry_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of failed retries',
                                `glue_type` varchar(50) NOT NULL COMMENT 'GLUE type',
                                `glue_source` mediumtext COMMENT 'GLUE source code',
                                `glue_remark` varchar(128) DEFAULT NULL COMMENT 'GLUE remark',
                                `glue_updatetime` datetime DEFAULT NULL COMMENT 'GLUE update time',
                                `child_jobid` varchar(255) DEFAULT NULL COMMENT 'Subtask ID, multiple commas separated',
                                `trigger_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Scheduling status: 0-stop, 1-running',
                                `trigger_last_time` bigint(13) NOT NULL DEFAULT '0' COMMENT 'Last scheduled time',
                                `trigger_next_time` bigint(13) NOT NULL DEFAULT '0' COMMENT 'Next scheduling time',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `xxl_job_log` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT,
                               `job_group` int(11) NOT NULL COMMENT 'Executor primary key ID',
                               `job_id` int(11) NOT NULL COMMENT 'Task, primary key ID',
                               `executor_address` varchar(255) DEFAULT NULL COMMENT 'Executor address, the address of this execution',
                               `executor_handler` varchar(255) DEFAULT NULL COMMENT 'executor task handler',
                               `executor_param` varchar(512) DEFAULT NULL COMMENT 'Executor task parameter',
                               `executor_sharding_param` varchar(20) DEFAULT NULL COMMENT 'Executor task sharding parameters, format such as 1/2',
                               `executor_fail_retry_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Number of failed retries',
                               `trigger_time` datetime DEFAULT NULL COMMENT 'Scheduling-time',
                               `trigger_code` int(11) NOT NULL COMMENT 'Scheduling-Result',
                               `trigger_msg` text COMMENT 'Scheduling-Log',
                               `handle_time` datetime DEFAULT NULL COMMENT 'execution-time',
                               `handle_code` int(11) NOT NULL COMMENT 'execution-status',
                               `handle_msg` text COMMENT 'execution-log',
                               `alarm_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Alarm status: 0-default, 1-no alarm required, 2-alarm successful, 3-alarm failed',
                               PRIMARY KEY (`id`),
                               KEY `I_trigger_time` (`trigger_time`),
                               KEY `I_handle_code` (`handle_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `xxl_job_log_report` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT,
                                      `trigger_day` datetime DEFAULT NULL COMMENT 'Scheduling-time',
                                      `running_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Running-log number',
                                      `suc_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Execution successful - number of logs',
                                      `fail_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Execution failure - number of logs',
                                      `update_time` datetime DEFAULT NULL,
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `i_trigger_day` (`trigger_day`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `xxl_job_logglue` (
                                   `id`          int(11) NOT NULL AUTO_INCREMENT,
                                   `job_id`      int(11) NOT NULL COMMENT 'Task primary key ID ',
                                   `glue_type`   varchar(50) DEFAULT NULL COMMENT '' GLUE type '',
                                   `glue_source` mediumtext COMMENT '' GLUE source code '',
                                   `glue_remark` varchar(128) NOT NULL COMMENT '' GLUE remark '',
                                   `add_time` datetime DEFAULT NULL,
                                   `update_time` datetime DEFAULT NULL,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `xxl_job_registry` (
                                    `id` int(11) NOT NULL AUTO_INCREMENT,
                                    `registry_group` varchar(50) NOT NULL,
                                    `registry_key` varchar(255) NOT NULL,
                                    `registry_value` varchar(255) NOT NULL,
                                    `update_time` datetime DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `i_g_k_v` (`registry_group`,`registry_key`,`registry_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `xxl_job_group` (
                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                 `app_name` varchar(64) NOT NULL COMMENT 'Executor AppName',
                                 `title` varchar(12) NOT NULL COMMENT 'executor name',
                                 `address_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Executor address type: 0=automatic registration, 1=manual entry',
                                 `address_list` text COMMENT 'Executor address list, multiple addresses comma separated',
                                 `update_time` datetime DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `xxl_job_user` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `username` varchar(50) NOT NULL COMMENT 'Account',
                                `password` varchar(50) NOT NULL COMMENT 'password',
                                `role` tinyint(4) NOT NULL COMMENT 'Role: 0-normal user, 1-administrator',
                                `permission` varchar(255) DEFAULT NULL COMMENT 'Permission: list of executor IDs, multiple commas separated',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `i_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `xxl_job_lock` (
                                `lock_name` varchar(50) NOT NULL COMMENT 'lock name',
                                PRIMARY KEY (`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `xxl_job_group`(`id`, `app_name`, `title`, `address_type`, `address_list`, `update_time`) VALUES (1, 'xxl-job-executor-sample', 'Sample executor', 0, NULL, '2018-11-03 22:21:31' );
INSERT INTO `xxl_job_info`(`id`, `job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`, `executor_block_strategy`, `executor_timeout`, `executor_fail_retry_count`, `glue_type`, `glue_source`, `glue_remark`, `glue_updatetime`, `child_jobid`) VALUES (1, 1, 'Test task 1 ', '2018-11-03 22:21:31', '2018-11-03 22:21:31', 'XXL', '', 'CRON', '0 0 0 * * ? *', ' DO_NOTHING', 'FIRST', 'demoJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2018-11-03 22:21:31', '' );
INSERT INTO `xxl_job_user`(`id`, `username`, `password`, `role`, `permission`) VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, NULL);
INSERT INTO `xxl_job_lock` ( `lock_name`) VALUES ( 'schedule_lock');

INSERT INTO `xxl_job_group`(`id`, `app_name`, `title`, `address_type`, `address_list`, `update_time`) VALUES (2, 'turn-scan-job', 'scan-job scheduled task', 0, NULL, '2021-12-09 09:53:38');
INSERT INTO `xxl_job_group`(`id`, `app_name`, `title`, `address_type`, `address_list`, `update_time`) VALUES (3, 'turn-scan-agent', 'agent scheduled task', 0, NULL, '2021-12-09 09:53:38');

INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Migrate historical data in the pledge table to the database task', '2021-11-29 14:37:06', '2021-11-29 15:17:47', 'admin', '', 'CRON ', '0/30 * * * * ?', 'DO_NOTHING', 'FIRST', 'stakingMigrateJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', ' 2021-11-29 14:37:06', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Proposal details update task', '2021-11-29 14:38:46', '2021-11-30 10:48:55', 'admin', '', 'CRON', '0/15 * * * * ?', 'DO_NOTHING', 'FIRST', 'proposalDetailUpdateJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11-29 14 :38:46', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Update foundation account balance', '2021-11-29 14:43:28', '2021-11-29 16:08:44', 'admin', '', 'CRON', '0 0 /6 * * * ?', 'DO_NOTHING', 'FIRST', 'balanceUpdateJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11-29 14:43:28', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Update built-in contract account balance', '2021-11-29 14:44:51', '2021-11-29 15:17:39', 'admin', '', 'CRON', '0/ 10 * * * * ?', 'DO_NOTHING', 'FIRST', 'updateContractAccountJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11-29 14:44:51', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Address table information supplement', '2021-11-29 14:48:41', '2021-12-01 17:38:23', 'admin', '', 'CRON', '0/5 * * * * ?', 'DO_NOTHING', 'FIRST', 'addressUpdateJobHandler', '1000', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11-29 14:48:41', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Migrate historical data in the commission table to ES tasks', '2021-11-30 10:44:55', '2021-11-30 10:44:55', 'admin', '', 'CRON ', '0/30 * * * * ?', 'DO_NOTHING', 'FIRST', 'delegateMigrateJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', ' 2021-11-30 10:44:55', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Node table supplement', '2021-11-30 10:47:41', '2021-11-30 10:47:41', 'admin', '', 'CRON', '0/5 * * * * ?', 'DO_NOTHING', 'FIRST', 'nodeUpdateJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11-30 10: 47:41', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Proposal information update task', '2021-11-30 10:48:30', '2021-11-30 10:48:30', 'admin', '', 'CRON', '0/15 * * * * ?', 'DO_NOTHING', 'FIRST', 'proposalInfoJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11-30 10 :48:30', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Total supply of full update tokens', '2021-11-30 10:50:12', '2021-11-30 10:50:12', 'admin', '', 'CRON', ' 0 */5 * * * ?', 'DO_NOTHING', 'FIRST', 'totalUpdateTokenTotalSupplyJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11 -30 10:50:12', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Incremental update of token holder balance', '2021-11-30 10:51:09', '2021-12-07 11:14:34', 'admin', '', 'CRON', '0 */1 * * * ?', 'DO_NOTHING', 'FIRST', 'incrementUpdateTokenHolderBalanceJobHandler', '500', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021 -11-30 10:51:09', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark`,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values  ( 2, 'Full update of token holder balance', '2021-11-30 10:51:57', '2021-11-30 10:51:57', 'admin', '', 'CRON', ' 0 0 0 * * ?', 'DO_NOTHING', 'FIRST', 'totalUpdateTokenHolderBalanceJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11-30 10:51 :57', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark`,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values  ( 2, 'Full update of token inventory information', '2021-11-30 10:53:01', '2021-12-01 18:06:37', 'admin', '', 'CRON', '0 0 1 */1 * ?', 'DO_NOTHING', 'FIRST', 'totalUpdateTokenInventoryJobHandler', '100', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11- 30 10:53:01', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Incremental update of token inventory information', '2021-11-30 10:53:42', '2021-12-01 18:07:33', 'admin', '', 'CRON', '0 */1 * * * ?', 'DO_NOTHING', 'FIRST', 'incrementUpdateTokenInventoryJobHandler', '10', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11 -30 10:53:42', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Update balance of destroyed 721 contract', '2021-11-30 10:54:19', '2021-11-30 10:54:19', 'admin', '', 'CRON', '0 */10 * * * ?', 'DO_NOTHING', 'FIRST', 'contractDestroyUpdateBalanceJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-11- 30 10:54:19', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Node operation backup table migration to ES task', '2021-12-01 16:54:32', '2021-12-01 17:02:57', 'admin', '', 'CRON', '0 */10 * * * ?', 'DO_NOTHING', 'FIRST', 'nodeOptMoveToESJobHandler', '100', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021 -12-01 16:54:32', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Update erc transaction number', '2021-12-06 16:04:26', '2021-12-06 16:04:26', 'admin', '', 'CRON', '0 */ 5 * * * ?', 'DO_NOTHING', 'FIRST', 'updateTokenQtyJobHandler', '500', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-12-06 16:04:26', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 2, 'Update address transaction number', '2021-12-06 17:06:33', '2021-12-06 17:06:33', 'admin', '', 'CRON', '0/30 * * * * ?', 'DO_NOTHING', 'FIRST', 'updateAddressQtyJobHandler', '500', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-12-06 17:06:33', '', 0, 0, 0);
INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 3, 'Update transaction statistics', '2021-12-06 17:11:28', '2021-12-06 18:17:41', 'admin', '', 'CRON', '0 */ 1 * * * ?', 'DO_NOTHING', 'FIRST', 'updateNetworkQtyJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-12-06 17 :11:28', '', 0, 0, 0);

INSERT INTO `xxl_job_info`(`job_group`, `job_desc`, `add_time`, `update_time`, `author`, `alarm_email`, `schedule_type`, `schedule_conf`, `misfire_strategy`, `executor_route_strategy`, `executor_handler`, `executor_param`,` executor_block_strategy`, `executor_timeout`,` executor_fail_retry_count`, `glue_source`,` glue_remark `,` glue_updatestime`, `child_jobid`,` trigger_status`, `trigger_last_time`,` trigger_next_time`) Values ( 3, 'Network statistics related information update task', '2021-12-07 10:45:13', '2021-12-07 10:45:13', 'admin', '', 'CRON', '0 /5 * * * * ?', 'DO_NOTHING', 'FIRST', 'networkStatUpdateJobHandler', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE code initialization', '2021-12- 07 10:45:13', '', 0, 0, 0);

commit;
