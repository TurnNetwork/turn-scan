package com.turn.browser.dao.param;

import com.turn.browser.AgentTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BusinessParamTest extends AgentTestBase {

	@Test
	public void test(){
		BusinessParam.YesNoEnum yesNoEnum = BusinessParam.YesNoEnum.NO;
		yesNoEnum.getCode();
		yesNoEnum.getDesc();
		assertTrue(true);
	}
}
