package com.turn.browser.aop;

import com.turn.browser.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Scheduled task aspect---Add link id
 */
@Slf4j
@Component
@Aspect
public class TaskAspect {

    /**
     * Task classes that need to print information---some scheduled tasks execute too fast and are not suitable for outputting logs. You can add them yourself if necessary.
     */
    private static final Set<String> taskLog = new HashSet<String>() {{
        add("com.turn.browser.task.NetworkStatUpdateTask");
        add("com.turn.browser.task.AddressUpdateTask");
        add("com.turn.browser.task.ErcTokenUpdateTask");
        add("com.turn.browser.task.NodeOptTask");
        add("com.turn.browser.task.NodeUpdateTask");
        add("com.turn.browser.task.UpdateTokenQtyTask");
    }};

    @Pointcut(value = "@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public void access() {

    }

    @Before("access()")
    public void doBefore(JoinPoint joinPoint) {
        CommonUtil.putTraceId();
        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        if (taskLog.contains(className)) {
            log.info("Scheduled task: class name [{}]---method [{}] starts...", className, methodName);
        }
    }

    @After("access()")
    public void after(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        if (taskLog.contains(className)) {
            log.info("Scheduled task: class name [{}]---method [{}] ended...", className, methodName);
        }
        CommonUtil.removeTraceId();
    }

}