package com.turn.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.turn.browser.dao.entity.AddrGame;
import com.turn.browser.v0152.bean.AddrGameDetailDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class AddrGameCacheService extends CacheBase {

	public List<AddrGameDetailDto> getAddrGameCache(String address) {
		Object addrGameCache = this.redisTemplate.opsForHash().get(redisKeyConfig.getAddrGames(), address);
		List<AddrGameDetailDto> addrGameList = new LinkedList<>();
		if(ObjectUtil.isNotNull(addrGameCache)){
			CopyOnWriteArrayList<JSONObject> addrGames = JSONObject.parseObject((String) addrGameCache,CopyOnWriteArrayList.class);
			addrGames.forEach(item->{
				addrGameList.add(JSONObject.toJavaObject(item,AddrGameDetailDto.class));
			});
		}
		return addrGameList;
	}

	/**
	 * Join the game and add cache
	 * @param addrGameDetailDto
	 */
	public void addAddrGameCache(AddrGameDetailDto addrGameDetailDto) {
		Object addrGameCache = redisTemplate.opsForHash().get(redisKeyConfig.getAddrGames(), addrGameDetailDto.getAddress());

		if(ObjectUtil.isNotNull(addrGameCache)){
			List addrGameList = JSONObject.parseObject((String) addrGameCache,List.class);
			addrGameList.add(addrGameDetailDto);
			redisTemplate.opsForHash().put(redisKeyConfig.getAddrGames(), addrGameDetailDto.getAddress(),JSON.toJSONString(addrGameList));
		}else {
			List<AddrGameDetailDto> addrGameList = new ArrayList<>();
			addrGameList.add(addrGameDetailDto);
			redisTemplate.opsForHash().put(redisKeyConfig.getAddrGames(), addrGameDetailDto.getAddress(),JSON.toJSONString(addrGameList));
		}
	}

	/**
	 * End game and exit cache
	 *
	 * @param addrGameList
	 */
	public void delAddrGameCache(List<AddrGame> addrGameList) {
		if(CollUtil.isNotEmpty(addrGameList)){
			addrGameList.forEach(addrGame->{
				Object addrGameCache = redisTemplate.opsForHash().get(redisKeyConfig.getAddrGames(), addrGame.getAddress());

				if(ObjectUtil.isNotNull(addrGameCache)){
					CopyOnWriteArrayList<JSONObject> addrGames = JSONObject.parseObject((String) addrGameCache,CopyOnWriteArrayList.class);
					addrGames.stream().forEach(item -> {
						AddrGameDetailDto x = JSONObject.toJavaObject(item, AddrGameDetailDto.class);
						if(x.getGameContractAddress().equals(addrGame.getGameContractAddress()) && x.getRoundId().equals(addrGame.getRoundId())){
							addrGames.remove(item);
						}
					});
					if(CollUtil.isNotEmpty(addrGames)){
						redisTemplate.opsForHash().put(redisKeyConfig.getAddrGames(), addrGame.getAddress(),addrGames);
					}else {
						redisTemplate.opsForHash().delete(redisKeyConfig.getAddrGames(), addrGame.getAddress());
					}
				}
			});
		}

	}
}
