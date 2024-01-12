package com.turn.browser.service.govern;

import com.turn.browser.bean.govern.ModifiableParam;
import com.turn.browser.client.TurnClient;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.custommapper.CustomConfigMapper;
import com.turn.browser.dao.entity.Config;
import com.turn.browser.dao.mapper.ConfigMapper;
import com.turn.browser.enums.ModifiableGovernParamEnum;
import com.turn.browser.utils.ChainVersionUtil;
import com.bubble.contracts.dpos.dto.resp.GovernParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @description: Governance parameter service
 **/
@Slf4j
@Service
@Transactional(rollbackFor = {Exception.class, Error.class})
public class ParameterService {

    @Resource
    private ConfigMapper configMapper;

    @Resource
    private TurnClient turnClient;

    @Resource
    private BlockChainConfig chainConfig;

    @Resource
    private CustomConfigMapper customConfigMapper;

    /**
     * Initialize the configuration table using the data returned by the debug_economic_config interface, which only needs to be called when synchronizing from the first block
     */
    public void initConfigTable() throws Exception {
        log.info("Governance parameter initialization...");
        configMapper.deleteByExample(null);
        // The proposal contract is only called to obtain the complete list of governable parameters. It is not certain whether the value at this time is the value at block 0 of the chain, so it is necessary
        // Use the debugEconomicConfig interface to get the initial value to replace it
        List<GovernParam> governParamList = turnClient.getProposalContract().getParamList("").send().getData();
        List<Config> configList = new ArrayList<>();
        int id = 1;
        Date date = new Date();
        for (GovernParam gp : governParamList) {
            Config config = new Config();
            config.setId(id);
            config.setModule(gp.getParamItem().getModule());
            config.setName(gp.getParamItem().getName());
            config.setRangeDesc(gp.getParamItem().getDesc());
            config.setActiveBlock(0L);
            configList.add(config);
            config.setCreateTime(date);
            config.setUpdateTime(date);
            // The Alaya version handles the [minimum lock release amount attribute] specially because:
            // In the Alaya version, the debug_economic interface does not return the minimumRelease parameter, so it needs to be queried in the proposal contract and set to the BlockChainConfig instance.
            // Prevent the following code from not getting the parameter value when gettingValueInBlockChainConfig("minimumRelease") and reporting an error
            ModifiableGovernParamEnum paramEnum = ModifiableGovernParamEnum.getMap().get(config.getName());
            if (paramEnum == ModifiableGovernParamEnum.RESTRICTING_MINIMUM_RELEASE) {
                // If the parameter is the minimum lock release amount, set the minimum lock release amount attribute in blockChainConfig to the current query value
                String minimumRelease = gp.getParamValue().getValue();
                chainConfig.setRestrictingMinimumRelease(new BigDecimal(minimumRelease));
                //The following code can obtain this value from the blockChainConfig instance
            }
            // When the browser first starts, call the debugEconomicConfig interface in BlockChainConfig to obtain the parameters when the chain first starts.
            //So when synchronizing from scratch, you need to get the initial parameter value from BlockChainConfig
            String initValue = getValueInBlockChainConfig(config.getName());
            config.setInitValue(initValue);
            config.setStaleValue(initValue);
            config.setValue(initValue);
            id++;
        }
        configMapper.batchInsert(configList);
    }

