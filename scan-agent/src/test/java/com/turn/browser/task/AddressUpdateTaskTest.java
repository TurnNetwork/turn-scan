//package com.turn.browser.task;
//
//import com.turn.browser.AgentTestBase;
//import com.turn.browser.AgentTestData;
//import com.turn.browser.enums.AppStatus;
//import com.turn.browser.utils.AppStatusUtil;
//import com.turn.browser.dao.custommapper.StatisticBusinessMapper;
//import com.turn.browser.dao.mapper.AddressMapper;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.ArrayList;
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
///**
// * @description:
// **/
//@RunWith(MockitoJUnitRunner.Silent.class)
//public class AddressUpdateTaskTest extends AgentTestData {
//    @Mock
//    private StatisticBusinessMapper statisticBusinessMapper;
//    @Mock
//    private AddressMapper addressMapper;
//    @InjectMocks
//    @Spy
//    private AddressUpdateTask target;
//
//    @Before
//    public void setup() throws Exception {
//        when(addressMapper.selectByExample(any())).thenReturn(new ArrayList<>(addressList));
//        when(statisticBusinessMapper.getAddressStatisticsFromStaking(any())).thenReturn(Collections.emptyList());
//        when(statisticBusinessMapper.getAddressStatisticsFromDelegation(any())).thenReturn(Collections.emptyList());
//    }
//
//    @Test
//    public void test(){
//        AppStatusUtil.setStatus(AppStatus.RUNNING);
//        target.batchUpdate(4,55);
//        //verify(target, times(1)).batchUpdate(any(),any());
//    }
//}
