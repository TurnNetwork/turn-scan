package com.turn.browser.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.turn.browser.dao.entity.NOptBak;
import com.turn.browser.dao.entity.NOptBakExample;
import com.turn.browser.dao.entity.PointLog;
import com.turn.browser.dao.mapper.NOptBakMapper;
import com.turn.browser.dao.mapper.PointLogMapper;
import com.turn.browser.service.elasticsearch.EsNodeOptService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class NodeOptTask {

    @Resource
    private PointLogMapper pointLogMapper;

    @Resource
    private NOptBakMapper nOptBakMapper;

    @Resource
    private EsNodeOptService esNodeOptService;

    /**
     * Migrate node operation backup table to ES tasks
     * Executed every 10 minutes
     *
     * @param :
     * @return: void
     */
    @XxlJob("nodeOptMoveToESJobHandler")
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void nodeOptMoveToES() throws Exception {
        try {
            int batchSize = Convert.toInt(XxlJobHelper.getJobParam(), 10);
            PointLog pointLog = pointLogMapper.selectByPrimaryKey(1);
            long oldPosition = Convert.toLong(pointLog.getPosition());
            XxlJobHelper.log("The current page number is [{}], the breakpoint is [{}]", batchSize, oldPosition);
            NOptBakExample nOptBakExample = new NOptBakExample();
            nOptBakExample.setOrderByClause("id asc limit " + batchSize);
            nOptBakExample.createCriteria().andIdGreaterThan(oldPosition);
            List<NOptBak> nOptBakList = nOptBakMapper.selectByExample(nOptBakExample);
            if (CollUtil.isNotEmpty(nOptBakList)) {
                Set<NOptBak> nodeOpts = new HashSet<>(nOptBakList);
                esNodeOptService.save(nodeOpts);
                NOptBak lastNOptBak = CollUtil.getLast(nOptBakList);
                pointLog.setPosition(lastNOptBak.getId().toString());
                pointLogMapper.updateByPrimaryKeySelective(pointLog);
                XxlJobHelper.log("Node operation backup table was successfully migrated to ES, breakpoint [{}]->[{}]", oldPosition, pointLog.getPosition());
            } else {
                XxlJobHelper.log("No node backup information found for current breakpoint [{}]", oldPosition);
            }
            XxlJobHelper.handleSuccess("Node operation backup table was successfully migrated to ES");
        } catch (Exception e) {
            log.error("Node operation backup table migration to ES exception", e);
            throw e;
        }
    }

}