    /**
     * Use the configuration in the configuration table to overwrite the BlockChainConfig in the memory and call it when restarting
     */
    public void overrideBlockChainConfig() {
        // Use the configuration of the database config table to overwrite the current configuration
        List<Config> configList = configMapper.selectByExample(null);
        ModifiableParam modifiableParam = ModifiableParam.builder().build().init(configList);

        //The minimum number of pledge tokens to create a validator (K)
        chainConfig.setStakeThreshold(modifiableParam.getStaking().getStakeThreshold());
        //The minimum number of Tokens required by the client for each commission and redemption (H)
        chainConfig.setDelegateThreshold(modifiableParam.getStaking().getOperatingThreshold());
        //Node pledge returns to the locking period
        chainConfig.setUnStakeRefundSettlePeriodCount(modifiableParam.getStaking().getUnStakeFreezeDuration().toBigInteger());
        //Number of unlock cycles for decommissioning
        if (modifiableParam.getStaking().getUnDelegateFreezeDuration().compareTo(BigDecimal.ZERO) > 0) {
            chainConfig.setUnDelegateFreezeDurationCount(modifiableParam.getStaking().getUnDelegateFreezeDuration().toBigInteger());
        }
        //Number of verification nodes for alternative settlement cycles (U)
        chainConfig.setSettlementValidatorCount(modifiableParam.getStaking().getMaxValidators().toBigInteger());
        //The maximum penalty for reporting is n3‱
        chainConfig.setDuplicateSignSlashRate(modifiableParam.getSlashing()
                                                             .getSlashFractionDuplicateSign()
                                                             .divide(BigDecimal.valueOf(10000), 16, RoundingMode.FLOOR));
        //Report reward n4%
        chainConfig.setDuplicateSignRewardRate(modifiableParam.getSlashing()
                .getDuplicateSignReportReward()
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR));
        //Evidence validity period
        chainConfig.setEvidenceValidEpoch(modifiableParam.getSlashing().getMaxEvidenceAge());
        //Deduct the number of block rewards
        chainConfig.setSlashBlockRewardCount(modifiableParam.getSlashing().getSlashBlocksReward());
        //Default maximum Gas per block
        chainConfig.setMaxBlockGasLimit(modifiableParam.getBlock().getMaxBlockGasLimit());
        // Zero block generation threshold, if this number is reached within the specified time range, there will be a penalty
        chainConfig.setZeroProduceNumberThreshold(modifiableParam.getSlashing().getZeroProduceNumberThreshold());
        // After the last zero block, if there is another zero block in the next N consensus cycles, zero block information will be recorded when these N consensus cycles are completed.
        chainConfig.setZeroProduceCumulativeTime(modifiableParam.getSlashing().getZeroProduceCumulativeTime());
        //Number of settlement cycles for zero block locking
        chainConfig.setZeroProduceFreezeDuration(modifiableParam.getSlashing().getZeroProduceFreezeDuration());
        //Reward ratio change period
        chainConfig.setRewardPerChangeInterval(modifiableParam.getStaking().getRewardPerChangeInterval());
        //Maximum change range of reward ratio +/-xxx
        chainConfig.setRewardPerMaxChangeRange(modifiableParam.getStaking().getRewardPerMaxChangeRange());
        //Additional issuance ratio
        chainConfig.setAddIssueRate(modifiableParam.getReward().getIncreaseIssuanceRatio().divide(new BigDecimal(10000)));
        //Minimum amount for lock-up release
        chainConfig.setRestrictingMinimumRelease(modifiableParam.getRestricting().getMinimumRelease());
    }

    /**
     * Configuration value rotation: the old value of value overwrites stale_value, and the new value in the parameter overwrites value
     *
     * @param activeConfigList activated configuration information list
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void rotateConfig(List<Config> activeConfigList) {
        //Update configuration table
        customConfigMapper.rotateConfig(activeConfigList);
        //Update BlockChainConfig in memory
        overrideBlockChainConfig();
    }

    /**
     * Get the corresponding current value in the current blockChainConfig based on the parameter name in the parameter proposal
     *
     * @param name
     * @return
     */
    public String getValueInBlockChainConfig(String name) {
        ModifiableGovernParamEnum paramEnum = ModifiableGovernParamEnum.getMap().get(name);
        String staleValue = "";
        switch (paramEnum) {
            // Pledge related
            case STAKE_THRESHOLD:
                staleValue = chainConfig.getStakeThreshold().toString();
                break;
            case OPERATING_THRESHOLD:
                staleValue = chainConfig.getDelegateThreshold().toString();
                break;
            case MAX_VALIDATORS:
                staleValue = chainConfig.getSettlementValidatorCount().toString();
                break;
            case UN_STAKE_FREEZE_DURATION:
                staleValue = chainConfig.getUnStakeRefundSettlePeriodCount().toString();
                break;
            case UN_DELEGATE_FREEZE_DURATION:
                staleValue = chainConfig.getUnDelegateFreezeDurationCount().toString();
                break;
            // Punishment related
            case SLASH_FRACTION_DUPLICATE_SIGN:
                staleValue = chainConfig.getDuplicateSignSlashRate().multiply(BigDecimal.valueOf(10000)).setScale(0).toString();
                break;
            case DUPLICATE_SIGN_REPORT_REWARD:
                staleValue = chainConfig.getDuplicateSignRewardRate().multiply(BigDecimal.valueOf(100)).setScale(0).toString();
                break;
            case MAX_EVIDENCE_AGE:
                staleValue = chainConfig.getEvidenceValidEpoch().toString();
                break;
            case SLASH_BLOCKS_REWARD:
                staleValue = chainConfig.getSlashBlockRewardCount().toString();
                break;
            // Block related
            case MAX_BLOCK_GAS_LIMIT:
                staleValue = chainConfig.getMaxBlockGasLimit().toString();
                break;
            // Zero block generation threshold, if this number is reached within the specified time range, there will be a penalty
            case ZERO_PRODUCE_NUMBER_THRESHOLD:
                staleValue = chainConfig.getZeroProduceNumberThreshold().toString();
                break;
            // After the last zero block, if there is another zero block in the next N consensus cycles, zero block information will be recorded when these N consensus cycles are completed.
            case ZERO_PRODUCE_CUMULATIVE_TIME:
                staleValue = chainConfig.getZeroProduceCumulativeTime().toString();
                break;
            // Node zero block penalty is locked time
            case ZERO_PRODUCE_FREEZE_DURATION:
                staleValue = chainConfig.getZeroProduceFreezeDuration().toString();
                break;
            case REWARD_PER_MAX_CHANGE_RANGE:
                staleValue = chainConfig.getRewardPerMaxChangeRange().toString();
                break;
            case REWARD_PER_CHANGE_INTERVAL:
                staleValue = chainConfig.getRewardPerChangeInterval().toString();
                break;
            case INCREASE_ISSUANCE_RATIO:
                staleValue = chainConfig.getAddIssueRate().multiply(new BigDecimal(10000)).setScale(0).toPlainString();
                break;
            case RESTRICTING_MINIMUM_RELEASE:
                //Minimum lock release amount (TURN)
                staleValue = chainConfig.getRestrictingMinimumRelease().toString();
                break;
            default:
                break;
        }
        return staleValue;
    }

    /**
     *
     * @param proposalVersion
     */
    public void configUnDelegateFreezeDuration(BigInteger proposalVersion) {
        BigInteger version = ChainVersionUtil.toBigIntegerVersion("1.3.0");
        if (proposalVersion.compareTo(version) >= 0) {
            try {
                List<Config> configList = configMapper.selectByExample(null);
                List<GovernParam> governParamList = turnClient.getProposalContract().getParamList("staking").send().getData();
                for (GovernParam gp : governParamList) {
                    Optional<Config> configOptional = configList.stream()
                                                                .filter(v -> v.getName().equalsIgnoreCase(gp.getParamItem().getName()))
                                                                .findFirst();
                    if (!configOptional.isPresent() && gp.getParamItem().getName().equalsIgnoreCase("unDelegateFreezeDuration")) {
                        Config config = new Config();
                        config.setModule(gp.getParamItem().getModule());
                        config.setName(gp.getParamItem().getName());
                        config.setRangeDesc(gp.getParamItem().getDesc());
                        config.setActiveBlock(0L);
                        String initValue = getValueInBlockChainConfig(config.getName());
                        config.setInitValue(initValue);
                        config.setStaleValue(initValue);
                        config.setValue(initValue);
                        configMapper.insertSelective(config);
                        log.info("New management parameters [{}]", config.getName());
                    }
                }
            } catch (Exception e) {
                log.error("Exception in proofreading management parameters", e);
            }
        }
    }

}
