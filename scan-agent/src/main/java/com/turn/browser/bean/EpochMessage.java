package com.turn.browser.bean;


import com.bubble.contracts.dpos.dto.resp.Node;
import com.turn.browser.service.epoch.EpochRetryService;
import com.turn.browser.service.epoch.EpochService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * All event information related to the current block (consensus cycle switching event/settlement cycle switching event/additional issuance cycle switching event)
 */
@Data
@Slf4j
public class EpochMessage {

    private BigInteger currentBlockNumber;

    /**
     * Current consensus cycle number
     */
    private BigInteger consensusEpochRound = BigInteger.ZERO;

    /**
     * Current settlement cycle number
     */
    private BigInteger settleEpochRound = BigInteger.ZERO;

    /**
     * The current round number of the additional issuance cycle
     */
    private BigInteger issueEpochRound = BigInteger.ZERO;

    /**
     * The reward value of each block in the current additional issuance cycle is BR/the total number of blocks in the additional issuance cycle.
     */
    private BigDecimal blockReward = BigDecimal.ZERO;

    /**
     * The pledge reward value of each settlement cycle of the current additional issuance cycle SSR=SR/the number of settlement cycles included in an additional issuance cycle
     */
    private BigDecimal settleStakeReward = BigDecimal.ZERO;

    /**
     * The pledge reward value of each node in the current settlement period is PerNodeSR=SSR/the actual number of verifiers in the current settlement period.
     */
    private BigDecimal stakeReward = BigDecimal.ZERO;

    /**
     * The pledge reward value of each node in the previous settlement cycle PerNodeSR=SSR/the actual number of verifiers in the current settlement cycle
     */
    private BigDecimal preStakeReward = BigDecimal.ZERO;

    /**
     * List of validators of the previous consensus cycle
     */
    private List<Node> preValidatorList = new ArrayList<>();

    /**
     * Current consensus cycle validator list
     */
    private List<Node> curValidatorList = new ArrayList<>();

    /**
     * List of validators of the previous settlement cycle
     */
    private List<Node> preVerifierList = new ArrayList<>();

    /**
     * List of validators for the current settlement cycle
     */
    private List<Node> curVerifierList = new ArrayList<>();

    /**
     * Current expected number of blocks
     */
    private Long expectBlockCount = 0L;

    private EpochMessage() {
    }

    public static EpochMessage newInstance() {
        return new EpochMessage();
    }

    public EpochMessage updateWithEpochService(EpochService epochService) {
        BeanUtils.copyProperties(epochService, this);
        return this;
    }

    public EpochMessage updateWithEpochRetryService(EpochRetryService epochRetryService) {
        BeanUtils.copyProperties(epochRetryService, this);
        preValidatorList.addAll(epochRetryService.getPreValidators());
        curValidatorList.addAll(epochRetryService.getCurValidators());
        preVerifierList.addAll(epochRetryService.getPreVerifiers());
        curVerifierList.addAll(epochRetryService.getCurVerifiers());
        return this;
    }

}
