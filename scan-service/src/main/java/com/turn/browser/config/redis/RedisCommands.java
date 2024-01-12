//package com.turn.browser.config.redis;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;

//public interface RedisCommands {
//
//    /**
//     *
//     * @method set
//     * @param key
//     * @param value
//     * @return
//     */
//    String set(final String key, final String value);
//
//    /**
//     *
//     * @method set
//     * @param key
//     * @param value
//     * @param expire
//     * @return
//     */
//    String set(final String key, final String value, final int expire);
//
//    /**
//     *
//     * @method setNX
//     * @param key
//     * @param value
//     * @return
//     */
//    String setNX(final String key, final String value);
//
//    /**
//     *
//     * @method set
//     * @param key
//     * @param value
//     * @return
//     */
//    String set(final byte[] key, final byte[] value);
//
//    /**
//     *
//     * @method set
//     * @param key
//     * @param value
//     * @param expire
//     * @return
//     */
//    String set(final byte[] key, final byte[] value, final int expire);
//
//    /**
//     *
//     * @method get
//     * @param key
//     * @return
//     */
//    String get(final String key);
//
//    /**
//     *
//     * @method del
//     * @param key
//     * @return
//     */
//    String del(final String key);
//
//    /**
//     *
//     * @method expire
//     * @param key
//     * @param expire
//     * @return
//     */
//    String expire(final String key, final int expire);
//
//    /**
//     *
//     * @method get
//     * @param key
//     * @return
//     */
//    byte[] get(final byte[] key);
//
//    /**
//     *
//     * @method hset
//     * @param key
//     * @param field
//     * @param value
//     * @return
//     */
//    Long hset(final byte[] key, final byte[] field, final byte[] value);
//
//    /**
//     *
//     * @method hmset
//     * @param key
//     * @param hash
//     * @return
//     */
//    String hmset(final byte[] key, final Map<byte[], byte[]> hash);
//
//    /**
//     *
//     * @method hget
//     * @param key
//     * @param field
//     * @param value
//     * @return
//     */
//    byte[] hget(final byte[] key, final byte[] field);
//
//    /**
//     *
//     * @method hmget
//     * @param key
//     * @param fields
//     * @return
//     */
//    List<byte[]> hmget(final byte[] key, final byte[]... fields);
//
//    /**
//     *
//     * @method hdel
//     * @param key
//     * @param field
//     * @return
//     */
//    Long hdel(final byte[] key, final byte[]... field);
//
//    Map<byte[], byte[]> hlistAll(final byte[] key);
//
//    long rpush(final byte[] key, final byte[]... strings);
//
//    long lpush(final byte[] key, final byte[]... strings);
//
//    byte[] lpop(final byte[] key);
//
//    /**
//     *
//     * @method zrange
//     * @param key
//     * @param start
//     * @param end
//     * @return
//     */
//    Set<String> zrange(String key, Long start, Long end);
//
//    /**
//     *
//     * @method zrevrange
//     * @param key
//     * @param start
//     * @param end
//     * @return
//     */
//    Set<String> zrevrange(String key, Long start, Long end);
//
//    /**
//     *
//     * @method zsize
//     * @param key
//     * @return
//     */
//    long zsize(String key);
//
//    String setnx(final String key, final String value, final long expire);
//}
