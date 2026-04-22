package com.maayn.notificationservice.support;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Reads {@code X-User-Roles} injected by the API gateway from the verified JWT
 * (comma-separated role names). Never trust a raw client header — the gateway
 * strips spoofed values before setting these.
 */
public final class WorkflowCallerRoles {

    private WorkflowCallerRoles() {}

    public static Set<String> fromCurrentRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (!(ra instanceof ServletRequestAttributes sra)) {
            return Set.of();
        }
        HttpServletRequest req = sra.getRequest();
        String raw = req.getHeader("X-User-Roles");
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Whether the caller may act on an approval task that requires {@code requiredRole}
     * (e.g. RISK, MANAGER, DIRECTOR). {@code ADMIN} and {@code SUPERADMIN} may act on any step.
     */
    public static boolean mayActOnStep(String requiredRole, Set<String> callerRoles) {
        if (callerRoles == null || callerRoles.isEmpty()) {
            return false;
        }
        if (callerRoles.contains("ADMIN") || callerRoles.contains("SUPERADMIN")) {
            return true;
        }
        if (requiredRole == null || requiredRole.isBlank()) {
            return false;
        }
        return callerRoles.contains(requiredRole.trim().toUpperCase(Locale.ROOT));
    }
}
