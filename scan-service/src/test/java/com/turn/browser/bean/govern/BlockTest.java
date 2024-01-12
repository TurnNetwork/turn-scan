package com.turn.browser.bean.govern;

import com.alibaba.fastjson.JSON;
import com.turn.browser.bean.govern.Block;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

/**
 * @Description:
 */
@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class BlockTest {

    @Test
    public void blockTest(){
        Block block = Block.builder()
                .maxBlockGasLimit(BigDecimal.TEN)
                .build();
        block.setMaxBlockGasLimit(BigDecimal.ONE);
        log.debug("staking : {}", JSON.toJSONString(block));
        block.getMaxBlockGasLimit();
        log.debug("value : {} ",block.getMaxBlockGasLimit());

    }
}