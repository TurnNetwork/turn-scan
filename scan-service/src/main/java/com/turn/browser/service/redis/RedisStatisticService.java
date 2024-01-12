package com.turn.browser.service.redis;

import com.turn.browser.dao.entity.NetworkStat;
import org.springframework.stereotype.Service;

@Service
public class RedisStatisticService extends AbstractRedisService<NetworkStat> {
    @Override
    public String getCacheKey() {
        return redisKeyConfig.getNetworkStat();
    }
}
