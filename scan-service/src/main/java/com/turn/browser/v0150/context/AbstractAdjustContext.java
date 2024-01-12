package com.turn.browser.v0150.context;

import com.alibaba.fastjson.JSON;
import com.turn.browser.config.BlockChainConfig;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.v0150.bean.AdjustParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Adjustment context
 */
@Slf4j()
@Data
public abstract class AbstractAdjustContext {

    protected BigInteger blockNumber;

    protected AdjustParam adjustParam;

    protected Staking staking;

    protected Node node;

    protected List<String> errors = new ArrayList<>();

    protected BlockChainConfig chainConfig;

    /**
     * Check whether the context is correct: whether the required data is available
     *
     * @return
     */
    public final List<String> validate() throws BlockNumberException {
        if (chainConfig == null) {
            errors.add("[Error]: BlockChainConfig is missing!");
            return errors;
        }
        if (adjustParam == null) {
            errors.add("[Error]: The adjustment data is missing!");
            return errors;
        }
        // Check whether all the adjustment target records exist
        if (node == null) {
            errors.add("[Error]: Node record missing: [Node ID=" + adjustParam.getNodeId() + "]");
        }
        if (staking == null) {
            errors.add("[Error]: The pledge record is missing: [node ID=" + adjustParam.getNodeId() + ", node pledge block number=" + adjustParam.getStakingBlockNum() + "]");
        }
        if (node == null || staking == null) {
            return errors;
        }
        //Set the original status and value related to pledge
        adjustParam.setStatus(staking.getStatus());
        adjustParam.setIsConsensus(staking.getIsConsensus());
        adjustParam.setIsSettle(staking.getIsSettle());
        adjustParam.setStakingHes(staking.getStakingHes());
        adjustParam.setStakingLocked(staking.getStakingLocked());
        adjustParam.setStakingReduction(staking.getStakingReduction());
        adjustParam.setStakingReductionEpoch(staking.getStakingReductionEpoch());
        adjustParam.setStakeHaveDeleReward(staking.getHaveDeleReward());
        //Set the original status and value of node-related statistical fields
        adjustParam.setNodeTotalValue(node.getTotalValue());
        adjustParam.setNodeStatDelegateValue(node.getStatDelegateValue());
        adjustParam.setNodeStatDelegateReleased(node.getStatDelegateReleased());
        adjustParam.setNodeHaveDeleReward(node.getHaveDeleReward());
        //Set the original status and value of pledge-related statistical fields
        adjustParam.setStakeStatDelegateHes(staking.getStatDelegateHes());
        adjustParam.setStakeStatDelegateLocked(staking.getStatDelegateLocked());
        adjustParam.setStakeStatDelegateReleased(staking.getStatDelegateReleased());
        adjustParam.setUnStakeFreezeDuration(staking.getUnStakeFreezeDuration());
        adjustParam.setLeaveTime(staking.getLeaveTime());
        adjustParam.setUnStakeEndBlock(staking.getUnStakeEndBlock());

        // Check whether each amount is sufficient for deduction
        validateAmount();
        // The status information of the commission or pledge in the adjustment parameters can only be set when the context is correct.
        if (!errors.isEmpty()) {
            return errors;
        }
        calculateAmountAndStatus();
        return errors;
    }

    /**
     * attention: invoke this method after validate()
     *
     * @return
     */
    public final String contextInfo() {
        StringBuilder sb = new StringBuilder(adjustParam.getOptType()).append("Adjustment parameters:\n")
                .append(JSON.toJSONString(adjustParam))
                .append("\n")
                .append("Node record:\n")
                .append(JSON.toJSONString(node))
                .append("\n")
                .append("Pledge record:\n")
                .append(JSON.toJSONString(staking))
                .append("\n");
        String extraContextInfo = extraContextInfo();
        if (StringUtils.isNotBlank(extraContextInfo)) {
            sb.append(extraContextInfo).append("\n");
        }
        return sb.toString();
    }

    /**
     * attention: invoke this method after validate()
     * Convert the error list into error string information
     *
     * @return
     */
    public final String errorInfo() {
        if (errors.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("============ ");
        if (adjustParam != null) {
            sb.append(adjustParam.getOptType()).append("Adjustment error============\n").append(adjustParam.getOptType()).append("Adjustment parameters: \n").append(JSON.toJSONString(adjustParam)).append("\n");
        } else {
            sb.append("Account adjustment error ============\n");
        }

        if (node != null) {
            sb.append("Node record:\n").append(JSON.toJSONString(node)).append("\n");
        }
        if (staking != null) {
            sb.append("Pledge record:\n").append(JSON.toJSONString(staking)).append("\n");
        }
        String extraContextInfo = extraContextInfo();
        if (StringUtils.isNotBlank(extraContextInfo)) {
            sb.append(extraContextInfo).append("\n");
        }
        errors.forEach(e -> sb.append(e).append("\n"));
        log.error("{}", sb.toString());
        return sb.toString();
    }

    /**
     * Verify the adjustment amount data
     */
    abstract void validateAmount();

    /**
     * attention: invoke this method after validateAmount()
     * Calculate the amount and status, and adjust the status related to entrustment or pledge in the parameters:
     * 1. If it is currently a commissioned adjustment context, update the isHistory field of the commissioned adjustment parameters.
     * 2. If it is currently a pledge adjustment context, update the status, isConsensus, and isSettle fields of the pledge adjustment parameters.
     */
    abstract void calculateAmountAndStatus() throws BlockNumberException;

    /**
     * attention: invoke this method after validate()
     * Additional contextual information
     */
    abstract String extraContextInfo();

}
