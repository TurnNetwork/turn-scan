//package com.turn.browser.dao.mapper;
//
//import com.turn.browser.AgentApplication;
//import com.turn.browser.AgentTestBase;
//import com.turn.browser.dao.mapper.SlashBusinessMapper;
//import com.turn.browser.dao.param.ppos.Report;
//import com.turn.browser.dao.entity.Node;
//import com.turn.browser.dao.entity.Slash;
//import com.turn.browser.dao.entity.Staking;
//import com.turn.browser.dao.entity.StakingKey;
//import com.turn.browser.dao.mapper.NodeMapper;
//import com.turn.browser.dao.mapper.SlashMapper;
//import com.turn.browser.dao.mapper.StakingMapper;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.util.StringUtils;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//
///**
// * @Description: 举报相关入库测试类
// */
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = AgentApplication.class, value = "spring.profiles.active=unit")
//@SpringBootApplication
//public class SlashBusinessTest extends AgentTestBase {
//    @Autowired
//    private SlashBusinessMapper slashBusinessMapper;
//    @Autowired
//    private StakingMapper stakingMapper;
//    @Autowired
//    private NodeMapper nodeMapper;
//    @Autowired
//    private SlashMapper slashMapper;
//
//    @Test
//    public void reportDuplicateSignMapper () {
//        Report reportDuplicateSignParam = reportDuplicateSignParam();
//        slashBusinessMapper.report(reportDuplicateSignParam);
//        //node更新数据验证
//        Node node = nodeMapper.selectByPrimaryKey(reportDuplicateSignParam.getNodeId());
//        assertEquals(node.getStatus().intValue(), reportDuplicateSignParam.getCodeStatus());
//        assertEquals(node.getStakingReductionEpoch().intValue(), reportDuplicateSignParam.getSettingEpoch());
//        assertEquals(node.getStakingReduction(), reportDuplicateSignParam.getCodeCurStakingLocked());
//        //staking更新数据验证
//        Staking staking = getStaking(reportDuplicateSignParam.getNodeId(), reportDuplicateSignParam.getStakingBlockNum().longValue());
//        assertEquals(staking.getStatus().intValue(), reportDuplicateSignParam.getCodeStatus());
//        assertEquals(staking.getStakingReductionEpoch().intValue(), reportDuplicateSignParam.getSettingEpoch());
//        assertEquals(staking.getStakingReduction(), reportDuplicateSignParam.getCodeCurStakingLocked());
//        //slash插入数据验证
//        Slash slash = slashMapper.selectByPrimaryKey(reportDuplicateSignParam.getTxHash());
//        assertFalse(StringUtils.isEmpty(slash));
//
//    }
//
//    public Staking getStaking ( String nodeId, long stakingBlockNumer ) {
//        StakingKey stakingKey = new StakingKey();
//        stakingKey.setNodeId(nodeId);
//        stakingKey.setStakingBlockNum(stakingBlockNumer);
//        return stakingMapper.selectByPrimaryKey(stakingKey);
//    }
//}