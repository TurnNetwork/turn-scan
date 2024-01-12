package com.turn.browser.service;

import com.turn.browser.ApiTestBase;
import com.turn.browser.cache.TransactionCacheDto;
import com.turn.browser.config.RedisKeyConfig;
import com.turn.browser.dao.entity.NetworkStat;
import com.turn.browser.elasticsearch.dto.Block;
import com.turn.browser.utils.I18nUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StatisticCacheServiceTest extends ApiTestBase {

    @InjectMocks
    private StatisticCacheService statisticCacheService;

    @Mock
    protected RedisKeyConfig redisKeyConfig;

    @Mock
    protected I18nUtil i18n;

    @Mock
    protected RedisTemplate<String, String> redisTemplate;

    @Test
    public void testGetBlockCache() {
        ZSetOperations valueOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(valueOperations);
        List<Block> blocks = statisticCacheService.getBlockCache(0, 10);
        assertNotNull(blocks);
    }

    @Test
    public void testGetNetworkCache() {
        ValueOperations valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        NetworkStat networkstat = statisticCacheService.getNetworkStatCache();
        assertNotNull(networkstat);
    }

    @Test
    public void testGetTransactionCache() {
        ZSetOperations valueOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(valueOperations);
        TransactionCacheDto transactionCacheDto = statisticCacheService.getTransactionCache(0, 10);
        assertNotNull(transactionCacheDto);
    }

    @Test
    public void testGetBlockCacheByStartEnd() {
        ZSetOperations valueOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(valueOperations);
        List<Block> blocks = statisticCacheService.getBlockCacheByStartEnd(1l, 10l);
        assertNotNull(blocks);
    }

}


