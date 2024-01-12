package com.turn.browser.task;

import cn.hutool.core.util.StrUtil;
import com.turn.browser.bean.CountBalance;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomInternalAddressMapper;
import com.turn.browser.dao.custommapper.CustomNOptBakMapper;
import com.turn.browser.dao.custommapper.CustomRpPlanMapper;
import com.turn.browser.dao.custommapper.StatisticBusinessMapper;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.service.account.AccountService;
import com.turn.browser.service.elasticsearch.EsErc1155TxRepository;
import com.turn.browser.service.elasticsearch.EsErc20TxRepository;
import com.turn.browser.service.elasticsearch.EsErc721TxRepository;
import com.turn.browser.service.elasticsearch.EsTransactionRepository;
import com.turn.browser.service.elasticsearch.bean.ESResult;
import com.turn.browser.service.elasticsearch.query.ESQueryBuilderConstructor;
import com.turn.browser.task.bean.NetworkStatistics;
import com.turn.browser.utils.AppStatusUtil;
import com.turn.browser.utils.CalculateUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * @Description: Network statistics related information update tasks
 */

@Component
@Slf4j
public class NetworkStatUpdateTask {

    @Resource
    private NetworkStatCache networkStatCache;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private AccountService accountService;

    @Resource
    private StatisticBusinessMapper statisticBusinessMapper;

    @Resource
    private CustomRpPlanMapper customRpPlanMapper;

    @Resource
    private CustomInternalAddressMapper customInternalAddressMapper;

    @Resource
    private EsTransactionRepository esTransactionRepository;

    @Resource
    private EsErc20TxRepository esErc20TxRepository;

    @Resource
    private EsErc721TxRepository esErc721TxRepository;

    @Resource
    private EsErc1155TxRepository esErc1155TxRepository;

    @Resource
    private CustomNOptBakMapper customNOptBakMapper;

    /**
     * Network statistics related information update tasks
     * Executed every 5 seconds
     *
     * @param :
     * @return: void
     */
    @XxlJob("networkStatUpdateJobHandler")
    public void networkStatUpdate() {
        // Only perform tasks when the program is running normally
        if (AppStatusUtil.isRunning()) {
            start();
        }
    }

    /**
     * Update transaction statistics
     * Update the cache, and then update the cache to MySQL and Redis
     * Executed every 1 minute
     *
     * @param :
     * @return: void
     */
    @XxlJob("updateNetworkQtyJobHandler")
    public void updateNetworkQty() {
        try {
            ESQueryBuilderConstructor count = new ESQueryBuilderConstructor();
            //Get the number of es transactions
            Long totalCount = 0L;
            try {
                ESResult<?> totalCountRes = esTransactionRepository.Count(count);
                totalCount = totalCountRes.getTotal();
            } catch (Exception e) {
                log.error("Exception in obtaining the number of es transactions", e);
            }
            //Get the number of erc20 transactions
            Long erc20Count = 0L;
            try {
                ESResult<?> erc20Res = esErc20TxRepository.Count(count);
                erc20Count = erc20Res.getTotal();
            } catch (Exception e) {
                log.error("Obtaining erc20 transaction number exception", e);
            }
            //Get the number of erc721 transactions
            Long erc721Count = 0L;
            try {
                ESResult<?> erc721Res = esErc721TxRepository.Count(count);
                erc721Count = erc721Res.getTotal();
            } catch (Exception e) {
                log.error("Obtaining erc721 transaction number exception", e);
            }
            //Get the number of erc1155 transactions
            Long erc1155Count = 0L;
            try {
                ESResult<?> erc1155Res = esErc1155TxRepository.Count(count);
                erc1155Count = erc1155Res.getTotal();
            } catch (Exception e) {
                log.error("Obtaining erc1155 transaction number exception", e);
            }
            //Get address statistics
            int addressQty = statisticBusinessMapper.getNetworkStatisticsFromAddress();
            //Get proposals in progress
            int doingProposalQty = statisticBusinessMapper.getNetworkStatisticsFromProposal();
            //Get the total number of proposals
            int proposalQty = statisticBusinessMapper.getProposalQty();
            //Get node operand
            long nodeOptSeq = customNOptBakMapper.getLastNodeOptSeq();
            NetworkStat networkStat = networkStatCache.getNetworkStat();
            networkStat.setTxQty(totalCount.intValue());
            networkStat.setErc20TxQty(erc20Count.intValue());
            networkStat.setErc721TxQty(erc721Count.intValue());
            networkStat.setErc1155TxQty(erc1155Count.intValue());
            networkStat.setAddressQty(addressQty);
            networkStat.setDoingProposalQty(doingProposalQty);
            networkStat.setProposalQty(proposalQty);
            networkStat.setNodeOptSeq(nodeOptSeq);
            XxlJobHelper.handleSuccess(StrUtil.format("The transaction statistics were updated successfully. The total number of transactions is [{}], the number of erc20 transactions is [{}], the number of erc721 transactions is [{}], the number of erc1155 transactions is [{}], and the number of addresses is [{}]. Continue The total number of proposals is [{}], the total number of proposals is [{}], and the number of node operations is [{}]",
                                                      totalCount.intValue(),
                                                      erc20Count.intValue(),
                                                      erc721Count.intValue(),
                                                      erc1155Count.intValue(),
                                                      addressQty,
                                                      doingProposalQty,
                                                      proposalQty,
                                                      nodeOptSeq));
        } catch (Exception e) {
            log.error("Abnormal update of transaction statistics", e);
            throw e;
        }
    }

