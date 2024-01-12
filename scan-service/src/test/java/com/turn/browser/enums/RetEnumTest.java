package com.turn.browser.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class RetEnumTest {

	@Test
	public void test() {
		RetEnum retEnum = RetEnum.valueOf("RET_PARAM_VALLID");
		assertTrue(retEnum.getCode()==-1);
		assertTrue(retEnum.getName().equals("Invalid request parameters"));
	}
	
	@Test
	public void testGetEnumByCodeValue() {
		RetEnum retEnum = RetEnum.getEnumByCodeValue(0);
		assertTrue(retEnum.getCode()==0);
		
		retEnum = RetEnum.getEnumByCodeValue(10);
		assertNull(retEnum);
	}

}
