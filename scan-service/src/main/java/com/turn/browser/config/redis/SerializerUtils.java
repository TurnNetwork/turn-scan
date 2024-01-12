//package com.turn.browser.config.redis;
//
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.*;
//
//@Slf4j
//public class SerializerUtils {
//    private SerializerUtils(){}
//
//    private static final JedisSerializer<Object> REDIS_SERIALIZER = new JdkSerializer<>();
//
//
//	public static JedisSerializer<Object> getRedisSerializer() {
//        return REDIS_SERIALIZER;
//    }
//
//    public static byte[] rawKey(Object key) {
//        if (key instanceof byte[]) {
//            return (byte[]) key;
//    public static byte[] rawValue(Object value) {
//        if (value instanceof byte[]) {
//            return (byte[]) value;
//        }
//        return REDIS_SERIALIZER.serialize(value);
//    }
//
//    public static byte[][] rawValues(Object... values) {
//        final byte[][] rawValues = new byte[values.length][];
//        int i = 0;
//        for (Object value : values) {
//            rawValues[i++] = rawValue(value);
//        }
//        return rawValues;
//    }
//
//
//    @SuppressWarnings({"unchecked"})
//    public static <T extends Collection<?>> T deserializeValues(Collection<byte[]> rawValues, Class<T> type,
//                                                                JedisSerializer<?> redisSerializer) {
//
//        if (redisSerializer == null) {
//            return (T) rawValues;
//        }
//        // connection in pipeline/multi mode
//        if (rawValues == null) {
//            return null;
//        }
//
//        Collection<Object> values = (List.class.isAssignableFrom(type) ? new ArrayList<>(rawValues.size())
//                : new LinkedHashSet<>(rawValues.size()));
//        for (byte[] bs : rawValues) {
//            values.add(redisSerializer.deserialize(bs));
//        }
//
//        return (T) values;
//    }
//
//    public static <K> byte[] rawHashKey(K hashKey) {
//        if (hashKey instanceof byte[]) {
//            return (byte[]) hashKey;
//        }
//        return REDIS_SERIALIZER.serialize(hashKey);
//    }
//
//    public static <V> byte[] rawHashValue(V value) {
//        if (value instanceof byte[]) {
//            return (byte[]) value;
//        }
//        return REDIS_SERIALIZER.serialize(value);
//    }
//
//    @SuppressWarnings("unchecked")
//	public static <K> byte[][] rawHashKeys(K... hashKeys) {
//        final byte[][] rawHashKeys = new byte[hashKeys.length][];
//        int i = 0;
//        for (K hashKey : hashKeys) {
//            rawHashKeys[i++] = rawHashKey(hashKey);
//        }
//        return rawHashKeys;
//    }
//
//    public static Object deserializeStr(byte[] byt) {
//        return REDIS_SERIALIZER.deserialize(byt);
//    }
//
//    /**
//     *
//     * @param byteMap
//     * @return
//     */
//    public static Map<String, Object> deserializerMapObj(Map<byte[], byte[]> byteMap) {
//        try {
//            Map<String, Object> result = new HashMap<>();
//            for (Map.Entry<?, ?> tmpMap : byteMap.entrySet()) {
//                byte[] byteKey = (byte[]) tmpMap.getKey();
//                byte[] byteValue = (byte[]) tmpMap.getValue();
//                result.put((String) deserializeStr(byteKey), deserializeStr(byteValue));
//            }
//            return result;
//        } catch (Exception e) {
//            log.error("",e);
//        }
//        return null;
//    }
//}
