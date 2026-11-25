package com.flowops.identity.application;

import com.flowops.identity.api.dto.LoginRequest;
import com.flowops.identity.api.dto.LoginResponse;
import com.flowops.identity.infrastructure.persistence.*;
import com.flowops.shared.domain.EntityNotFoundException;
import com.flowops.shared.infrastructure.JwtTokenProvider;
import com.flowops.shared.infrastructure.SessionPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {
    private static final String INVALID_LOGIN = "Tên đăng nhập hoặc mật khẩu không chính xác";
    private static final String INVALID_SESSION = "Phiên không hợp lệ hoặc đã hết hạn";
    private final SpringDataUserRepository users;
    private final SpringDataUserSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;
    private final LoginRateLimiter limiter;
    private final SecureRandom random = new SecureRandom();
    private final String dummyHash;
    private final long sessionTtlMs;

    public AuthService(SpringDataUserRepository users, SpringDataUserSessionRepository sessions,
                       PasswordEncoder passwordEncoder, JwtTokenProvider jwt, LoginRateLimiter limiter,
                       @Value("${flowops.jwt.refresh-token-expiration-ms:604800000}") long sessionTtlMs) {
        if (sessionTtlMs <= 0) throw new IllegalArgumentException("Session TTL must be positive");
        this.users = users;
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.limiter = limiter;
        this.sessionTtlMs = sessionTtlMs;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public record AuthResult(String accessToken, LoginResponse.UserDto user, String refreshCookie, Instant expiresAt) {
        @Override public String toString() { return "AuthResult[REDACTED]"; }
    }
    private record RefreshParts(UUID sid, String secret) {
        @Override public String toString() { return "RefreshParts[REDACTED]"; }
    }

    @Transactional
    public AuthResult login(LoginRequest request, String remoteIp) {
        String username = request.getUsername();
        try (LoginRateLimiter.Admission admission = limiter.reserve(username, remoteIp)) {
            String password = request.getPassword();
            if (!validBcryptInput(password)) {
                passwordEncoder.matches("invalid", dummyHash);
                admission.failed();
                throw new BadCredentialsException(INVALID_LOGIN);
            }
            Long userId = users.findIdByUsername(username).orElse(null);
            UserJpaEntity user = userId == null ? null : users.findByIdForUpdate(userId).orElse(null);
            if (user == null || !username.equals(user.getUsername()) || !Boolean.TRUE.equals(user.getIsActive())) {
                passwordEncoder.matches(password, dummyHash);
                admission.failed();
                throw new BadCredentialsException(INVALID_LOGIN);
            }
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                admission.failed();
                throw new BadCredentialsException(INVALID_LOGIN);
            }

            Instant now = Instant.now();
            UserSessionJpaEntity session = new UserSessionJpaEntity();
            session.setId(UUID.randomUUID());
            session.setUser(user);
            session.setAuthVersion(user.getAuthVersion());
            session.setCreatedAt(now);
            session.setExpiresAt(now.plusMillis(sessionTtlMs));
            String secret = newSecret();
            session.setTokenHash(hash(secret));
            sessions.save(session);
            AuthResult result = result(user, session, secret, true);
            admission.succeeded();
            return result;
        }
    }

    @Transactional
    public AuthResult refresh(String cookie) {
        RefreshParts parts = parseCookie(cookie);
        Long userId = sessions.findUserIdBySessionId(parts.sid()).orElseThrow(AuthService::badSession);
        UserJpaEntity user = users.findByIdForUpdate(userId).orElseThrow(AuthService::badSession);
        UserSessionJpaEntity session = sessions.findByIdForUpdate(parts.sid()).orElseThrow(AuthService::badSession);
        if (!session.getUser().getId().equals(user.getId()) || !valid(user, session)
                || !MessageDigest.isEqual(HexFormat.of().parseHex(session.getTokenHash()),
                                          HexFormat.of().parseHex(hash(parts.secret())))) throw badSession();
        String nextSecret = newSecret();
        session.setTokenHash(hash(nextSecret));
        return result(user, session, nextSecret, false);
    }

    @Transactional
    public void logout(SessionPrincipal principal) {
        UserJpaEntity user = users.findByIdForUpdate(principal.userId()).orElseThrow(AuthService::badSession);
        UserSessionJpaEntity session = sessions.findByIdForUpdate(principal.sid()).orElseThrow(AuthService::badSession);
        if (!session.getUser().getId().equals(user.getId()) || !valid(user, session)
                || !session.getAuthVersion().equals(principal.authVersion())) throw badSession();
        if (session.getRevokedAt() == null) session.setRevokedAt(Instant.now());
    }

    @Transactional
    public void changePassword(SessionPrincipal principal, String currentPassword, String newPassword) {
        UserJpaEntity user = users.findByIdForUpdate(principal.userId()).orElseThrow(AuthService::badSession);
        UserSessionJpaEntity session = sessions.findByIdForUpdate(principal.sid()).orElseThrow(AuthService::badSession);
        if (!session.getUser().getId().equals(user.getId()) || !valid(user, session)
                || !session.getAuthVersion().equals(principal.authVersion())) throw badSession();
        if (!validBcryptInput(currentPassword) || !passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            throw new BadCredentialsException("Mật khẩu hiện tại không chính xác");
        if (!validBcryptInput(newPassword) || newPassword.codePointCount(0, newPassword.length()) < 15)
            throw new PasswordPolicyException();
        String newHash = passwordEncoder.encode(newPassword);
        List<UserSessionJpaEntity> allSessions = sessions.findByUserIdForUpdate(user.getId());
        Instant now = Instant.now();
        user.setPasswordHash(newHash);
        user.setAuthVersion(user.getAuthVersion() + 1);
        for (UserSessionJpaEntity item : allSessions) if (item.getRevokedAt() == null) item.setRevokedAt(now);
    }

    @Transactional(readOnly = true)
    public LoginResponse.UserDto getCurrentUser(Long userId) {
        return toUserDto(users.findById(userId).orElseThrow(() -> new EntityNotFoundException("Người dùng", userId)));
    }

    private AuthResult result(UserJpaEntity user, UserSessionJpaEntity session, String secret, boolean includeUser) {
        String accessToken = jwt.generateAccessToken(user.getId(), session.getId(),
                user.getAuthVersion(), session.getExpiresAt());
        return new AuthResult(accessToken, includeUser ? toUserDto(user) : null,
                session.getId() + "." + secret, session.getExpiresAt());
    }

    private LoginResponse.UserDto toUserDto(UserJpaEntity user) {
        return LoginResponse.UserDto.builder().id(String.valueOf(user.getId())).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName())
                .departmentId(user.getDepartment() == null ? null : String.valueOf(user.getDepartment().getId()))
                .departmentName(user.getDepartment() == null ? null : user.getDepartment().getName())
                .roles(user.getRoles().stream().map(RoleJpaEntity::getCode).toList()).build();
    }

    private boolean valid(UserJpaEntity user, UserSessionJpaEntity session) {
        return Boolean.TRUE.equals(user.getIsActive()) && session.getRevokedAt() == null
                && Instant.now().isBefore(session.getExpiresAt())
                && user.getAuthVersion().equals(session.getAuthVersion());
    }

    private String newSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean validBcryptInput(String password) {
        return password != null && StandardCharsets.UTF_8.newEncoder().canEncode(password)
                && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    private static RefreshParts parseCookie(String cookie) {
        if (cookie == null) throw badSession();
        String[] pieces = cookie.split("\\.", -1);
        if (pieces.length != 2) throw badSession();
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(pieces[1]);
            if (bytes.length != 32 || !Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).equals(pieces[1])) throw badSession();
            return new RefreshParts(UUID.fromString(pieces[0]), pieces[1]);
        } catch (IllegalArgumentException ex) {
            throw badSession();
        }
    }

    private static String hash(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static BadCredentialsException badSession() { return new BadCredentialsException(INVALID_SESSION); }
}
