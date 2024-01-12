package com.turn.browser.task;

import com.turn.browser.dao.custommapper.CustomStakingHistoryMapper;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.dao.entity.StakingExample;
import com.turn.browser.dao.entity.StakingHistory;
import com.turn.browser.dao.mapper.StakingMapper;
import com.turn.browser.utils.AppStatusUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: Migrate historical data in the pledge table to the database task
 */
@Component
@Slf4j
public class StakingMigrateTask {

    @Resource
    private StakingMapper stakingMapper;

    @Resource
    private CustomStakingHistoryMapper customStakingHistoryMapper;

    /**
     * Migrate historical data in the pledge table to the database task
     * Executed every 30 seconds
     *
     * @param :
     * @return: void
     */
    @XxlJob("stakingMigrateJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    void stakingMigrate() {
        // Only perform tasks when the program is running normally
        if (AppStatusUtil.isRunning()) start();
    }

    protected void start() {
        try {
            StakingExample stakingExample = new StakingExample();
            stakingExample.createCriteria().andStatusEqualTo(3);
            List<Staking> stakingList = stakingMapper.selectByExample(stakingExample);
            Set<StakingHistory> stakingHistoryList = new HashSet<>();
            if (!stakingList.isEmpty()) {
                stakingList.forEach(staking -> {
                    StakingHistory stakingHistory = new StakingHistory();
                    BeanUtils.copyProperties(staking, stakingHistory);
                    stakingHistoryList.add(stakingHistory);
                });
                customStakingHistoryMapper.batchInsertOrUpdateSelective(stakingHistoryList, StakingHistory.Column.values());
            }
            XxlJobHelper.handleSuccess("The task of migrating historical data in the pledge table to the database was successful");
        } catch (Exception e) {
            log.error("Exception in the task of migrating historical data in the pledge table to the database", e);
            throw e;
        }
    }

}