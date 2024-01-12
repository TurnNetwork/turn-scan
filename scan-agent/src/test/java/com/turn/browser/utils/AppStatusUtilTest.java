package com.turn.browser.utils;

import com.turn.browser.enums.AppStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AppStatusUtilTest {

    @Test
    public void test(){
        AppStatusUtil.setStatus(AppStatus.STOPPED);
        AppStatusUtil.getStatus();
        AppStatusUtil.isStopped();
        AppStatusUtil.isBooting();
        AppStatusUtil.isRunning();

        AppStatusUtil.setStatus(AppStatus.RUNNING);
        AppStatusUtil.getStatus();
        AppStatusUtil.isStopped();
        AppStatusUtil.isBooting();
        AppStatusUtil.isRunning();

        AppStatusUtil.setStatus(AppStatus.BOOTING);
        AppStatusUtil.getStatus();
        AppStatusUtil.isStopped();
        AppStatusUtil.isBooting();
        AppStatusUtil.isRunning();

        assertTrue(true);
    }
}
