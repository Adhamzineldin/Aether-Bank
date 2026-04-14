package com.maayn.iamservice.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Intercept all service methods
    @Around("execution(* com.maayn.iamservice.service.impl..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();

        logger.info("➡ Entering method: {}", methodName);

        Object result;

        try {
            result = joinPoint.proceed();
            logger.info("✔ Method success: {}", methodName);
        } catch (Throwable ex) {
            logger.error("Exception in method: {} | {}", methodName, ex.getMessage());
            throw ex;
        }

        logger.info("Exiting method: {}", methodName);

        return result;
    }
}