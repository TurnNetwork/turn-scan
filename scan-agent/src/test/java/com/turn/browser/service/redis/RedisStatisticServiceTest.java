package com.turn.browser.service.redis;

import com.alibaba.fastjson.JSON;
import com.turn.browser.AgentTestBase;
import com.turn.browser.bean.CollectionNetworkStat;
import com.turn.browser.config.RedisKeyConfig;
import com.turn.browser.dao.entity.NetworkStat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RedisStatisticServiceTest extends AgentTestBase {

    @Mock
    protected RedisTemplate<String, String> redisTemplate;

    @Mock
    private RedisKeyConfig redisKeyConfig;

    @InjectMocks
    @Spy
    private RedisStatisticService target;

    @Before
    public void setup() {
        //ReflectionTestUtils.setField(target, "maxItemCount", 500000);
        ValueOperations vo = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(vo);
        ZSetOperations zo = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zo);
        when(zo.size(any())).thenReturn(100L);
        Set<String> data = new HashSet<>();
        data.add(JSON.toJSONString(blockList.get(0)));
    }

    /**
     * 根据区块号获取激励池余额
     */
    @Test
    public void test() {
        NetworkStat networkStat = CollectionNetworkStat.newInstance();
        Set<NetworkStat> data = new HashSet<>();
        data.add(networkStat);
        target.save(data, true);
    }

}
