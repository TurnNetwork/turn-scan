package com.turn.browser.cache;

import cn.hutool.core.util.ObjectUtil;
import com.turn.browser.bean.ConfigChange;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.elasticsearch.dto.Block;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Network statistics cache
 */
@Slf4j
@Component
@Data
public class NetworkStatCache {

    private NetworkStat networkStat = new NetworkStat();

    @Autowired
    private TpsCalcCache tpsCalcCache;

    /**
     * Update network statistics based on block dimensions
     *
     * @param block
     */
    public void updateByBlock(Block block) {
        this.tpsCalcCache.updateIfNotHandle(block);
        int tps = this.tpsCalcCache.getTps();
        int maxTps = this.tpsCalcCache.getMaxTps();
        this.networkStat.setCurTps(tps);
        if (maxTps > this.networkStat.getMaxTps()) {
            this.networkStat.setMaxTps(maxTps);
        }
    }

    /**
     * Update network statistics based on tasks
     *
     * @param turnValue:
     * @param availableStaking:
     * @param totalValue: Total number of real-time pledge orders
     * @param stakingValue: Total number of real-time pledges
     * @return: void
     */
    public void updateByTask(BigDecimal turnValue, BigDecimal availableStaking, BigDecimal totalValue, BigDecimal stakingValue) {
        if (ObjectUtil.isNotNull(turnValue) && turnValue.compareTo(BigDecimal.ZERO) > 0) {
            this.networkStat.setTurnValue(turnValue);
        }
        this.networkStat.setAvailableStaking(availableStaking);
        this.networkStat.setStakingDelegationValue(totalValue);
        this.networkStat.setStakingValue(stakingValue);
    }

    /**
     * Update network statistics based on additional issuance or settlement cycle changes
     */
    public void updateByEpochChange(ConfigChange configChange) {
        if (configChange.getBlockReward() != null) this.networkStat.setBlockReward(configChange.getBlockReward());
        if (configChange.getYearStartNum() != null) this.networkStat.setAddIssueBegin(configChange.getYearStartNum().longValue());
        if (configChange.getYearEndNum() != null) this.networkStat.setAddIssueEnd(configChange.getYearEndNum().longValue());
        if (configChange.getSettleStakeReward() != null) this.networkStat.setSettleStakingReward(configChange.getSettleStakeReward());
        if (configChange.getStakeReward() != null) this.networkStat.setStakingReward(configChange.getStakeReward());
        if (configChange.getAvgPackTime() != null) this.networkStat.setAvgPackTime(configChange.getAvgPackTime().longValue());
        if (StringUtils.isNotBlank(configChange.getIssueRates())) this.networkStat.setIssueRates(configChange.getIssueRates());
    }

    /**
     * Initialize network cache
     *
     * @param networkStat
     * @return void
     */
    public void init(NetworkStat networkStat) {
        this.networkStat = networkStat;
    }

}
