package com.turn.browser.utils;

import com.turn.browser.config.BlockChainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundCalculation {

    private RoundCalculation(){}

    /**
     * The number of voting ending rounds is converted into block height
     * Ending block height = Block height of proposal transaction + Consensus cycle block number - Block height of proposal transaction % Consensus cycle block number + Proposal parameter input round number * Consensus cycle block number - 20
     */


    private static Logger logger = LoggerFactory.getLogger(RoundCalculation.class);

    /**
     * Get parameter proposal voting end block number
     * @param proposalTxBlockNumber Block number of proposal transaction
     * @param chainConfig chain configuration
     * @return
     */
    public static BigDecimal getParameterProposalVoteEndBlockNum (long proposalTxBlockNumber, BlockChainConfig chainConfig ) {
        try {
            //Exchange at block height
            BigDecimal txBlockNumber = BigDecimal.valueOf(proposalTxBlockNumber);
            //The number of blocks in the settlement cycle
            BigDecimal settlePeriodBlockCount = new BigDecimal(chainConfig.getSettlePeriodBlockCount());
            //[Governance] The longest time for voting on parameter proposals to last (unit: s)
            BigDecimal paramProposalVoteDurationSeconds= new BigDecimal(chainConfig.getParamProposalVoteDurationSeconds());
            //Block interval
            BigDecimal blockInterval = BigDecimal.ONE;
            // Calculate the voting deadline block number:
            // (CEILING(block number where the parameter proposal is located/number of blocks in the settlement cycle)+FLOOR(the longest voting time for the parameter proposal/(block interval*number of blocks in the settlement cycle)))*number of blocks in the settlement cycle
            return txBlockNumber
                    .divide(settlePeriodBlockCount,0, RoundingMode.CEILING)
                    .add(paramProposalVoteDurationSeconds.divide(blockInterval.multiply(settlePeriodBlockCount),0,RoundingMode.FLOOR))
                    .multiply(settlePeriodBlockCount);
        } catch (Exception e) {
            logger.error("[RoundCalculation] exception");
            return BigDecimal.ZERO;
        }
    }

    public static BigDecimal endBlockNumCal ( String blockNumber, BigDecimal consensusRound, BlockChainConfig chainConfig ) {
        try {
            //Exchange at block height
            BigDecimal txBlockNumber = new BigDecimal(blockNumber);
            //The number of consensus cycle blocks
            BigDecimal consensusCount = new BigDecimal(chainConfig.getConsensusPeriodBlockCount());
            //The block height of the proposed exchange is % of the consensus cycle block number, and which consensus round the exchange is in
            BigDecimal[] belongToConList = txBlockNumber.divideAndRemainder(consensusCount);
            BigDecimal belongToCon = belongToConList[1];
            //Conversion ends quickly
            return txBlockNumber.add(consensusCount).subtract(belongToCon).add(consensusRound.multiply(consensusCount)).subtract(new BigDecimal(20));
        } catch (Exception e) {
            logger.error("[RoundCalculation] exception");
            return BigDecimal.ZERO;
        }
    }

    /**
     * Effective round number conversion block height
     * Effective block height = voting end block + consensus cycle number of blocks - voting end block% consensus cycle block number + 1
     */
    public static BigDecimal activeBlockNumCal ( BigDecimal voteNum, BlockChainConfig chainConfig ) {
        try {
            //The number of consensus cycle blocks
            BigDecimal consensusCount = new BigDecimal(chainConfig.getConsensusPeriodBlockCount());
            //The block height of the proposed exchange is % of the consensus cycle block number, and which consensus round the exchange is in
            BigDecimal[] belongToConList = voteNum.divideAndRemainder(consensusCount);
            BigDecimal belongToCon = belongToConList[1];
            //Conversion effective block height
            return voteNum.add(consensusCount).subtract(belongToCon).add(BigDecimal.ONE);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
