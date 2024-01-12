package com.turn.browser.task;

import com.turn.browser.bean.CustomDelegation;
import com.turn.browser.dao.entity.Delegation;
import com.turn.browser.dao.entity.DelegationExample;
import com.turn.browser.dao.mapper.DelegationMapper;
import com.turn.browser.service.elasticsearch.EsDelegationService;
import com.turn.browser.utils.AppStatusUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Migrate historical data in the commission table to ES tasks
 */
@Component
@Slf4j
public class DelegateMigrateTask {

    @Resource
    private DelegationMapper delegationMapper;

    @Resource
    private EsDelegationService esDelegationService;

    /**
     * Migrate historical data in the commission table to ES tasks
     * Executed every 30 seconds
     *
     * @param :
     * @return: void
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    @XxlJob("delegateMigrateJobHandler")
    public void delegateMigrate() throws Exception {
        // Only perform tasks when the program is running normally
        if (AppStatusUtil.isRunning()) start();
    }

    protected void start() throws Exception {
        try {
            DelegationExample delegationExample = new DelegationExample();
            delegationExample.createCriteria().andIsHistoryEqualTo(CustomDelegation.YesNoEnum.YES.getCode());
            List<Delegation> delegationList = delegationMapper.selectByExample(delegationExample);
            if (delegationList.isEmpty()) return;
            Set<Delegation> delegationSet = new HashSet<>(delegationList);
            esDelegationService.save(delegationSet);
            delegationMapper.batchDeleteIsHistory(delegationList);
            XxlJobHelper.handleSuccess("Delegation history migration to ES completed");
        } catch (Exception e) {
            log.error("Delegation history migration to ES exception", e);
            throw e;
        }
    }

}
