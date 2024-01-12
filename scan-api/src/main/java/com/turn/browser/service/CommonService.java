package com.turn.browser.service;

import com.turn.browser.bean.CountBalance;
import com.turn.browser.bean.StakingBO;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomInternalAddressMapper;
import com.turn.browser.dao.custommapper.CustomNodeMapper;
import com.turn.browser.dao.custommapper.CustomRpPlanMapper;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Service
public class CommonService {

    @Resource
    private CustomNodeMapper customNodeMapper;

    @Resource
    private BlockChainConfig blockChainConfig;

    @Resource
    private CustomInternalAddressMapper customInternalAddressMapper;

    @Resource
    private CustomRpPlanMapper customRpPlanMapper;

    @Resource
    private StatisticCacheService statisticCacheService;

    public String getNodeName(String nodeId, String nodeName) {
        /**
         * When nodeId is empty or nodeName is not empty, name is returned directly.
         */
        if (StringUtils.isNotBlank(nodeName) || StringUtils.isBlank(nodeId)) {
            return nodeName;
        }
        return customNodeMapper.findNameById(nodeId);
    }

    /**
     * Get the total circulation (AAA)
     * Total issuance = initial issuance * (1 + additional issuance ratio) ^ number of years
     *
     * @param
     * @return java.math.BigDecimal
     */
    public BigDecimal getIssueValue() {
        BigDecimal issueValue = BigDecimal.ZERO;
        try {
            NetworkStat networkStat = statisticCacheService.getNetworkStatCache();
            issueValue = networkStat.getIssueValue();
        } catch (Exception e) {
            log.error("Get the total circulation exception", e);
        }
        return issueValue;
    }

    /**
     * Get circulation
     * Circulation volume = total issuance volume of this additional issuance cycle - unexpired amount of locked positions - real-time entrustment reward pool contract balance - real-time incentive pool balance - real-time balance of all foundation accounts
     *
     * @param
     * @return void
     */
    public BigDecimal getCirculationValue() {
        NetworkStat networkStat = statisticCacheService.getNetworkStatCache();
        return CommonUtil.ofNullable(() -> networkStat.getTurnValue()).orElse(BigDecimal.ZERO);
    }

    /**
     * Query statistical balance
     *
     * @param
     * @return java.util.List<com.turn.browser.bean.CountBalance>
     */
    private List<CountBalance> countBalance() {
        List<CountBalance> list = customInternalAddressMapper.countBalance();
        return list;
    }

    /**
     * Get the total pledge and pledge rate denominator
     *
     * @param networkStatRedis:
     * @return: com.turn.browser.bean.StakingBO
     */
    public StakingBO getTotalStakingValueAndStakingDenominator(NetworkStat networkStatRedis) {
        StakingBO bo = new StakingBO();
        List<CountBalance> list = countBalance();
        // Get real-time pledge contract balance
        CountBalance stakingValue = list.stream().filter(v -> v.getType() == 2).findFirst().orElseGet(CountBalance::new);
        bo.setTotalStakingValue(stakingValue.getFree());
        log.debug("The real-time pledge contract balance (total pledge) is [{}]", stakingValue.getFree().toPlainString());
        BigDecimal issueValue = networkStatRedis.getIssueValue();
        // Get the real-time commission reward pool contract balance
        CountBalance delegationValue = list.stream().filter(v -> v.getType() == 6).findFirst().orElseGet(CountBalance::new);
        // Real-time incentive pool balance
        CountBalance incentivePoolValue = list.stream().filter(v -> v.getType() == 3).findFirst().orElseGet(CountBalance::new);
        // Get real-time balances of all foundation accounts
        CountBalance foundationValue = list.stream().filter(v -> v.getType() == 0).findFirst().orElseGet(CountBalance::new);
        BigDecimal stakingDenominator = issueValue.subtract(incentivePoolValue.getFree()).subtract(delegationValue.getFree()).subtract(foundationValue.getFree()).subtract(foundationValue.getLocked());
        log.debug("Pledge rate denominator [{}] = total issuance [{}] - real-time incentive pool balance [{}] - real-time delegation reward pool contract balance [{}] - real-time all foundation account balances [{}] - real-time all funds Account locked balance [{}];",
                  stakingDenominator.toPlainString(),
                  issueValue.toPlainString(),
                  incentivePoolValue.getFree().toPlainString(),
                  delegationValue.getFree().toPlainString(),
                  foundationValue.getFree().toPlainString(),
                  foundationValue.getLocked().toPlainString());
        if (stakingDenominator.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Error in obtaining pledge rate denominator [{}], cannot be less than or equal to 0", stakingDenominator.toPlainString());
        }
        bo.setStakingDenominator(stakingDenominator);
        return bo;
    }

}
