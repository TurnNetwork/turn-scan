package com.turn.browser.config;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DownFileCommonTest {

	@Test
	public void testDownload() throws IOException {
		DownFileCommon downFileCommon = new DownFileCommon();
		HttpServletResponse httpServletResponse = EasyMock.createMock(HttpServletResponse.class);
		byte[] data = new byte[0];
		try {
			downFileCommon.download(httpServletResponse, "1.csv", 0, data);
		} catch (Exception e) {
			assertEquals(e.getMessage(), null);
		}
		
	}
}
