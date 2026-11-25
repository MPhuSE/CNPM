package com.flowops.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowops.identity.infrastructure.persistence.SpringDataUserRepository;
import com.flowops.identity.infrastructure.persistence.SpringDataUserSessionRepository;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.identity.infrastructure.persistence.UserSessionJpaEntity;
import com.flowops.identity.application.LoginRateLimiter;
import com.flowops.identity.application.AuthService;
import com.flowops.identity.application.SessionAuthenticationService;
import com.flowops.bootstrap.FlowOpsApplication;
import com.flowops.shared.infrastructure.JwtTokenProvider;
import com.flowops.shared.infrastructure.SessionPrincipal;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Testcontainers
@SpringBootTest(classes = FlowOpsApplication.class, properties = {"flowops.auth.bcrypt-strength=4", "flowops.auth.cookie.secure=false",
        "flowops.auth.rate-limit.account-failures=100", "flowops.auth.rate-limit.ip-attempts=100",
        "spring.jpa.hibernate.ddl-auto=none"})
@AutoConfigureMockMvc
class AuthSessionIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("flowops_test").withUsername("flowops").withPassword("flowops");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PasswordEncoder encoder;
    @Autowired SpringDataUserRepository users;
    @Autowired SpringDataUserSessionRepository sessions;
    @Autowired JwtTokenProvider jwt;
    @Autowired LoginRateLimiter limiter;
    @Autowired AuthService authService;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired SessionAuthenticationService sessionAuthenticationService;
    @Autowired JdbcTemplate jdbc;

    private static final String ORIGIN = "http://localhost:5173";
    private static final String PASSWORD = "test-password";
    private static boolean baselineShimApplied;

    private static void compatibilityShimForBaselineSchema() throws Exception {
        if (baselineShimApplied) return;
        // Existing V1 lacks BaseEntity.lockVersion on departments/approval_groups.
        // This isolated container shim tests auth behavior; production still needs a separately approved migration.
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("ALTER TABLE departments ADD COLUMN lock_version BIGINT NOT NULL DEFAULT 0");
            statement.execute("ALTER TABLE approval_groups ADD COLUMN lock_version BIGINT NOT NULL DEFAULT 0");
        }
        baselineShimApplied = true;
    }

    @BeforeEach
    void resetUser() throws Exception {
        compatibilityShimForBaselineSchema();
        sessions.deleteAll();
        UserJpaEntity user = users.findByUsername("employee").orElseThrow();
        user.setPasswordHash(encoder.encode(PASSWORD));
        user.setAuthVersion(0L);
        user.setIsActive(true);
        users.saveAndFlush(user);
    }

    @Test
    void loginIsGenericAndCookieOnlyContainsRefreshSecret() throws Exception {
        MvcResult success = login("employee", PASSWORD);
        assertEquals(200, success.getResponse().getStatus());
        assertTrue(success.getResponse().getHeader(HttpHeaders.SET_COOKIE).contains("HttpOnly"));
        assertTrue(success.getResponse().getHeader(HttpHeaders.SET_COOKIE).contains("SameSite=Lax"));
        assertTrue(success.getResponse().getHeader(HttpHeaders.SET_COOKIE).contains("Path=/api/v1/auth"));
        assertFalse(success.getResponse().getHeader(HttpHeaders.SET_COOKIE).contains("Domain="));
        assertFalse(success.getResponse().getHeader(HttpHeaders.SET_COOKIE).contains("Secure"));
        long cookieAge = Long.parseLong(success.getResponse().getHeader(HttpHeaders.SET_COOKIE).split("Max-Age=")[1].split(";", 2)[0]);
        assertTrue(cookieAge > 0 && cookieAge <= 604800);
        JsonNode body = mapper.readTree(success.getResponse().getContentAsString());
        assertTrue(body.hasNonNull("accessToken"));
        assertEquals("employee", body.path("user").path("username").asText());
        assertFalse(success.getResponse().getContentAsString().contains(cookieValue(success)));
        UUID successSid = UUID.fromString(jwt.parseClaims(body.path("accessToken").asText()).get("sid", String.class));
        UserSessionJpaEntity created = sessions.findById(successSid).orElseThrow();
        assertFalse(jwt.parseClaims(body.path("accessToken").asText()).getExpiration().toInstant().isAfter(created.getExpiresAt()));
        String bad = normalizeTrace(login("employee", "wrong").getResponse().getContentAsString());
        assertEquals(401, login("nobody", "wrong").getResponse().getStatus());
        assertEquals(bad, normalizeTrace(login("nobody", "wrong").getResponse().getContentAsString()));
        UserJpaEntity user = users.findByUsername("employee").orElseThrow();
        user.setIsActive(false);
        users.saveAndFlush(user);
        assertEquals(bad, normalizeTrace(login("employee", PASSWORD).getResponse().getContentAsString()));
    }

    @Test
    void refreshRotatesAndConcurrentReuseHasOneWinner() throws Exception {
        MvcResult first = login("employee", PASSWORD);
        String initialCookie = cookieValue(first);
        MvcResult refreshed = refresh(initialCookie);
        assertEquals(200, refreshed.getResponse().getStatus());
        assertNotEquals(initialCookie, cookieValue(refreshed));
        MvcResult replay = refresh(initialCookie);
        assertEquals(401, replay.getResponse().getStatus());
        assertNull(replay.getResponse().getHeader(HttpHeaders.SET_COOKIE));

        String currentCookie = cookieValue(refreshed);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Integer> a = pool.submit(() -> { start.await(); return refresh(currentCookie).getResponse().getStatus(); });
            Future<Integer> b = pool.submit(() -> { start.await(); return refresh(currentCookie).getResponse().getStatus(); });
            start.countDown();
            assertEquals(List.of(200, 401), List.of(a.get(), b.get()).stream().sorted().toList());
        }
    }

    @Test
    void logoutRevokesOnlyCurrentSessionAndPasswordRevokesAll() throws Exception {
        MvcResult first = login("employee", PASSWORD);
        MvcResult second = login("employee", PASSWORD);
        String firstToken = token(first);
        String secondToken = token(second);
        MockHttpServletResponse logout = mvc.perform(post("/api/v1/auth/logout").header("Origin", ORIGIN)
                .header("X-FlowOps-CSRF", "1").header("Authorization", "Bearer " + firstToken)).andReturn().getResponse();
        assertEquals(204, logout.getStatus());
        assertTrue(logout.getHeader(HttpHeaders.SET_COOKIE).contains("Max-Age=0"));
        assertEquals(401, me(firstToken));
        assertEquals(200, me(secondToken));

        MockHttpServletResponse badPassword = password(secondToken, "wrong", "new-strong-password");
        assertEquals(401, badPassword.getStatus());
        assertEquals(200, me(secondToken));
        assertEquals(204, password(secondToken, PASSWORD, "new-strong-password").getStatus());
        assertEquals(401, me(secondToken));
        assertEquals(401, login("employee", PASSWORD).getResponse().getStatus());
        assertEquals(200, login("employee", "new-strong-password").getResponse().getStatus());
    }

    @Test
    void jwtSessionVersionAndOriginCsrfAreEnforced() throws Exception {
        MvcResult login = login("employee", PASSWORD);
        String token = token(login);
        assertEquals(200, me(token));
        assertEquals(401, me(new JwtTokenProvider("another-signing-key-with-at-least-32-bytes", 900000)
                .generateAccessToken(3L, UUID.randomUUID(), 0L, Instant.now().plusSeconds(30))));
        assertEquals(401, me(jwt.generateAccessToken(3L, UUID.randomUUID(), 0L, Instant.now().plusSeconds(30))));
        assertEquals(401, me(jwt.generateAccessToken(3L, UUID.randomUUID(), 1L, Instant.now().plusSeconds(30))));
        assertEquals(401, me(jwt.generateAccessToken(3L, UUID.randomUUID(), 0L, Instant.now().minusSeconds(1))));
        UUID sid = UUID.fromString(jwt.parseClaims(token).get("sid", String.class));
        assertEquals(401, me(jwt.generateAccessToken(1L, sid, 0L, Instant.now().plusSeconds(30))));
        UserSessionJpaEntity session = sessions.findById(sid).orElseThrow();
        session.setExpiresAt(Instant.now().minusSeconds(1));
        sessions.saveAndFlush(session);
        assertEquals(401, me(token));
        session.setExpiresAt(Instant.now().plusSeconds(60));
        session.setRevokedAt(Instant.now());
        sessions.saveAndFlush(session);
        assertEquals(401, me(token));
        assertEquals(403, mvc.perform(post("/api/v1/auth/login").header("X-FlowOps-CSRF", "1")
                .contentType("application/json").content("{}")).andReturn().getResponse().getStatus());
        assertEquals(403, mvc.perform(post("/api/v1/auth/login").header("Origin", ORIGIN)
                .contentType("application/json").content("{}")).andReturn().getResponse().getStatus());
        MockHttpServletResponse invalidOrigin = mvc.perform(post("/api/v1/auth/login")
                .header("Origin", "null").header("X-FlowOps-CSRF", "1")
                .contentType("application/json").content("{}")).andReturn().getResponse();
        assertEquals(403, invalidOrigin.getStatus());
        assertEquals("ACCESS_DENIED", mapper.readTree(invalidOrigin.getContentAsString()).path("code").asText());
        assertEquals(200, mvc.perform(options("/api/v1/auth/login").header("Origin", ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-FlowOps-CSRF,Content-Type"))
                .andReturn().getResponse().getStatus());
    }

    @Test
    void bearerAuthoritiesReflectCurrentDatabaseRoles() throws Exception {
        String token = token(login("employee", PASSWORD));
        UUID sid = UUID.fromString(jwt.parseClaims(token).get("sid", String.class));
        assertTrue(sessionAuthenticationService.authenticate(3L, sid, 0L).authorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_EMPLOYEE")));
        jdbc.update("DELETE FROM user_roles WHERE user_id = 3 AND role_id = 2");
        try {
            assertTrue(sessionAuthenticationService.authenticate(3L, sid, 0L).authorities().isEmpty());
            assertEquals(200, me(token));
        } finally {
            jdbc.update("INSERT INTO user_roles (user_id, role_id, assigned_by) VALUES (3, 2, 1)");
        }
    }

    @Test
    void passwordUnicodeAndBcryptByteBoundary() throws Exception {
        String bearer = token(login("employee", PASSWORD));
        assertEquals(400, password(bearer, PASSWORD, "😀".repeat(14)).getStatus());
        assertEquals(400, password(bearer, PASSWORD, "😀".repeat(19)).getStatus());
        assertEquals(200, me(bearer));
        String exactly72Bytes = "😀".repeat(18);
        assertEquals(204, password(bearer, PASSWORD, exactly72Bytes).getStatus());
        assertEquals(200, login("employee", exactly72Bytes).getResponse().getStatus());
    }

    @Test
    void loginRejectsBcryptTruncationAndMalformedUnicodeWithoutServerError() throws Exception {
        String exactly72Bytes = "😀".repeat(18);
        UserJpaEntity user = users.findByUsername("employee").orElseThrow();
        user.setPasswordHash(encoder.encode(exactly72Bytes));
        users.saveAndFlush(user);
        MvcResult success = login("employee", exactly72Bytes);
        assertEquals(200, success.getResponse().getStatus());
        assertEquals(401, login("employee", exactly72Bytes + "x").getResponse().getStatus());
        MockHttpServletResponse malformed = mvc.perform(post("/api/v1/auth/login")
                .header("Origin", ORIGIN).header("X-FlowOps-CSRF", "1")
                .contentType("application/json")
                .content("{\"username\":\"employee\",\"password\":\"\\uD800\"}"))
                .andReturn().getResponse();
        assertEquals(401, malformed.getStatus());
        assertEquals("UNAUTHORIZED", mapper.readTree(malformed.getContentAsString()).path("code").asText());
        String bearer = token(success);
        assertEquals(401, password(bearer, exactly72Bytes + "x", "another-valid-password").getStatus());
        String malformedNew = "{\"currentPassword\":" + mapper.writeValueAsString(exactly72Bytes)
                + ",\"newPassword\":\"" + "\\uD800".repeat(15) + "\"}";
        assertEquals(400, mvc.perform(post("/api/v1/auth/password").header("Origin", ORIGIN)
                .header("X-FlowOps-CSRF", "1").header("Authorization", "Bearer " + bearer)
                .contentType("application/json").content(malformedNew)).andReturn().getResponse().getStatus());
        assertEquals(200, me(bearer));
    }

    @Test
    void passwordChangePreservesRevocationTimestampOfOldSession() throws Exception {
        MvcResult active = login("employee", PASSWORD);
        MvcResult old = login("employee", PASSWORD);
        UUID oldSid = UUID.fromString(jwt.parseClaims(token(old)).get("sid", String.class));
        UserSessionJpaEntity revoked = sessions.findById(oldSid).orElseThrow();
        Instant revokedAt = Instant.now().minusSeconds(60).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        revoked.setRevokedAt(revokedAt);
        sessions.saveAndFlush(revoked);
        assertEquals(204, password(token(active), PASSWORD, "another-valid-password").getStatus());
        assertEquals(revokedAt, sessions.findById(oldSid).orElseThrow().getRevokedAt());
    }

    @Test
    void legacyShortPasswordCanStillLogInButCannotBeSetAsNewPassword() throws Exception {
        String legacyPassword = "oldpass";
        UserJpaEntity user = users.findByUsername("employee").orElseThrow();
        user.setPasswordHash(encoder.encode(legacyPassword));
        users.saveAndFlush(user);

        MvcResult login = login("employee", legacyPassword);
        assertEquals(200, login.getResponse().getStatus());
        assertEquals(400, password(token(login), legacyPassword, "short-new").getStatus());
        assertEquals(200, me(token(login)));
    }

    @Test
    void rateLimitReturnsJson429AndRetryAfter() throws Exception {
        String uniqueName = "rate-" + UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            try (var admission = limiter.reserve(uniqueName, "test-only-ip")) { admission.failed(); }
        }
        MockHttpServletResponse response = login(uniqueName, "wrong").getResponse();
        assertEquals(429, response.getStatus());
        assertNotNull(response.getHeader(HttpHeaders.RETRY_AFTER));
        assertEquals("RATE_LIMITED", mapper.readTree(response.getContentAsString()).path("code").asText());
    }

    @Test
    void swaggerMarksLoginAndRefreshPublic() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/v3/api-docs")).andReturn().getResponse();
        assertEquals(200, response.getStatus());
        JsonNode paths = mapper.readTree(response.getContentAsString()).path("paths");
        JsonNode loginSecurity = paths.path("/api/v1/auth/login").path("post").path("security");
        JsonNode refreshSecurity = paths.path("/api/v1/auth/refresh").path("post").path("security");
        assertTrue(loginSecurity.isArray() && loginSecurity.isEmpty());
        assertTrue(refreshSecurity.isArray() && refreshSecurity.isEmpty());
    }

    @Test
    void loginWaitingOnPasswordChangeRechecksPasswordUnderUserLock() throws Exception {
        MvcResult existing = login("employee", PASSWORD);
        UUID sid = UUID.fromString(jwt.parseClaims(token(existing)).get("sid", String.class));
        SessionPrincipal principal = new SessionPrincipal(3L, sid, 0L);
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<?> change = pool.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                users.findByIdForUpdate(3L).orElseThrow();
                locked.countDown();
                await(release);
                authService.changePassword(principal, PASSWORD, "changed-password-123");
            }));
            locked.await();
            Future<Integer> oldLogin = pool.submit(() -> login("employee", PASSWORD).getResponse().getStatus());
            release.countDown();
            change.get();
            assertEquals(401, oldLogin.get());
            assertEquals(200, login("employee", "changed-password-123").getResponse().getStatus());
        }
    }

    @Test
    void refreshWaitingOnLogoutSeesRevokedSession() throws Exception {
        MvcResult existing = login("employee", PASSWORD);
        UUID sid = UUID.fromString(jwt.parseClaims(token(existing)).get("sid", String.class));
        SessionPrincipal principal = new SessionPrincipal(3L, sid, 0L);
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<?> logout = pool.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                users.findByIdForUpdate(3L).orElseThrow();
                locked.countDown();
                await(release);
                authService.logout(principal);
            }));
            locked.await();
            Future<Integer> refresh = pool.submit(() -> refresh(cookieValue(existing)).getResponse().getStatus());
            release.countDown();
            logout.get();
            assertEquals(401, refresh.get());
        }
    }

    private static void await(CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
    }

    private MvcResult login(String username, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").header("Origin", ORIGIN).header("X-FlowOps-CSRF", "1")
                .contentType("application/json")
                .content(mapper.writeValueAsString(new LoginBody(username, password)))).andReturn();
    }
    private MvcResult refresh(String cookie) throws Exception {
        return mvc.perform(post("/api/v1/auth/refresh").header("Origin", ORIGIN).header("X-FlowOps-CSRF", "1")
                .cookie(new Cookie("flowops_refresh", cookie))).andReturn();
    }
    private MockHttpServletResponse password(String bearer, String current, String next) throws Exception {
        return mvc.perform(post("/api/v1/auth/password").header("Origin", ORIGIN).header("X-FlowOps-CSRF", "1")
                .header("Authorization", "Bearer " + bearer).contentType("application/json")
                .content(mapper.writeValueAsString(new PasswordBody(current, next)))).andReturn().getResponse();
    }
    private int me(String bearer) throws Exception {
        return mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + bearer)).andReturn().getResponse().getStatus();
    }
    private String token(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString()).path("accessToken").asText();
    }
    private String cookieValue(MvcResult result) {
        return result.getResponse().getHeader(HttpHeaders.SET_COOKIE).split(";", 2)[0].split("=", 2)[1];
    }
    private String normalizeTrace(String body) throws Exception {
        JsonNode tree = mapper.readTree(body);
        ((com.fasterxml.jackson.databind.node.ObjectNode) tree).remove("traceId");
        return tree.toString();
    }
    private record LoginBody(String username, String password) {}
    private record PasswordBody(String currentPassword, String newPassword) {}
}
