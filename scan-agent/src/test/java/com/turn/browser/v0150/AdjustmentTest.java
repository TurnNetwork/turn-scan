//package com.turn.browser.v015;
//
//import com.alaya.protocol.Web3j;
//import com.alaya.protocol.http.HttpService;
//import com.alibaba.fastjson.JSON;
//import com.turn.browser.AgentApplication;
//import com.turn.browser.client.TurnClient;
//import com.turn.browser.client.SpecialApi;
//import com.turn.browser.utils.NetworkParams;
//import com.turn.browser.v015.bean.AdjustParam;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import javax.annotation.Resource;
//import java.math.BigInteger;
//import java.util.List;
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = AgentApplication.class,properties = {"spring.profiles.active=dev"})
//public class AdjustmentTest {
//
//    @Resource
//    private SpecialApi specialApi;
//    @Resource
//    private TurnClient turnClient;
//
//    @Test
//    public void testSpc() throws Exception {
//        List<AdjustParam> params = specialApi.getStakingDelegateAdjustDataList(turnClient.getWeb3jWrapper().getWeb3j(), BigInteger.valueOf(9641));
//
//        log.debug(JSON.toJSONString(params));
//    }
//}