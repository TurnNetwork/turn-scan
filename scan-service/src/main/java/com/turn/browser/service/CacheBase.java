package com.turn.browser.service;

import com.turn.browser.config.RedisKeyConfig;
import com.turn.browser.enums.I18nEnum;
import com.turn.browser.response.RespPage;
import com.turn.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Set;

/**
 * Basic encapsulation cache acquisition logic
 * @Description:
 */
@Slf4j
public class CacheBase {
    @Resource
    protected RedisKeyConfig redisKeyConfig;
    @Resource
    protected RedisTemplate<String,String> redisTemplate;
    @Resource
    protected I18nUtil i18n;

    private final Logger logger = LoggerFactory.getLogger(CacheBase.class);


    protected boolean validateParam(Collection<?> items){
        if(items.isEmpty()){
            // No updates
            logger.debug("Empty Content");
            return false;
        }
        return true;
    }

    protected static class CachePageInfo<T>{
        Set<String> data;
        RespPage<T> page;
    }
    
    protected <T> CachePageInfo <T> getCachePageInfo(String cacheKey,int pageNum,int pageSize){
        RespPage<T> page = new RespPage<>();
        page.setErrMsg(i18n.i(I18nEnum.SUCCESS));
        CachePageInfo<T> cpi = new CachePageInfo<>();
        Long pagingTotalCount = redisTemplate.opsForZSet().size(cacheKey);
        if(pagingTotalCount==null) pagingTotalCount=0L;
        if(pagingTotalCount>redisKeyConfig.getMaxItem()){
            // If the cache quantity is greater than maxItemNum, use maxItemNum as the paging quantity
            pagingTotalCount = redisKeyConfig.getMaxItem();
        }
        page.setTotalCount(pagingTotalCount);

        long pageCount = pagingTotalCount/pageSize;
        if(pagingTotalCount%pageSize!=0){
            pageCount+=1;
        }
        page.setTotalPages(pageCount);

        // Redis cache paging starts from index 0
        if(pageNum<=0){
            pageNum=1;
        }
        if(pageSize<=0){
            pageSize=1;
        }
        long start = (pageNum-1L)*pageSize;
        long end = (pageNum*pageSize)-1L;
        cpi.data = redisTemplate.opsForZSet().reverseRange(cacheKey,start,end);
// cpi.data = jedisClient.zrevrange(cacheKey, start, end);
        cpi.page = page;
        return cpi;
    }

    protected <T> CachePageInfo <T> getCachePageInfoByStartEnd(String cacheKey,long start,long end,RedisTemplate<String,String> redisTemplate, long maxItemNum){
        RespPage<T> page = new RespPage<>();
        page.setErrMsg(i18n.i(I18nEnum.SUCCESS));

        CachePageInfo<T> cpi = new CachePageInfo<>();
        Long pagingTotalCount = redisTemplate.opsForZSet().size(cacheKey);
        if(pagingTotalCount!=null&&pagingTotalCount>maxItemNum){
            // If the cache quantity is greater than maxItemNum, use maxItemNum as the paging quantity
            pagingTotalCount = maxItemNum;
        }
        page.setTotalCount(pagingTotalCount==null?0L:pagingTotalCount);

        cpi.data = redisTemplate.opsForZSet().reverseRange(cacheKey, start, end);
        cpi.page = page;
        return cpi;
    }

    protected <T> CachePageInfo <T> getCachePageInfoByStartEnd(String cacheKey,long start,long end){
        RespPage<T> page = new RespPage<>();
        page.setErrMsg(i18n.i(I18nEnum.SUCCESS));

        CachePageInfo<T> cpi = new CachePageInfo<>();
//        cpi.data = jedisClient.zrevrange(cacheKey, start, end);
        cpi.data = redisTemplate.opsForZSet().reverseRange(cacheKey,start,end);
        cpi.page = page;
        return cpi;
    }
}
