package com.flowops.shared.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthOriginCsrfFilter extends OncePerRequestFilter {
    private final Set<String> allowedOrigins;
    private final SecurityErrorWriter errors;

    public AuthOriginCsrfFilter(@Value("${flowops.cors.allowed-origins}") String origins, SecurityErrorWriter errors) {
        this.allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).collect(Collectors.toUnmodifiableSet());
        if (allowedOrigins.isEmpty() || allowedOrigins.stream().anyMatch(s -> s.isEmpty() || s.equals("*") || s.equals("null")))
            throw new IllegalArgumentException("Explicit CORS origins required");
        this.errors = errors;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !"POST".equals(request.getMethod()) || !Set.of("/api/v1/auth/login", "/api/v1/auth/refresh",
                "/api/v1/auth/logout", "/api/v1/auth/password").contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        List<String> origins = Collections.list(request.getHeaders("Origin"));
        if (origins.size() != 1 || !allowedOrigins.contains(origins.get(0))) {
            errors.write(response, 403, "ACCESS_DENIED", "Origin không được phép");
            return;
        }
        List<String> csrfHeaders = Collections.list(request.getHeaders("X-FlowOps-CSRF"));
        if (csrfHeaders.size() != 1 || !"1".equals(csrfHeaders.get(0))) {
            errors.write(response, 403, "ACCESS_DENIED", "Thiếu hoặc sai CSRF header");
            return;
        }
        chain.doFilter(request, response);
    }
}
