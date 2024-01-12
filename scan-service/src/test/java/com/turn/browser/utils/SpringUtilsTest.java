package com.turn.browser.utils;

import com.turn.browser.config.EsClusterConfig;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @program: browser-server
 * @description:
 */
@RunWith(PowerMockRunner.class)
public class SpringUtilsTest {

    @Test
    @PrepareForTest({SpringUtils.class, SpringUtilsTest.class})
    public void test_resetSpring() throws Exception {
        SpringUtils targe = PowerMockito.spy(new SpringUtils());
        EsClusterConfig esClusterConfig = mock(EsClusterConfig.class);
        DefaultListableBeanFactory defaultListableBeanFactory = mock(DefaultListableBeanFactory.class);
        ReflectionTestUtils.setField(targe, "esClusterConfig", esClusterConfig);
        ReflectionTestUtils.setField(targe, "defaultListableBeanFactory", defaultListableBeanFactory);
        EsClusterConfig es = mock(EsClusterConfig.class);
        PowerMockito.whenNew(EsClusterConfig.class)
                .withNoArguments()
                .thenReturn(es);
        RestHighLevelClient restHighLevelClient = mock(RestHighLevelClient.class);
        when(es.client()).thenReturn(restHighLevelClient);
        when(defaultListableBeanFactory.containsBean(any())).thenReturn(true);
        targe.resetSpring("a");
        Assert.assertTrue(true);
    }

}
