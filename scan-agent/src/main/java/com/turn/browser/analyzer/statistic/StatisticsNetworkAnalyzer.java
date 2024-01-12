package com.turn.browser.analyzer.statistic;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.turn.browser.bean.CollectionEvent;
import com.turn.browser.bean.EpochInfo;
import com.turn.browser.bean.EpochMessage;
import com.turn.browser.cache.NetworkStatCache;
import com.turn.browser.cache.NodeCache;
import com.turn.browser.client.TurnClient;
import com.turn.browser.client.SpecialApi;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.StatisticBusinessMapper;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.utils.CalculateUtils;
import com.turn.browser.utils.EpochUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
public class StatisticsNetworkAnalyzer {

    @Resource
    private NetworkStatCache networkStatCache;

    @Resource
    private NodeCache nodeCache;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private StatisticBusinessMapper statisticBusinessMapper;

    @Resource
    private SpecialApi specialApi;

    @Resource
    private TurnClient turnClient;

    /**
     * years
     */
    private volatile int yearNum = 0;

    /**
     * total circulation
     */
    private volatile BigDecimal totalIssueValue = BigDecimal.ZERO;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void analyze(CollectionEvent event, Block block, EpochMessage epochMessage) throws Exception {
        long startTime = System.currentTimeMillis();
        log.debug("Block storage statistics: blocks [{}], number of transactions [{}], number of consensus cycles [{}], number of settlement cycles [{}], number of additional issuance cycles [{}]",
                  block.getNum(),
                  event.getTransactions().size(),
                  epochMessage.getConsensusEpochRound(),
                  epochMessage.getSettleEpochRound(),
                  epochMessage.getIssueEpochRound());
        // Network statistics
        NetworkStat networkStat = networkStatCache.getNetworkStat();
        networkStat.setNodeId(block.getNodeId());
        networkStat.setNodeName(nodeCache.getNode(block.getNodeId()).getNodeName());
        networkStat.setNextSettle(CalculateUtils.calculateNextSetting(chainConfig.getSettlePeriodBlockCount(), epochMessage.getSettleEpochRound(), epochMessage.getCurrentBlockNumber()));
        setTotalIssueValue(block.getNum(), event.getEpochMessage().getSettleEpochRound(), networkStat);
        statisticBusinessMapper.networkChange(networkStat);
        log.debug("Processing time:{} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * Set total circulation
     *
     * @param curBlockNum:      Current block height
     * @param settleEpochRound: Settlement cycle rounds
     * @param networkStat:      Network statistics object instance
     * @return: void
     */
    private void setTotalIssueValue(Long curBlockNum, BigInteger settleEpochRound, NetworkStat networkStat) throws Exception {
        try {
            // The block of the settlement cycle obtains the total issuance amount
            if ((curBlockNum - 1) % chainConfig.getSettlePeriodBlockCount().longValue() == 0) {
                log.info("The current block height [{}] obtains the total issuance in the settlement period [{}]", curBlockNum, settleEpochRound);
                yearNum = getYearNum(curBlockNum);
                totalIssueValue = getTotalIssueValue(yearNum);
                networkStat.setYearNum(yearNum);
                networkStat.setIssueValue(totalIssueValue);
            } else {
                // For blocks in non-settlement cycles, the value in local memory is taken. If the value verification is incorrect, it will be obtained again.
                if (yearNum < 1 || ObjectUtil.isNull(totalIssueValue) || totalIssueValue.compareTo(BigDecimal.ZERO) <= 0) {
                    // When the agent is restarted and the settlement cycle has not been caught up, if the local memory fails, it will be reacquired.
                    log.info("If the year in local memory is less than 1 or the total issuance is empty or the total issuance is less than or equal to 0, the year and total issuance will be re-obtained.");
                    yearNum = getYearNum(curBlockNum);
                    totalIssueValue = getTotalIssueValue(yearNum);
                }
                networkStat.setYearNum(yearNum);
                networkStat.setIssueValue(totalIssueValue);
                log.info("The current block height [{}] successfully obtained the year [{}] and total issuance amount [{}] of local memory in the [{}]th settlement period.", curBlockNum, settleEpochRound, yearNum, totalIssueValue.toPlainString());
            }
        } catch (Exception e) {
            log.error(StrUtil.format("The current block height [{}] is abnormal in obtaining the total issuance volume in the [{}]th settlement cycle, and will try again.", curBlockNum, settleEpochRound), e);
            // If the current block obtains the total issuance abnormally, reset the value of the local memory
            yearNum = 0;
            totalIssueValue = BigDecimal.ZERO;
            throw e;
        }
    }

    /**
     * Current block height
     *
     * @param currentNumber: Current block height
     * @return: int
     */
    private int getYearNum(Long currentNumber) throws Exception {
        // The last block number of the previous settlement cycle
        BigInteger preSettleEpochLastBlockNumber = EpochUtil.getPreEpochLastBlockNumber(Convert.toBigInteger(currentNumber), chainConfig.getSettlePeriodBlockCount());
        // Get from special interface
        EpochInfo epochInfo = specialApi.getEpochInfo(turnClient.getWeb3jWrapper().getWeb3j(), preSettleEpochLastBlockNumber);

        int yearNum = epochInfo.getYearNum().intValue();
        if (yearNum < 1) {
            throw new Exception(StrUtil.format("The current block [{}], the last block number of the previous settlement cycle [{}] and the year obtained [{}] are abnormal.", currentNumber, preSettleEpochLastBlockNumber, yearNum));
        }
        return yearNum;
    }

    /**
     * Get total circulation
     *
     * @param yearNum: What year
     * @return: java.math.BigDecimal
     */
    private BigDecimal getTotalIssueValue(int yearNum) throws Exception {
        // Get the initial issuance amount
        BigDecimal initIssueAmount = chainConfig.getInitIssueAmount();
        initIssueAmount = com.bubble.utils.Convert.toVon(initIssueAmount, com.bubble.utils.Convert.Unit.KPVON);
        // Fixed annual issuance ratio
        BigDecimal addIssueRate = chainConfig.getAddIssueRate();
        BigDecimal issueValue = initIssueAmount.multiply(addIssueRate.add(new BigDecimal(1L)).pow(yearNum)).setScale(4, BigDecimal.ROUND_HALF_UP);
        log.info("Total issuance volume [{}] = initial issuance volume [{}]*(1+ additional issuance ratio [{}])^year [{}]", issueValue.toPlainString(), initIssueAmount.toPlainString(), addIssueRate.toPlainString(), yearNum);
        if (issueValue.compareTo(BigDecimal.ZERO) <= 0 || issueValue.compareTo(initIssueAmount) <= 0) {
            throw new Exception(StrUtil.format("Error in obtaining total circulation [{}], which cannot be less than or equal to 0 or less than or equal to the initial circulation", issueValue.toPlainString()));
        }
        return issueValue;
    }

}
