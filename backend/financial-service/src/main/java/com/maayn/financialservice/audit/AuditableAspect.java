package com.maayn.financialservice.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditableAspect {

    private final AuditPublisher auditPublisher;

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        String label = auditable.value().isBlank()
                ? pjp.getSignature().toShortString()
                : auditable.value();
        try {
            Object result = pjp.proceed();
            auditPublisher.publishSuccess(auditable.action(), null, label + " succeeded");
            return result;
        } catch (Throwable ex) {
            auditPublisher.publishFailure(auditable.action(), null,
                    label + " failed: " + ex.getMessage());
            throw ex;
        }
    }
}

