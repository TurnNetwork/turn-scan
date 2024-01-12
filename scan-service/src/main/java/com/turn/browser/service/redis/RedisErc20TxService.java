package com.turn.browser.service.redis;

import com.alibaba.fastjson.JSON;
import com.turn.browser.elasticsearch.dto.ErcTx;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * ERC20 token transaction cache data
 */
@Service
public class RedisErc20TxService extends AbstractRedisService<ErcTx> {
    @Override
    public String getCacheKey() {
        return redisKeyConfig.getErc20Tx();
    }

    @Override
    public void updateMinMaxScore(Set<ErcTx> data) {
        minMax.reset();
        data.forEach(item -> {
            Long score = item.getSeq();
            if (score < minMax.getMinOffset()) minMax.setMinOffset(score);
            if (score > minMax.getMaxOffset()) minMax.setMaxOffset(score);
        });
    }

    @Override
    public void updateExistScore(Set<String> exist) {
        Objects.requireNonNull(exist).forEach(item -> existScore.add(JSON.parseObject(item, ErcTx.class).getSeq()));
    }

    @Override
    public void updateStageSet(Set<ErcTx> data) {
        data.forEach(item -> {
            // Only put into the cache if they do not exist in the cache
            if (!existScore.contains(item.getSeq()))
                stageSet.add(new DefaultTypedTuple<>(JSON.toJSONString(item), item.getSeq().doubleValue()));
        });
    }
}