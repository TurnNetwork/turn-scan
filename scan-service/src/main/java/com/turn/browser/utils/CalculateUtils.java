package com.turn.browser.utils;

import com.turn.browser.bean.PeriodValueElement;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.constant.Browser;
import com.turn.browser.dao.entity.NetworkStat;
import com.bubble.utils.Convert;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
public class CalculateUtils {

    private CalculateUtils() {
    }

    /**
     *Higher than the remaining blocks in the next settlement cycle
     *
     * @param settlePeriodBlockCount The total number of blocks in each settlement period
     * @param curSettingEpoch The current settlement cycle number
     * @param curBlockNumber current block height
     * @return
     */
    public static Long calculateNextSetting(BigInteger settlePeriodBlockCount, BigInteger curSettingEpoch, BigInteger curBlockNumber) {
        return settlePeriodBlockCount.multiply(curSettingEpoch).subtract(curBlockNumber).longValue();
    }

    /**
     * Calculate the usable pledge amount
     *
     * @param networkStat:
     * @param incentivePoolAccountBalance:
     * @return: java.math.BigDecimal
     */
    public static BigDecimal calculationAvailableValue(NetworkStat networkStat, BigDecimal incentivePoolAccountBalance) {
        return networkStat.getIssueValue().subtract(incentivePoolAccountBalance);
    }

    /**
     * Rotation profit
     *
     * @param profits profit list
     * @param curSettleProfit business [pledge/entrustment] profit of the current settlement cycle
     * @param curSettleEpoch The current settlement cycle number
     * @param chainConfig chain configuration
     */
    public static void rotateProfit(List<PeriodValueElement> profits, BigDecimal curSettleProfit, BigInteger curSettleEpoch, BlockChainConfig chainConfig) {
        //Add the income of the previous period
        if (curSettleEpoch.longValue() == 0) curSettleProfit = BigDecimal.ZERO; // If the current settlement period is 0, the profit is 0
        PeriodValueElement pve = new PeriodValueElement();
        pve.setPeriod(curSettleEpoch.longValue());
        pve.setValue(curSettleProfit);
        profits.add(pve);
        // +1: retain the previous period's return of the oldest period in the specified number of periods and use it as a reference point for return calculation
        if (profits.size() > chainConfig.getMaxSettlePeriodCount4AnnualizedRateStat().longValue() + 1) {
            // Sort by settlement period from large to small
            profits.sort((c1, c2) -> Integer.compare(0, c1.getPeriod().compareTo(c2.getPeriod())));
            // Delete redundant elements, +1: Retain the previous period's income of the oldest period in the specified number of periods and use it as a reference point for income calculation
            //Delete from back to front to prevent errors
            for (int i = profits.size() - 1; i >= chainConfig.getMaxSettlePeriodCount4AnnualizedRateStat().longValue() + 1; i--) profits.remove(i);
        }
    }

    /**
     * Rotation cost
     *
     * @param costs cost list
     * @param curSettleCost Business [pledge/entrustment] cost of the current settlement cycle
     * @param curSettleEpoch The current settlement cycle number
     * @param chainConfig chain configuration
     */
    public static void rotateCost(List<PeriodValueElement> costs, BigDecimal curSettleCost, BigInteger curSettleEpoch, BlockChainConfig chainConfig) {
        //Add the staking cost for the next period
        if (curSettleEpoch.longValue() == 0) curSettleCost = BigDecimal.ZERO; // If the current settlement period is 0, the cost is 0
        PeriodValueElement pve = new PeriodValueElement();
        pve.setPeriod(curSettleEpoch.longValue());
        pve.setValue(curSettleCost);
        costs.add(pve);
        // Keep the specified number of latest records
        if (costs.size() > chainConfig.getMaxSettlePeriodCount4AnnualizedRateStat().longValue() + 1) {
            // Sort by settlement period from large to small
            costs.sort((c1, c2) -> Integer.compare(0, c1.getPeriod().compareTo(c2.getPeriod())));
            //Remove redundant elements
            for (int i = costs.size() - 1; i >= chainConfig.getMaxSettlePeriodCount4AnnualizedRateStat().longValue() + 1; i--) costs.remove(i);
        }
    }

    /**
     * Calculate annualized rate
     *
     * @param profits profit list
     * @param costs cost list
     * @param chainConfig chain configuration
     * @return
     */
    public static BigDecimal calculateAnnualizedRate(List<PeriodValueElement> profits, List<PeriodValueElement> costs, BlockChainConfig chainConfig) {
        //Profit accumulation is arranged from large to small by period
        profits.sort((c1, c2) -> Integer.compare(0, c1.getPeriod().compareTo(c2.getPeriod())));
        // Costs are arranged from large to small by period
        costs.sort((c1, c2) -> Integer.compare(0, c1.getPeriod().compareTo(c2.getPeriod())));
        // Latest profit accumulation
        PeriodValueElement first = profits.get(profits.size() - 1);
        // Profit = Maximum accumulated income - Minimum accumulated income
        PeriodValueElement max = first;
        for (PeriodValueElement p : profits) {
            if (p.getValue().compareTo(max.getValue()) > 0) max = p;
        }
        BigDecimal profitSum = max.getValue().subtract(first.getValue()).abs();

        // Latest profit accumulation
        PeriodValueElement last = profits.get(0);
        BigDecimal costSum = BigDecimal.ZERO;
        for (PeriodValueElement cost : costs) {
            // Skip the cost period that is greater than the maximum settlement period where the profit is located
            if (cost.getPeriod() > last.getPeriod()) continue;
            costSum = costSum.add(cost.getValue());
        }

        if (costSum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Annualized rate = (profit/cost) x number of settlement cycles in each additional issuance cycle x 100
        BigDecimal rate = profitSum.divide(costSum, 16, RoundingMode.FLOOR) // 除总成本
                                   .multiply(new BigDecimal(chainConfig.getSettlePeriodCountPerIssue())) // Multiply the number of settlement cycles per additional issuance cycle
                                   .multiply(BigDecimal.valueOf(100));
        return rate.setScale(2, RoundingMode.FLOOR);
    }

}
