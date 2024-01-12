package com.turn.browser.service.redis;

import com.alibaba.fastjson.JSON;
import com.turn.browser.config.RedisKeyConfig;
import com.turn.browser.dao.entity.NetworkStat;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description: Redis service
 */
@Slf4j
public abstract class AbstractRedisService<T> {
    @Resource
    protected RedisKeyConfig redisKeyConfig;
    @Resource
    protected RedisTemplate<String,String> redisTemplate;
    MinMaxScore minMax= MinMaxScore.builder().build();
    // List of tuples to be stored
    Set<ZSetOperations.TypedTuple<String>> stageSet = new HashSet<>();
    // The record score that already exists in redis in the parameter list of this operation
    Set<Long> existScore = new HashSet<>();

    @Data
    @Builder
    @Accessors(chain = true)
    public static class MinMaxScore{
        private Long minOffset;
        private Long maxOffset;
        MinMaxScore reset(){
            minOffset=Long.MAX_VALUE;
            maxOffset=Long.MIN_VALUE;
            return this;
        }
    }

    /**
     * Clear block cache
     */
    public void clear() {
        redisTemplate.delete(getCacheKey());
    }
    public abstract String getCacheKey();
    public void updateMinMaxScore(Set<T> data){}
    public void updateExistScore(Set<String> exist){}
    public void updateStageSet(Set<T> data){}

    /**
     * Template method, the general process operates here, specific attributes are handled by subclasses
     * @param data The data set that needs to be stored in redis
     * @param serialOverride needs to execute serial override one by one, such as updating statistical records. The default is batch processing.
     */
    public void save(Set<T> data, boolean serialOverride) {
        if(data.isEmpty()) return;
        long startTime = System.currentTimeMillis();

        if(serialOverride) {
            data.forEach(item -> {
                String json = JSON.toJSONString(item);
                redisTemplate.opsForValue().set(getCacheKey(), json);
            });
        }else{
            // Get the total number of records in the cache
            Long cacheItemCount = redisTemplate.opsForZSet().size(getCacheKey());
            //Update MinMax maximum score and minimum score
            updateMinMaxScore(data);
            // Check if there is a value in the cache
            existScore.clear();
            Set<String> exist = redisTemplate.opsForZSet().rangeByScore(getCacheKey(),minMax.getMinOffset(),minMax.getMaxOffset());
            updateExistScore(exist);
            //First clear the waiting list
            stageSet.clear();
            //Update the list to be stored
            updateStageSet(data);
            //Execute warehousing operation
            if(!stageSet.isEmpty()) redisTemplate.opsForZSet().add(getCacheKey(), stageSet);
            if(cacheItemCount!=null&&cacheItemCount>redisKeyConfig.getMaxItem()){
                // If the number of updated cache items is greater than the specified number, the oldest (cacheItemCount-maxItemCount) items need to be deleted.
                redisTemplate.opsForZSet().removeRange(getCacheKey(),0,cacheItemCount-redisKeyConfig.getMaxItem());
            }
        }
        log.debug("Processing time: {} ms",System.currentTimeMillis()-startTime);
    }

    public Long size(String key){
        boolean hasKey = redisTemplate.hasKey(key);
        if(hasKey){
            return redisTemplate.opsForZSet().size(key);
        }
        return 0L;
    }
}
