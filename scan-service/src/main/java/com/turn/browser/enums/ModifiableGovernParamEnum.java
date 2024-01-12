package com.turn.browser.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Modifiable governance parameter enumeration
 */
public enum ModifiableGovernParamEnum {
    /**
     * Pledge threshold
     */
    STAKE_THRESHOLD("staking", "stakeThreshold"),
    /**
     * Commission threshold
     */
    OPERATING_THRESHOLD("staking", "operatingThreshold"),
    /**
     * Commission threshold
     */
    MAX_VALIDATORS("staking", "maxValidators"),
    /**
     * The number of settlement cycles required to un-pledge
     */
    UN_STAKE_FREEZE_DURATION("staking", "unStakeFreezeDuration"),
    /**
     * Number of unlocking cycles for decommissioning
     */
    UN_DELEGATE_FREEZE_DURATION("staking", "unDelegateFreezeDuration"),
    /**
     * Penalty for double signing is 10,000 points
     */
    SLASH_FRACTION_DUPLICATE_SIGN("slashing", "slashFractionDuplicateSign"),
    /**
     *Dual sign bonus percentage
     */
    DUPLICATE_SIGN_REPORT_REWARD("slashing", "duplicateSignReportReward"),
    /**
     *Dual sign bonus percentage
     */
    MAX_EVIDENCE_AGE("slashing", "maxEvidenceAge"),
    /**
     * Low block rate penalty block reward number
     */
    SLASH_BLOCKS_REWARD("slashing", "slashBlocksReward"),
    /**
     * Block maximum Gas limit
     */
    MAX_BLOCK_GAS_LIMIT("block", "maxBlockGasLimit"),
    /**
     * Zero block generation threshold, if this number is reached within the specified time range, there will be a penalty
     */
    ZERO_PRODUCE_NUMBER_THRESHOLD("slashing", "zeroProduceNumberThreshold"),
    /**
     * After the last zero block, if there is another zero block in the next N consensus cycles, zero block information will be recorded when these N consensus cycles are completed.
     */
    ZERO_PRODUCE_CUMULATIVE_TIME("slashing", "zeroProduceCumulativeTime"),
    /**
     * Node zero block penalty is locked for time
     */
    ZERO_PRODUCE_FREEZE_DURATION("slashing", "zeroProduceFreezeDuration"),
    REWARD_PER_MAX_CHANGE_RANGE("staking", "rewardPerMaxChangeRange"),
    REWARD_PER_CHANGE_INTERVAL("staking", "rewardPerChangeInterval"),
    INCREASE_ISSUANCE_RATIO("reward", "increaseIssuanceRatio"),
    RESTRICTING_MINIMUM_RELEASE("restricting", "minimumRelease");

    private String module;

    private String name;

    ModifiableGovernParamEnum(String module, String name) {
        this.module = module;
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    private static final Map<String, ModifiableGovernParamEnum> MAP = new HashMap<>();

    public static Map<String, ModifiableGovernParamEnum> getMap() {
        return MAP;
    }

    static {
        Arrays.asList(ModifiableGovernParamEnum.values()).forEach(paramEnum -> MAP.put(paramEnum.getName(), paramEnum));
    }
}
