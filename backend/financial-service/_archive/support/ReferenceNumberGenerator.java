package com.maayn.financialservice.support;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReferenceNumberGenerator {

    public String generate(String prefix) {
        String normalizedPrefix = prefix == null ? "REF" : prefix.trim().toUpperCase();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return normalizedPrefix + "-" + suffix;
    }
}