    protected void start() {
        try {
            NetworkStat networkStat = networkStatCache.getNetworkStat();
            Long curNumber = networkStat.getCurNumber();
            //Get the incentive pool balance
            BigDecimal inciteBalance = accountService.getInciteBalance(BigInteger.valueOf(curNumber));
            //Calculate circulation
            BigDecimal turnValue = getCirculationValue(networkStat);
            //Calculate the usable pledge amount
            BigDecimal availableStaking = CalculateUtils.calculationAvailableValue(networkStat, inciteBalance);
            //Get node-related network statistics
            NetworkStatistics networkStatistics = statisticBusinessMapper.getNetworkStatisticsFromNode();
            //Total number of real-time pledge commissions
            BigDecimal totalValue = networkStatistics.getTotalValue() == null ? BigDecimal.ZERO : networkStatistics.getTotalValue();
            //Total number of real-time pledges
            BigDecimal stakingValue = networkStatistics.getStakingValue() == null ? BigDecimal.ZERO : networkStatistics.getStakingValue();
            networkStatCache.updateByTask(turnValue, availableStaking, totalValue, stakingValue);
            String msg = StrUtil.format("The network statistics task is successful, the circulation volume [{}], the available pledge amount [{}], the total number of real-time pledge commissions [{}], the total number of real-time pledges [{}]",
                                        turnValue.toPlainString(),
                                        availableStaking.toPlainString(),
                                        totalValue.toPlainString(),
                                        stakingValue.toPlainString());
            XxlJobHelper.log(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("An error occurred in the network statistics task:", e);
            throw e;
        }
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
     * Get circulation
     * Circulation volume = total issuance volume of this additional issuance cycle - unexpired amount of locked positions - real-time entrustment reward pool contract balance - real-time incentive pool balance - real-time balance of all foundation accounts
     *
     * @param networkStat:
     * @return: java.math.BigDecimal
     */
    private BigDecimal getCirculationValue(NetworkStat networkStat) {
        List<CountBalance> list = countBalance();
        // Lock the unexpired amount
        BigDecimal rpNotExpiredValue = customRpPlanMapper.getRPNotExpiredValue(chainConfig.getSettlePeriodBlockCount().longValue(), networkStat.getCurNumber());
        rpNotExpiredValue = Optional.ofNullable(rpNotExpiredValue).orElse(BigDecimal.ZERO);
        // Get the real-time commission reward pool contract balance
        CountBalance delegationValue = list.stream().filter(v -> v.getType() == 6).findFirst().orElseGet(CountBalance::new);
        // Real-time incentive pool balance
        CountBalance incentivePoolValue = list.stream().filter(v -> v.getType() == 3).findFirst().orElseGet(CountBalance::new);
        // Get real-time balances of all foundation accounts
        CountBalance foundationValue = list.stream().filter(v -> v.getType() == 0).findFirst().orElseGet(CountBalance::new);
        // When the agent is first started, if the first scheduled task is faster than the main process, the total circulation may not be calculated yet, and the circulation may be negative.
        BigDecimal circulationValue = networkStat.getIssueValue()
                                                 .subtract(rpNotExpiredValue)
                                                 .subtract(delegationValue.getFree())
                                                 .subtract(incentivePoolValue.getFree())
                                                 .subtract(foundationValue.getFree());
        log.info("Circulation volume [{}] = total issuance volume in this additional issuance cycle [{}] - unexpired amount locked up [{}] - real-time entrustment reward pool contract balance [{}] - real-time incentive pool balance [{}]- Real-time balance of all foundation accounts [{}]; current block height [{}] total number of blocks in the settlement cycle [{}]",
                 circulationValue.toPlainString(),
                 networkStat.getIssueValue().toPlainString(),
                 rpNotExpiredValue.toPlainString(),
                 delegationValue.getFree().toPlainString(),
                 incentivePoolValue.getFree().toPlainString(),
                 foundationValue.getFree().toPlainString(),
                 networkStat.getCurNumber(),
                 chainConfig.getSettlePeriodBlockCount().longValue());
        return circulationValue;
    }

}
