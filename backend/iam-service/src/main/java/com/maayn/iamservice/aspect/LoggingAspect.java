package com.maayn.iamservice.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logging Aspect
 * AOP interceptor for logging service method execution
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Intercept all service method calls
     */
    @Around("execution(* com.maayn.iamservice.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        logger.debug("➡ Entering method: {}", methodName);

        Object result;

        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("✔ Method completed: {} ({}ms)", methodName, duration);
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("✗ Exception in method: {} ({}ms) | {}", methodName, duration, ex.getMessage());
            throw ex;
        }

        return result;
    }
}