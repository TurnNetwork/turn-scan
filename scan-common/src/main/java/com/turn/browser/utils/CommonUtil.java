package com.turn.browser.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.turn.browser.bean.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class CommonUtil {

    /**
     * Support lambda's chain empty judgment
     * Usage: ofNullable(() -> obj.getObj1().getObj2().getObj3()).ifPresent(res -> System.out.println(res));
     * Explanation: It will automatically determine whether obj, getObj1(), getObj2(), and getObj3() are empty. If the value of getObj3() is not empty, it will be printed. For example, if getObj2() is empty, the result is null instead of a null pointer.
     *
     * @param resolver
     * @return java.util.Optional<T>
     */
    public static <T> Optional<T> ofNullable(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    /**
     * create trace-id
     *
     * @param
     * @return java.lang.String
     */
    public static String createTraceId() {
        return StrUtil.removeAll(UUID.randomUUID().toString(), "-");
    }

    /**
     * Add global link ID--default generated link ID
     *
     * @param
     * @return void
     */
    public static void putTraceId() {
        try {
            MDC.put(CommonConstant.TRACE_ID, createTraceId());
        } catch (Exception e) {
            log.error("Add link ID exception", e);
        }
    }

    /**
     * Add global link ID--default generated link ID
     *
     * @param customKey custom key
     * @return void
     */
    public static void putCustomTraceId(String customKey) {
        try {
            MDC.put(customKey, createTraceId());
        } catch (Exception e) {
            log.error("Add link ID exception", e);
        }
    }

    /**
     * Add global link ID--default generated link ID
     *
     * @param traceId
     * @return void
     */
    public static void putTraceId(String traceId) {
        try {
            if (StrUtil.isNotBlank(traceId)) {
                MDC.put(CommonConstant.TRACE_ID, traceId);
            } else {
                log.error("Please enter link ID");
            }
        } catch (Exception e) {
            log.error("Add link ID exception", e);
        }
    }

    /**
     * Get global link ID
     *
     * @param
     * @return java.lang.String
     */
    public static String getTraceId() {
        String traceId = "";
        try {
            traceId = MDC.get(CommonConstant.TRACE_ID);
        } catch (Exception e) {
            log.error("Exception in getting global link ID", e);
        }
        return traceId;
    }

    /**
     * Delete global link ID
     *
     * @param
     * @return void
     */
    public static void removeTraceId() {
        try {
            MDC.remove(CommonConstant.TRACE_ID);
        } catch (Exception e) {
            log.error("Delete link ID exception", e);
        }
    }

    /**
     * Delete global link ID
     *
     * @param customKey Custom key
     * @return void
     */
    public static void removeCustomTraceId(String customKey) {
        try {
            MDC.remove(customKey);
        } catch (Exception e) {
            log.error("Delete link ID exception", e);
        }
    }

}
