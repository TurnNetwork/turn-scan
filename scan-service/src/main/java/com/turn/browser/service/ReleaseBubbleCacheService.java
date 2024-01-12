package com.turn.browser.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ReleaseBubbleCacheService extends CacheBase {

	/**
	 * Get cache information released by bubble
	 * @param releaseBubbleBlockNumber
	 * @return
	 */
	public List<Long>  getBubbleReleaseCache(Long releaseBubbleBlockNumber) {
		Object bubbleIdListStr = this.redisTemplate.opsForHash().get(redisKeyConfig.getBubbleRelease(), releaseBubbleBlockNumber);
		List<Long> bubbleIdList = new LinkedList<>();
		if(ObjectUtil.isNotNull(bubbleIdListStr)){
			CopyOnWriteArrayList bubbleIdLists = JSONObject.parseObject((String) bubbleIdListStr,CopyOnWriteArrayList.class);
			bubbleIdLists.forEach(item-> bubbleIdList.add(Long.parseLong(String.valueOf(item))));
		}
		return bubbleIdList;
	}

	/**
	 * Add bubble release cache
	 */
	public void addReleaseBubbleCache(Long releaseBubbleBlockNumber, List<Long> bubbleIdList) {
		redisTemplate.opsForHash().put(redisKeyConfig.getBubbleRelease(), releaseBubbleBlockNumber, JSONObject.toJSONString(bubbleIdList));
	}

	/**
	 * Release clear cache
	 * @param releaseBubbleBlockNumber
	 */
	public void delReleaseBubbleCache(Long releaseBubbleBlockNumber) {
		redisTemplate.opsForHash().delete(redisKeyConfig.getBubbleRelease(), releaseBubbleBlockNumber);
	}

	/**
	 * 获取bubble待释放的缓存信息
	 * @param releaseBubbleBlockNumber
	 * @return
	 */
	public List<Long>  getBubblePreReleaseCache(Long releaseBubbleBlockNumber) {
		Object bubbleIdListStr = this.redisTemplate.opsForHash().get(redisKeyConfig.getBubblePreRelease(), releaseBubbleBlockNumber);
		List<Long> bubbleIdList = new LinkedList<>();
		if(ObjectUtil.isNotNull(bubbleIdListStr)){
			CopyOnWriteArrayList bubbleIdLists = JSONObject.parseObject((String) bubbleIdListStr,CopyOnWriteArrayList.class);
			bubbleIdLists.forEach(item-> bubbleIdList.add(Long.parseLong(String.valueOf(item))));
		}
		return bubbleIdList;
	}

	/**
	 * 添加bubble释放缓存
	 */
	public void addPreReleaseBubbleCache(Long releaseBubbleBlockNumber, List<Long> bubbleIdList) {
		redisTemplate.opsForHash().put(redisKeyConfig.getBubblePreRelease(), releaseBubbleBlockNumber, JSONObject.toJSONString(bubbleIdList));
	}

	/**
	 * 释放清除缓存
	 * @param releaseBubbleBlockNumber
	 */
	public void delPreReleaseBubbleCache(Long releaseBubbleBlockNumber) {
		redisTemplate.opsForHash().delete(redisKeyConfig.getBubblePreRelease(), releaseBubbleBlockNumber);
	}
}