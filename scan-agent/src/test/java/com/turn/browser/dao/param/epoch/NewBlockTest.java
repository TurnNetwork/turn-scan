package com.turn.browser.dao.param.epoch;

import com.turn.browser.AgentTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

/**
 * @Description: 新区块更新入参
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class NewBlockTest extends AgentTestBase {

    @Test
    public void test(){
        NewBlock target = NewBlock.builder()
                .blockRewardValue(BigDecimal.TEN)
                .feeRewardValue(BigDecimal.TEN)
                .nodeId("0xdd")
                .stakingBlockNum(BigInteger.TEN)
                .build();
        target.setBlockRewardValue(null)
                .setFeeRewardValue(null)
                .setNodeId(null)
                .setStakingBlockNum(null);
        target.getBlockRewardValue();
        target.getFeeRewardValue();
        target.getNodeId();
        target.getStakingBlockNum();
        target.getBusinessType();

        assertTrue(true);
    }
}
