package com.turn.browser.v0150.context;

import com.turn.browser.TestData;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.v0150.bean.AdjustParam;
import com.turn.browser.dao.entity.Delegation;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.bean.CustomStaking;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class DelegateAdjustContextTest extends TestData {
    private DelegateAdjustContext target;
    private AdjustParam adjustParam;
    private Node node;
    private Staking staking;
    private Delegation delegation;

    @Before
    public void setup(){
        target = new DelegateAdjustContext();
        for (AdjustParam param : adjustParamList) {
            if ("delegate".equals(param.getOptType())) {
                adjustParam = param;
                break;
            }
        }
        node = nodeList.get(0);
        staking = stakingList.get(0);
        staking.setStatus(CustomStaking.StatusEnum.CANDIDATE.getCode());
        delegation = delegationList.get(0);
    }

    @Test
    public void blockChainConfigNullTest() throws BlockNumberException {
        List<String> errors = target.validate();
        Assert.assertEquals(1, errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("BlockChainConfig is missing"));
    }

    @Test
    public void adjustParamNullTest() throws BlockNumberException {
        target.setChainConfig(blockChainConfig);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Adjustment data is missing"));
    }

    @Test
    public void nodeStakingNullTest() throws BlockNumberException {
        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        List<String> errors = target.validate();
        Assert.assertEquals(2,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Node record is missing"));
        Assert.assertTrue(errorInfo.contains("[Error]: stake record is missing"));
    }

    @Test
    public void stakingNullTest() throws BlockNumberException {
        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: stake record is missing"));
    }

    @Test
    public void nodeNullTest() throws BlockNumberException {
        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: node record is missing"));
    }

    @Test
    public void delegateNullTest() throws BlockNumberException {
        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: entrust record is missing"));
    }

    // ********************The node status is candidate or locked********************
    @Test
    public void delegateHesEqualTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.ONE);
        delegation.setDelegateHes(BigDecimal.TEN);
        delegation.setDelegateLocked(BigDecimal.TEN);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void delegateLockedEqualTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.ONE);
        adjustParam.setLock(BigDecimal.TEN);
        delegation.setDelegateHes(BigDecimal.TEN);
        delegation.setDelegateLocked(BigDecimal.TEN);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void delegateHesNotEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        delegation.setDelegateHes(BigDecimal.ONE);
        delegation.setDelegateLocked(BigDecimal.valueOf(100));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: delegate record [demurrage period amount"));
    }

    @Test
    public void delegateLockedNotEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        delegation.setDelegateHes(BigDecimal.valueOf(100));
        delegation.setDelegateLocked(BigDecimal.ONE);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Commission record [Lock-up period amount"));
    }

    @Test
    public void delegateBothNotEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        delegation.setDelegateHes(BigDecimal.ONE);
        delegation.setDelegateLocked(BigDecimal.ONE);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(2,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Commission record [demurrage period amount"));
        Assert.assertTrue(errorInfo.contains("[Error]: Commission record [Lock-up period amount"));
    }

    @Test
    public void delegateBothEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.ONE);
        adjustParam.setLock(BigDecimal.ONE);
        delegation.setDelegateHes(BigDecimal.TEN);
        delegation.setDelegateLocked(BigDecimal.TEN);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }


    // ********************The node status is exiting or exited********************
    @Test
    public void delegateReleasedEqualTest() throws BlockNumberException {
        staking.setStatus(CustomStaking.StatusEnum.EXITED.getCode());
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        delegation.setDelegateReleased(BigDecimal.valueOf(20));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void delegateReleasedNotEqualTest() throws BlockNumberException {
        staking.setStatus(CustomStaking.StatusEnum.EXITED.getCode());
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        delegation.setDelegateReleased(BigDecimal.valueOf(50));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void delegateReleasedNotEnoughTest() throws BlockNumberException {
        staking.setStatus(CustomStaking.StatusEnum.EXITED.getCode());
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        delegation.setDelegateReleased(BigDecimal.valueOf(15));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        target.setDelegation(delegation);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("Commission record [Amount to be withdrawn"));
    }
}
