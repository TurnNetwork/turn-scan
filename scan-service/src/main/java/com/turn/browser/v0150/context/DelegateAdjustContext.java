package com.turn.browser.v0150.context;

import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.CustomDelegation;
import com.turn.browser.bean.CustomStaking;
import com.turn.browser.dao.entity.Delegation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Commissioned adjustment context
 *Required data:
 * 1. Pledge information
 * 2. Node information
 * 3. Commission information
 */
@Slf4j
@Data
public class DelegateAdjustContext extends AbstractAdjustContext {

    private Delegation delegation;

    /**
     * Verify whether the relevant amount of the entrustment meets the adjustment requirements
     */
    @Override
    void validateAmount() {
        if (delegation == null) {
            errors.add("[Error]: The delegation record is missing: [node ID=" + adjustParam.getNodeId() + ", node pledge block number=" + adjustParam.getStakingBlockNum() + ", principal=" + adjustParam.getAddr () + "]");
            return;
        }

        //Set the original state of the delegate
        adjustParam.setIsHistory(delegation.getIsHistory());
        //Set the original amount of the order
        adjustParam.setDelegateHes(delegation.getDelegateHes());
        adjustParam.setDelegateLocked(delegation.getDelegateLocked());
        adjustParam.setDelegateReleased(delegation.getDelegateReleased());
        //Set the original commission summary amount of the node
        adjustParam.setNodeStatDelegateValue(node.getStatDelegateValue());
        adjustParam.setNodeStatDelegateReleased(node.getStatDelegateReleased());
        //Set the original commission summary amount of pledge
        adjustParam.setStakeStatDelegateHes(staking.getStatDelegateHes());
        adjustParam.setStakeStatDelegateLocked(staking.getStatDelegateLocked());
        adjustParam.setStakeStatDelegateReleased(staking.getStatDelegateReleased());
        if (adjustParam.getStatus() == CustomStaking.StatusEnum.EXITING.getCode() || adjustParam.getStatus() == CustomStaking.StatusEnum.EXITED.getCode()) {
            // For nodes that are exiting or have exited, subtract hes and lock from the delegateReleased of the delegate
            if (adjustParam.getDelegateReleased().compareTo(adjustParam.getHes().add(adjustParam.getLock())) < 0) {
                errors.add("[Error]: The commission record [amount to be withdrawn [" + adjustParam.getDelegateReleased() + "]] is less than the adjustment parameter [hesitation period [" + adjustParam.getHes() + "] + locking period [" + adjustParam.getLock() + "】]Amount【" + adjustParam.getHes()
                        .add(adjustParam.getLock()) + "】!");
            }
        } else {
            // Candidate or locked nodes will be deducted from the respective hesitation period or locking period amount of the entrustment
            if (adjustParam.getDelegateHes().compareTo(adjustParam.getHes()) < 0) {
                errors.add("[Error]: The commission record [the amount during the hesitation period [" + adjustParam.getDelegateHes() + "]] is less than the adjustment parameter [the amount during the hesitation period [" + adjustParam.getHes() + "]]!") ;
            }
            if (adjustParam.getDelegateLocked().compareTo(adjustParam.getLock()) < 0) {
                errors.add("[Error]: The commission record [lock period amount [" + adjustParam.getDelegateLocked() + "]] is less than the adjustment parameter [lock period amount [" + adjustParam.getLock() + "]]!") ;
            }
        }
    }

    @Override
    void calculateAmountAndStatus() {
        if (adjustParam.getStatus() == CustomStaking.StatusEnum.EXITING.getCode() || adjustParam.getStatus() == CustomStaking.StatusEnum.EXITED.getCode()) {
            // Exiting or exiting nodes
            // 1. Subtract hes and lock from the delegateReleased of the delegate
            adjustParam.setDelegateReleased(adjustParam.getDelegateReleased().subtract(adjustParam.getHes()).subtract(adjustParam.getLock()));
            // 2. Subtract the lock from the node's statDelegateReleased
            adjustParam.setNodeStatDelegateReleased(adjustParam.getNodeStatDelegateReleased().subtract(adjustParam.getHes()).subtract(adjustParam.getLock()));
            // 3. Subtract hes and lock from the pledged statDelegateReleased
            adjustParam.setStakeStatDelegateReleased(adjustParam.getStakeStatDelegateReleased().subtract(adjustParam.getHes()).subtract(adjustParam.getLock()));
        } else {
            // Candidate or locked node
            // 1. Deduct from the amount of the commission’s hesitation period or lock-in period
            adjustParam.setDelegateHes(adjustParam.getDelegateHes().subtract(adjustParam.getHes()));
            adjustParam.setDelegateLocked(adjustParam.getDelegateLocked().subtract(adjustParam.getLock()));
            // 2. Subtract hes and lock from the node's statDelegateValue
            adjustParam.setNodeStatDelegateValue(adjustParam.getNodeStatDelegateValue().subtract(adjustParam.getHes()).subtract(adjustParam.getLock()));
            // 3. Subtract hes from the pledged statDelegateHes
            adjustParam.setStakeStatDelegateHes(adjustParam.getStakeStatDelegateHes().subtract(adjustParam.getHes()));
            // 4. Subtract lock from pledged statDelegateLocked
            adjustParam.setStakeStatDelegateLocked(adjustParam.getStakeStatDelegateLocked().subtract(adjustParam.getLock()));
            // 5. Subtract hes and lock from the node's total effective pledge and delegation totalValue
            adjustParam.setNodeTotalValue(adjustParam.getNodeTotalValue().subtract(adjustParam.getHes()).subtract(adjustParam.getLock()));
        }
        // After deducting the amount from the pledge-related amount, if (delegateHes+delegateLocked+delegateReleased)==0, the delegation is set to history
        BigDecimal delegateHes = adjustParam.getDelegateHes();
        BigDecimal delegateLocked = adjustParam.getDelegateLocked();
        BigDecimal delegateReleased = adjustParam.getDelegateReleased();
        if (delegateHes.add(delegateLocked).add(delegateReleased).compareTo(BigDecimal.ZERO) <= 0) {
            adjustParam.setIsHistory(CustomDelegation.YesNoEnum.YES.getCode());

            //Add the reward in the adjustment parameters to the node and pledge
            adjustParam.setStakeHaveDeleReward(adjustParam.getStakeHaveDeleReward().add(adjustParam.getReward()));
            adjustParam.setNodeHaveDeleReward(adjustParam.getNodeHaveDeleReward().add(adjustParam.getReward()));
        }
    }

    @Override
    String extraContextInfo() {
        if (delegation == null) {
            return "";
        }
        String extra = "委托记录：\n" + JSON.toJSONString(delegation);
        return extra;
    }

}
