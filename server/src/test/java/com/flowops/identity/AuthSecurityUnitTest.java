package com.flowops.identity;

import com.flowops.identity.application.AuthService;
import com.flowops.identity.api.AuthController;
import com.flowops.identity.api.dto.ChangePasswordRequest;
import com.flowops.shared.infrastructure.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthSecurityUnitTest {
    private static final String KEY = "test-only-signing-secret-with-32-bytes";

    @Test
    void accessTtlMustBePositiveAndCannotOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(KEY, 0));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(KEY, -1));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(KEY, Long.MAX_VALUE));
        assertNotNull(new JwtTokenProvider(KEY, 900_000));
    }

    @Test
    void internalAuthResultsDoNotPrintTokensOrSecrets() throws Exception {
        AuthService.AuthResult result = new AuthService.AuthResult("access-secret", null, "refresh-secret", Instant.now());
        assertFalse(result.toString().contains("access-secret"));
        assertFalse(result.toString().contains("refresh-secret"));

        Class<?> type = Class.forName("com.flowops.identity.application.AuthService$RefreshParts");
        Constructor<?> constructor = type.getDeclaredConstructor(UUID.class, String.class);
        constructor.setAccessible(true);
        Object parts = constructor.newInstance(UUID.randomUUID(), "refresh-secret");
        assertFalse(parts.toString().contains("refresh-secret"));
        assertFalse(new ChangePasswordRequest("current-secret", "new-secret").toString().contains("current-secret"));
    }

    @Test
    void productionProfileForcesSecureCookieEvenWhenFlagIsFalse() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        AuthController controller = new AuthController(null, false, environment);
        assertEquals(true, ReflectionTestUtils.getField(controller, "secureCookie"));
    }
}
