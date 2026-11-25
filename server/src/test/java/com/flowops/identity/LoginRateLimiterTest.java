package com.flowops.identity;

import com.flowops.identity.application.LoginRateLimiter;
import com.flowops.identity.application.RateLimitException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {
    @Test
    void concurrentReservationsAdmitOnlyOneAtAccountLimit() throws Exception {
        LoginRateLimiter limiter = new LoginRateLimiter(1, 10, 900, 2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch decisions = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = pool.submit(() -> attempt(limiter, start, decisions, release));
            Future<Boolean> second = pool.submit(() -> attempt(limiter, start, decisions, release));
            start.countDown();
            try {
                assertTrue(decisions.await(5, TimeUnit.SECONDS));
            } finally { release.countDown(); }
            assertNotEquals(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
        }
        // Closing the winning reservation releases account capacity after unexpected completion.
        try (var admission = limiter.reserve("alice", "127.0.0.1")) { admission.succeeded(); }
    }

    private static boolean attempt(LoginRateLimiter limiter, CountDownLatch start, CountDownLatch decisions,
                                   CountDownLatch release) throws Exception {
        start.await();
        try (var admission = limiter.reserve("alice", "127.0.0.1")) {
            decisions.countDown();
            release.await();
            return true;
        } catch (RateLimitException ex) {
            decisions.countDown();
            return false;
        }
    }

    @Test
    void ipCountsAllAttemptsIncludingSuccessAndAccountCountsOnlyFailures() {
        LoginRateLimiter limiter = new LoginRateLimiter(2, 2, 900, 3);
        try (var admission = limiter.reserve("alice", "127.0.0.1")) { admission.succeeded(); }
        try (var admission = limiter.reserve("alice", "127.0.0.1")) { admission.succeeded(); }
        RateLimitException ip = assertThrows(RateLimitException.class, () -> limiter.reserve("alice", "127.0.0.1"));
        assertTrue(ip.getRetryAfterSeconds() > 0);
    }

    @Test
    void failuresAreAtomicAndMemoryBoundIsFailClosed() {
        LoginRateLimiter limiter = new LoginRateLimiter(2, 10, 900, 3);
        try (var admission = limiter.reserve("alice", "ip-a")) { admission.failed(); }
        try (var admission = limiter.reserve("alice", "ip-a")) { admission.failed(); }
        assertThrows(RateLimitException.class, () -> limiter.reserve("alice", "ip-a"));
        // One account and one IP occupy two slots; two new keys cannot exceed maxEntries=3.
        assertThrows(RateLimitException.class, () -> limiter.reserve("bob", "ip-b"));
    }

    @Test
    void unexpectedExitReleasesReservationWithoutCountingAccountFailure() {
        LoginRateLimiter limiter = new LoginRateLimiter(1, 3, 900, 2);
        assertThrows(IllegalStateException.class, () -> {
            try (var admission = limiter.reserve("alice", "127.0.0.1")) {
                throw new IllegalStateException("database unavailable");
            }
        });
        try (var admission = limiter.reserve("alice", "127.0.0.1")) { admission.succeeded(); }
    }
}
