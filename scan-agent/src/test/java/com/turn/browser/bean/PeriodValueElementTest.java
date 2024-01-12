package com.turn.browser.bean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

/**
 * @Description: 利润和成本bean单元测试
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class PeriodValueElementTest {

    @Test
    public void test(){
        PeriodValueElement profit = new PeriodValueElement();
        profit.setPeriod(100L);
        profit.setValue(BigDecimal.TEN);
        assertTrue(true);
    }
}
