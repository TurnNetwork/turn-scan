package com.turn.browser.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.turn.browser.v0152.bean.BubbleDetailDto;
import org.springframework.stereotype.Service;


@Service
public class BubbleCacheService extends CacheBase {

	public BubbleDetailDto getBubbleCache(Long bubbleId) {
		Object addrGameCache = this.redisTemplate.opsForHash().get(redisKeyConfig.getBubbleInfo(), bubbleId);
		if(ObjectUtil.isNotNull(addrGameCache)){
			return JSONObject.parseObject((String) addrGameCache,BubbleDetailDto.class);
		}
		return null;

	}

	/**
	 * Add bubble cache
	 * @param bubbleDetailDto
	 */
	public void addBubbleCache(BubbleDetailDto bubbleDetailDto, Long bubbleId) {

		redisTemplate.opsForHash().put(redisKeyConfig.getBubbleInfo(), bubbleId,JSON.toJSONString(bubbleDetailDto));
	}

	/**
	 * bubble release clear cache
	 *
	 * @param bubbleId
	 */
	public void delBubbleCache(Long bubbleId) {
		redisTemplate.opsForHash().delete(redisKeyConfig.getBubbleInfo(), bubbleId);
	}
}
