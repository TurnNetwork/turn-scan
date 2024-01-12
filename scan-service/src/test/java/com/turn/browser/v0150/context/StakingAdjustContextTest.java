package com.turn.browser.v0150.context;

import com.turn.browser.TestData;
import com.turn.browser.exception.BlockNumberException;
import com.turn.browser.v0150.bean.AdjustParam;
import com.turn.browser.dao.entity.Node;
import com.turn.browser.dao.entity.Staking;
import com.turn.browser.bean.CustomStaking;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class StakingAdjustContextTest extends TestData {
    private StakingAdjustContext target;
    private AdjustParam adjustParam;
    private Node node;
    private Staking staking;

    @Before
    public void setup(){
        target = new StakingAdjustContext();
        for (AdjustParam param : adjustParamList) {
            if ("staking".equals(param.getOptType())) {
                adjustParam = param;
                break;
            }
        }
        node = nodeList.get(0);
        staking = stakingList.get(0);
        // The default is candidate status
        staking.setStatus(CustomStaking.StatusEnum.CANDIDATE.getCode());
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
        Assert.assertTrue(errorInfo.contains("[Error]: Stake record is missing"));
        Assert.assertTrue(errorInfo.contains("[Error]: Node record is missing"));
    }

    @Test
    public void stakingNullTest() throws BlockNumberException {
        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Stake record is missing"));
    }

    @Test
    public void nodeNullTest() throws BlockNumberException {
        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Node record is missing"));
    }

    //**********************The node status is candidate or locked************************
    @Test
    public void stakingHesEqualTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.ONE);
        staking.setStakingHes(BigDecimal.TEN);
        staking.setStakingLocked(BigDecimal.TEN);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void stakingLockedEqualTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.ONE);
        adjustParam.setLock(BigDecimal.TEN);
        staking.setStakingHes(BigDecimal.TEN);
        staking.setStakingLocked(BigDecimal.TEN);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void stakingHesNotEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        staking.setStakingHes(BigDecimal.ONE);
        staking.setStakingLocked(BigDecimal.valueOf(100));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Pledge record [hesitation period amount"));
    }

    @Test
    public void stakingLockedNotEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.valueOf(200));
        staking.setStakingHes(BigDecimal.valueOf(100));
        staking.setStakingLocked(BigDecimal.TEN);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Pledge record [Lock-up period amount"));
    }

    @Test
    public void stakingBothNotEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        staking.setStakingHes(BigDecimal.ONE);
        staking.setStakingLocked(BigDecimal.ONE);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(2,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Stake record [hesitation period amount"));
        Assert.assertTrue(errorInfo.contains("[Error]: Stake record [Lock-up period amount"));
    }

    @Test
    public void stakingBothEnoughTest() throws BlockNumberException {
        adjustParam.setHes(BigDecimal.ONE);
        adjustParam.setLock(BigDecimal.ONE);
        staking.setStakingHes(BigDecimal.TEN);
        staking.setStakingLocked(BigDecimal.TEN);

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    // ********************The node status is exiting or exited********************
    @Test
    public void stakingReductionEqualTest() throws BlockNumberException {
        staking.setStatus(CustomStaking.StatusEnum.EXITED.getCode());
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        staking.setStakingReduction(BigDecimal.valueOf(20));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void stakingReductionNotEqualTest() throws BlockNumberException {
        staking.setStatus(CustomStaking.StatusEnum.EXITED.getCode());
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        staking.setStakingReduction(BigDecimal.valueOf(50));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(0,errors.size());
    }

    @Test
    public void stakingReductionNotEnoughTest() throws BlockNumberException {
        staking.setStatus(CustomStaking.StatusEnum.EXITED.getCode());
        adjustParam.setHes(BigDecimal.TEN);
        adjustParam.setLock(BigDecimal.TEN);
        staking.setStakingReduction(BigDecimal.valueOf(15));

        target.setChainConfig(blockChainConfig);
        target.setAdjustParam(adjustParam);
        target.setNode(node);
        target.setStaking(staking);
        List<String> errors = target.validate();
        Assert.assertEquals(1,errors.size());
        String errorInfo = target.errorInfo();
        Assert.assertTrue(errorInfo.contains("[Error]: Stake record [amount returned"));
    }
}
