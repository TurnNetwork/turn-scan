package com.turn.browser.utils;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;


public class ChainVersionUtilTest {

    @Test
    public void shouldEquals() {
        BigInteger version = ChainVersionUtil.toBigIntegerVersion("1.1.0");
        assertEquals(BigInteger.valueOf(65792), version);
        try {
			version = ChainVersionUtil.toBigIntegerVersion("abcdef");
		} catch (Exception e) {
			assertTrue(e instanceof NumberFormatException);
		}
    }


    @Test
    public void shouldVersionEquests() {
        String version = ChainVersionUtil.toStringVersion(BigInteger.valueOf(65792));
        assertEquals("1.1.0", version);
    }
    
    @Test
    public void TestTransferBigVersion() {
    	BigInteger version = ChainVersionUtil.toBigVersion(BigInteger.valueOf(65792));
        assertEquals("65792", version.toString());
    }
}
