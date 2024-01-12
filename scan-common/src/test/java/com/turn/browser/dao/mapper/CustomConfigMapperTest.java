//package com.turn.browser.dao.mapper;//package com.turn.browser.dao.mapper;
//
//import com.turn.browser.AgentApplication;
//import com.turn.browser.AgentTestBase;
//import com.turn.browser.config.BlockChainConfig;
//import com.turn.browser.dao.entity.Config;
//import com.turn.browser.service.govern.ParameterService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @Description: 质押相关入库测试类
// */
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = AgentApplication.class, value = "spring.profiles.active=unit")
//@SpringBootApplication
//public class CustomConfigMapperTest extends AgentTestBase {
//    @Autowired
//    private ParameterService parameterService;
//    @Autowired
//    private BlockChainConfig chainConfig;
//
//    @Test
//    public void rotateConfig () {
//        List<Config> configList = new ArrayList<>();
//        Config config = new Config();
//        config.setModule("staking");
//        config.setName("stakeThreshold");
//        config.setValue("22222222222");
//        configList.add(config);
//
//        //更新内存中的BlockChainConfig
//        parameterService.rotateConfig(configList);
//        log.error("");
//    }
//}