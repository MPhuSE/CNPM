package com.flowops.shared.infrastructure;

import io.jsonwebtoken.Claims;
import com.flowops.identity.application.SessionAuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, SessionAuthenticationService sessionAuthenticationService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionAuthenticationService = sessionAuthenticationService;
    }


    private final JwtTokenProvider jwtTokenProvider;
    private final SessionAuthenticationService sessionAuthenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                Long userId = Long.valueOf(claims.getSubject());
                java.util.UUID sid = java.util.UUID.fromString(claims.get("sid", String.class));
                Object versionClaim = claims.get("authVersion");
                if (!(versionClaim instanceof Number number)) throw new IllegalArgumentException("Invalid auth version");
                SessionAuthenticationService.Authenticated current = sessionAuthenticationService.authenticate(userId, sid, number.longValue());
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(current.principal(), null, current.authorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
