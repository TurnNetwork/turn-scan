//package com.turn.browser.task;
//
//import com.turn.browser.AgentTestBase;
//import com.turn.browser.AgentTestData;
//import com.turn.browser.enums.AppStatus;
//import com.turn.browser.utils.AppStatusUtil;
//import com.turn.browser.dao.custommapper.CustomStakingHistoryMapper;
//import com.turn.browser.dao.mapper.StakingMapper;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.ArrayList;
//
//import static org.mockito.Mockito.*;
//
///**
// * @description:
// **/
//@RunWith(MockitoJUnitRunner.Silent.class)
//public class StakingMigrateTaskTest extends AgentTestData {
//    @Mock
//    private StakingMapper stakingMapper;
//    @Mock
//    private CustomStakingHistoryMapper customStakingHistoryMapper;
//    @InjectMocks
//    @Spy
//    private StakingMigrateTask target;
//
//    @Before
//    public void setup() {
//        when(stakingMapper.selectByExample(any())).thenReturn(new ArrayList<>(stakingList));
//    }
//
//    @Test
//    public void test() {
//        AppStatusUtil.setStatus(AppStatus.RUNNING);
//        target.stakingMigrate();
//        verify(target, times(1)).stakingMigrate();
//
//        doThrow(new RuntimeException("")).when(stakingMapper).selectByExample(any());
//        target.stakingMigrate();
//    }
//}
