package com.maayn.accountservice.aspect;

import java.util.Arrays;

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

    // Matches the implementation, not the interface, to capture the actual execution logic
    @Around("execution(* com.example.account.service.AccountProfileServiceImpl.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.info("AUDIT - Banking Operation: [{}] started with params: {}", methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();

        try {
            Object proceed = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            logger.info("AUDIT - Banking Operation: [{}] completed in {}ms", methodName, executionTime);
            return proceed;
        } catch (Exception e) {
            logger.error("AUDIT - SECURITY ALERT: [{}] failed. Error: {}", methodName, e.getMessage());
            throw e;
        }
    }
}