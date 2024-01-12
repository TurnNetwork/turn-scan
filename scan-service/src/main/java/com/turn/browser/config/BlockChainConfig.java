package com.turn.browser.config;

import com.turn.browser.bean.CustomStaking;
import com.turn.browser.client.TurnClient;
import com.turn.browser.dao.mapper.ConfigMapper;
import com.turn.browser.enums.InnerContractAddrEnum;
import com.turn.browser.exception.ConfigLoadingException;
import com.bubble.protocol.core.methods.response.bean.EconomicConfig;
import com.bubble.utils.Convert;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * Chain parameter unified configuration items
 * @Description:
 */

@Slf4j
@DependsOn("networkParams")
@Data
@Configuration
@ConfigurationProperties(prefix = "turn")
public class BlockChainConfig {

    private static Set<String> INNER_CONTRACT_ADDR;

    @Resource
    private ConfigMapper configMapper;

    @Resource
    private TurnClient client;

    static {
        File saltFile = FileUtils.getFile(System.getProperty("user.dir"), "jasypt.properties");
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(saltFile)) {
            properties.load(in);
            String salt = properties.getProperty("jasypt.encryptor.password");
            if (StringUtils.isBlank(salt)) {
                throw new ConfigLoadingException("Crypto salt cannot be empty!");
            }
            salt = salt.trim();
            System.setProperty("JASYPT_ENCRYPTOR_PASSWORD", salt);
            log.info("salt:{}", salt);
        } catch (IOException | ConfigLoadingException e) {
            log.error("Error loading decrypted file", e);
            System.exit(1);
        }
    }

    public Set<String> getInnerContractAddr() {
        return Collections.unmodifiableSet(INNER_CONTRACT_ADDR);
    }

    /*******************The following parameters are obtained through the rpc interface debug_economicConfig*******************/
    @Value("${turn.chainId}")
    private long chainId;

    //[General] Default maximum Gas for each block
    @Value("${turn.maxBlockGasLimit}")
    private BigDecimal maxBlockGasLimit;

    /**
     * [General] Target value of the number of blocks produced by each validator per consensus cycle
     */
    private BigInteger expectBlockCount;

    /**
     * [General] Number of verification nodes in each consensus round
     */
    private BigInteger consensusValidatorCount;

    /**
     * [General] Number of verification nodes per settlement cycle
     */
    private BigInteger settlementValidatorCount;

    /**
     * [General] The number of minutes in each additional issuance cycle
     */
    private BigInteger additionalCycleMinutes;

    /**
     *【General】Number of minutes per billing cycle
     */
    private BigInteger settlementCycleMinutes;

    /**
     * [General] The number of settlement cycles in each additional issuance cycle
     */
    private BigInteger settlePeriodCountPerIssue;

    /**
     * [General] Block production interval = System-allocated node block production time window/target value of the number of blocks produced by each validator per view
     */
    private BigInteger blockInterval;

    /**
     * [General] Consensus round block number = expectBlockCount x consensusValidatorCount
     */
    private BigInteger consensusPeriodBlockCount;

    /**
     * [General] Total number of blocks in each settlement cycle = ROUND_DOWN (number of minutes specified in the settlement cycle x 60 / (block interval x number of consensus round blocks)) x number of consensus round blocks
     */
    private BigInteger settlePeriodBlockCount;

    /**
     * [General] Total number of blocks in each additional issuance cycle = ROUND_DOWN (time of additional issuance cycle x 60/(block interval x number of blocks in settlement cycle)) x number of blocks in settlement cycle
     */
    private BigInteger addIssuePeriodBlockCount;

    /**
     * [General] Turn Foundation account address
     */
    private String turnFundAccount;

    /**
     * [General] Turn Foundation Account Initial Balance
     */
    private BigDecimal turnFundInitAmount;

    /**
     * [General] Developer Incentive Fund Account Address
     */
    private String communityFundAccount;

    /**
     * [General] Initial Balance of Developer Incentive Fund Account
     */
    private BigDecimal communityFundInitAmount;

    /**
     * [Staking] Staking threshold: the minimum number of pledged Tokens (VON) required to create a validator
     */
    private BigDecimal stakeThreshold;

    /**
     * [Pledge] Commission Threshold (VON)
     */
    private BigDecimal delegateThreshold;

    /**
     * [Pledge] The number of settlement cycles for node pledge return lock
     */
    private BigInteger unStakeRefundSettlePeriodCount;

    /**
     * [Pledge] Number of unlocking periods for delegating
     */
    private BigInteger unDelegateFreezeDurationCount;

    /**
     * [Penalty] Double signing reward percentage
     */
    private BigDecimal duplicateSignRewardRate;

    /**
     * [Punishment] Double signing penalty percentage
     */
    private BigDecimal duplicateSignSlashRate;

    /**
     * [Punishment] The number of periods for which the reporting evidence is effective
     */
    private BigDecimal evidenceValidEpoch;

    /**
     * [Punishment] Deduct the number of block rewards
     */
    private BigDecimal slashBlockRewardCount;

    /**
     * [Governance] Text proposal participation rate: >
     */
    private BigDecimal minProposalTextParticipationRate;

    /**
     * [Governance] Text proposal support rate: >=
     */
    private BigDecimal minProposalTextSupportRate;

    /**
     * [Governance] Cancellation proposal participation rate: >
     */
    private BigDecimal minProposalCancelParticipationRate;

    /**
     * [Governance] Cancellation proposal support rate: >=
     */
    private BigDecimal minProposalCancelSupportRate;

    /**
     * [Governance] Upgrade proposal pass rate
     */
    private BigDecimal minProposalUpgradePassRate;

    /**
     * [Governance] Default end rounds of text proposals
     */
    private BigDecimal proposalTextConsensusRounds;

    /**
     * [Governance] Set the number of pre-upgrade starting rounds
     */
    private BigDecimal versionProposalActiveConsensusRounds;

    /**
     * [Governance] The longest time for voting on parameter proposals to last (unit: s)
     */
    private BigInteger paramProposalVoteDurationSeconds;

    /**
     * [Governance] Parameter proposal voting participation rate threshold (one of the conditions for parameter proposal voting to pass: greater than this value, then parameter proposal voting passes)
     */
    private BigDecimal paramProposalVoteRate;

    /**
     * [Governance] Parameter proposal voting support rate threshold (one of the conditions for passing parameter proposal voting: greater than or equal to this value, then parameter proposal voting passes
     */
    private BigDecimal paramProposalSupportRate;

    /**
     * [Reward] The incentive pool is allocated to the proportion of block incentives
     */
    private BigDecimal blockRewardRate;

    /**
     * [Reward] The proportion of the incentive pool allocated to staking incentives
     */
    private BigDecimal stakeRewardRate;

    /**
     * [Reward] Turn foundation years
     */
    private BigInteger turnFoundationYear;

    /**
     * [General] The current number of additional issuance cycles
     */
    private BigDecimal issueEpochRound;

    /**
     * [General] The starting block number of the current issuance cycle
     */
    private BigDecimal issueEpochStartBlockNumber;

    /**
     * [General] The end block number of the current issuance cycle
     */
    private BigDecimal issueEpochEndBlockNumber;

    /**
     * [Lockup] Minimum release amount (TURN)
     */
    private BigDecimal restrictingMinimumRelease;

    /************************The following parameters are obtained from the application configuration file**********************/

    /**
     * How many consecutive periods can the statistical annualized rate of pledge nodes take at most?
     */
    private BigInteger maxSettlePeriodCount4AnnualizedRateStat;

    /**
     * Turn initial total issuance (ATP)
     */
    private BigDecimal initIssueAmount;

    /**
     * Fixed issuance ratio every year
     */
    private BigDecimal addIssueRate;

    /**
     * The proportion of additional issuance allocated to the incentive pool each year
     */
    private BigDecimal incentiveRateFromIssue;

    /**
     * How many blocks back in each consensus round is the time to elect the next round of validators
     */
    private BigInteger electionBackwardBlockCount;

    /**
     * Within 10 years, the foundation will fill the amount of the incentive pool (ATP)
     */
    private Map<Integer, BigDecimal> foundationSubsidies;

    /**
     * Proposal url parameter template
     */
    private String proposalUrlTemplate;

    /**
     * Proposal pip_num parameter template
     */
    private String proposalPipNumTemplate;

    /**
     *keyBase
     */
    private String keyBase;

    /**
     *keyBaseApi
     */
    private String keyBaseApi;

    /**
     * Initial built-in node default pledge amount (VON)
     */
    private BigDecimal defaultStakingLockedAmount;

    /**
     * [Violation-Low Block Production Rate] The number of consecutive zero block productions within the maximum tolerance period
     * Description: Represents the threshold of zero block generation. When a node continues to achieve zero block generation for a period of time and the number of times is greater than or equal to this threshold, it will be punished.
     */
    private Integer zeroProduceNumberThreshold;

    /**
     * [Violation-low block rate] Maintain the maximum tolerated number of consensus rounds with zero block generation
     * Note: Use N to represent the value set in the following fields, as explained below:
     * After the last zero block, if there is another zero block in the next N consensus cycles, zero block information will be recorded when these N consensus cycles are completed.
     */
    private Integer zeroProduceCumulativeTime;

    /**
     * [Violation-low block production rate] Zero block production penalty locking period number
     */
    private Integer zeroProduceFreezeDuration;

    /**
     * The modification range of node delegation reward ratio/the adjustment range of node delegation ratio needs to be divided by 100%
     */
    private Integer rewardPerMaxChangeRange;

    /**
     * Modification interval of node delegation reward ratio/interval period of adjustment of delegation ratio
     */
    private Integer rewardPerChangeInterval;

    /**
     * Initial built-in node information
     */
    private List<CustomStaking> defaultStakingList = new ArrayList<>();

    /**
     * Token definition event
     */
    private Map<String, String> eventDefine;

    private String addressPrefix;

    @PostConstruct
    public void init() throws ConfigLoadingException {

        BlockChainConfig.INNER_CONTRACT_ADDR = new HashSet<>(InnerContractAddrEnum.getAddresses());
        defaultStakingLockedAmount = Convert.toVon(defaultStakingLockedAmount, Convert.Unit.KPVON);
        // Update the configuration using the data returned by the economic model parameter interface
        updateWithEconomicConfig(client.getEconomicConfig());
        // Refresh contract
        client.updateContract();
    }

    private void updateWithEconomicConfig(EconomicConfig dec) {
        //[General] Target value of the number of blocks produced by each validator per consensus cycle
        setExpectBlockCount(dec.getCommon().getPerRoundBlocks());
        //[General] Number of verification nodes in each consensus round
        setConsensusValidatorCount(dec.getCommon().getMaxConsensusVals());
        //[General] Number of verification nodes per settlement cycle
        setSettlementValidatorCount(dec.getStaking().getMaxValidators());
        //[General] The number of minutes specified in the additional issuance cycle
        setAdditionalCycleMinutes(dec.getCommon().getAdditionalCycleTime());
        //[General] Minutes of each settlement cycle
        setSettlementCycleMinutes(dec.getCommon().getMaxEpochMinutes());
        //[General] Block production interval = System-allocated node block production time window/target value of the number of blocks produced by each validator per view
        setBlockInterval(dec.getCommon().getNodeBlockTimeWindow().divide(expectBlockCount));
        //[General] Consensus round block number = expectBlockCount x consensusValidatorCount
        setConsensusPeriodBlockCount(expectBlockCount.multiply(dec.getCommon().getMaxConsensusVals()));
        //[General] Total number of blocks in each settlement cycle = ROUND_DOWN (number of minutes specified in the settlement cycle x 60/(block interval x number of consensus round blocks)) x number of consensus round blocks
        setSettlePeriodBlockCount(settlementCycleMinutes.multiply(BigInteger.valueOf(60))
                .divide(blockInterval.multiply(consensusPeriodBlockCount))
                .multiply(consensusPeriodBlockCount));
        //[General] Turn Foundation account address
        setTurnFundAccount(dec.getInnerAcc().getBubbleFundAccount());
        //[General] Turn foundation account initial balance
        setTurnFundInitAmount(new BigDecimal(dec.getInnerAcc().getBubbleFundBalance()));
        //[General] Community Developer Incentive Fund Account Address
        setCommunityFundAccount(dec.getInnerAcc().getCdfAccount());
        //[General] Initial balance of community developer incentive fund account
        setCommunityFundInitAmount(new BigDecimal(dec.getInnerAcc().getCdfBalance()));

        //[Pledge] The minimum number of pledged Tokens (VON) required to create a validator
        setStakeThreshold(new BigDecimal(dec.getStaking().getStakeThreshold()));
        //[Pledge] The minimum number of Tokens (VON) required by the client for each commission and redemption
        setDelegateThreshold(new BigDecimal(dec.getStaking().getOperatingThreshold()));
        //[Pledge] The number of settlement cycles for node pledge return locking
        setUnStakeRefundSettlePeriodCount(dec.getStaking().getUnStakeFreezeDuration());
        setUnDelegateFreezeDurationCount(dec.getStaking().getUnDelegateFreezeDuration());
        //[Punishment] Double signing reward percentage
        setDuplicateSignRewardRate(dec.getSlashing()
                .getDuplicateSignReportReward()
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR));
        //[Punishment] Penalty for double signing is 10,000 points
        setDuplicateSignSlashRate(new BigDecimal(dec.getSlashing()
                                                    .getSlashFractionDuplicateSign()).divide(BigDecimal.valueOf(10000),
                                                                                             16,
                                                                                             RoundingMode.FLOOR));
        //[Punishment] Number of validity periods of reporting evidence
        setEvidenceValidEpoch(new BigDecimal(dec.getSlashing().getMaxEvidenceAge()));
        //[Punishment] Deduct the number of block rewards
        setSlashBlockRewardCount(new BigDecimal(dec.getSlashing().getSlashBlocksReward()));

        //[Governance] Text proposal participation rate: >
        setMinProposalTextParticipationRate(dec.getGov()
                .getTextProposalVoteRate()
                .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));
        //[Governance] Text proposal support rate: >=
        setMinProposalTextSupportRate(dec.getGov()
                .getTextProposalSupportRate()
                .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));
        //[Governance] Cancellation proposal participation rate: >
        setMinProposalCancelParticipationRate(dec.getGov()
                .getCancelProposalVoteRate()
                .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));
        //[Governance] Cancel proposal support rate: >=
        setMinProposalCancelSupportRate(dec.getGov()
                                           .getCancelProposalSupportRate()
                                           .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));
        //[Governance] Upgrade proposal pass rate
        setMinProposalUpgradePassRate(dec.getGov()
                .getVersionProposalSupportRate()
                .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));
        //[Governance] Text proposal voting cycle
        setProposalTextConsensusRounds(new BigDecimal(dec.getGov()
                                                         .getTextProposalVoteDurationSeconds()) // The longest time the text proposal voting lasts (unit: s)
                .divide(new BigDecimal(
                                BigInteger.ONE // Block production interval = System-allocated node block production time window/target value of the number of blocks produced by each validator per view
                                        .multiply(
                                                dec.getCommon()
                                                        .getPerRoundBlocks())
                                        .multiply(
                                                consensusValidatorCount))
                        //The number of verification nodes in each consensus round
                                                                                                        ,
                                                                                                        0,
                                                                                                        RoundingMode.FLOOR));

        //[Governance] The longest time for voting on parameter proposals to last (unit: s)
        setParamProposalVoteDurationSeconds(dec.getGov().getParamProposalVoteDurationSeconds());
        //[Governance] Parameter proposal voting participation rate threshold (one of the conditions for parameter proposal voting to pass: greater than this value, then parameter proposal voting passes)
        setParamProposalVoteRate(dec.getGov()
                .getParamProposalVoteRate()
                .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));
        //[Governance] Parameter proposal voting support rate threshold (one of the conditions for parameter proposal voting to pass: greater than or equal to this value, then parameter proposal voting passes
        setParamProposalSupportRate(dec.getGov()
                                       .getParamProposalSupportRate()
                                       .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));

        //[Reward] The incentive pool allocation gives the proportion of block incentives
        setBlockRewardRate(new BigDecimal(dec.getReward().getNewBlockRate()).divide(BigDecimal.valueOf(100),
                2,
                RoundingMode.FLOOR));
        //[Reward] The ratio of the incentive pool allocated to staking incentives = 1-block reward ratio
        setStakeRewardRate(BigDecimal.ONE.subtract(blockRewardRate));
        //[Reward] Turn foundation years
        setTurnFoundationYear(dec.getReward().getBubbleFoundationYear());
        //[Punishment] Zero block generation times
        setZeroProduceCumulativeTime(dec.getSlashing().getZeroProduceCumulativeTime().intValue());
        //[Penalty] Zero block threshold
        setZeroProduceNumberThreshold(dec.getSlashing().getZeroProduceNumberThreshold().intValue());
        // Node zero block penalty is locked time
        setZeroProduceFreezeDuration(dec.getSlashing().getZeroProduceFreezeDuration().intValue());
        //[Pledge] Commission ratio adjustment range limit
        setRewardPerMaxChangeRange(dec.getStaking().getRewardPerMaxChangeRange().intValue());
        //[Pledge] Commission ratio adjustment interval
        setRewardPerChangeInterval(dec.getStaking().getRewardPerChangeInterval().intValue());
        //[Lockup] Minimum lockup release amount, (debug_economic interface turn version will return minimumRelease, alaya version will not return minimumRelease
        // This value needs to be set in ParameterService.initConfigTable() in the alaya version browser
        //setRestrictingMinimumRelease(new BigDecimal(dec.getRestricting().getMinimumRelease()));
    }

}