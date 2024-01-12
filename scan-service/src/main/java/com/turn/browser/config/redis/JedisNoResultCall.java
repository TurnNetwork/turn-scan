//package com.turn.browser.config.redis;
//
//import redis.clients.jedis.JedisCluster;
//
//public abstract class JedisNoResultCall implements JedisCallback<Object>{
//
//	public Object doInRedis(JedisCluster jedisCluster) {
//		action(jedisCluster);
//		return null;
//	}
//
//	abstract void action(JedisCluster jedisCluster);
//
//}
