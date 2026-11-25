package com.flowops.identity.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;

/** Provisional TBD09 policy: account failures and IP attempts in a bounded, process-local window. */
@Component
public class LoginRateLimiter {
    private static final class Counter {
        private int count;
        private int reservations;
        private final Instant start;

        private Counter(Instant start) { this.start = start; }
    }

    private final Map<String, Counter> counters = new HashMap<>();
    private final int accountFailureLimit;
    private final int ipAttemptLimit;
    private final int windowSeconds;
    private final int maxEntries;

    public LoginRateLimiter(@Value("${flowops.auth.rate-limit.account-failures:5}") int accountFailureLimit,
                            @Value("${flowops.auth.rate-limit.ip-attempts:30}") int ipAttemptLimit,
                            @Value("${flowops.auth.rate-limit.window-seconds:900}") int windowSeconds,
                            @Value("${flowops.auth.rate-limit.max-entries:10000}") int maxEntries) {
        if (accountFailureLimit < 1 || ipAttemptLimit < 1 || windowSeconds < 1 || maxEntries < 2)
            throw new IllegalArgumentException("Invalid rate limit settings");
        this.accountFailureLimit = accountFailureLimit;
        this.ipAttemptLimit = ipAttemptLimit;
        this.windowSeconds = windowSeconds;
        this.maxEntries = maxEntries;
    }

    /** Admission is atomic; the monitor is released before BCrypt or any database work. */
    public synchronized Admission reserve(String username, String remoteIp) {
        Instant now = Instant.now();
        cleanup(now);
        String accountKey = "a:" + keyDigest(username);
        String ipKey = "i:" + keyDigest(remoteIp);
        Counter account = counters.get(accountKey);
        Counter ip = counters.get(ipKey);
        if (account != null && account.count + account.reservations >= accountFailureLimit)
            throw new RateLimitException(retryAfter(account, now));
        if (ip != null && ip.count >= ipAttemptLimit)
            throw new RateLimitException(retryAfter(ip, now));
        int needed = (account == null ? 1 : 0) + (ip == null ? 1 : 0);
        if (counters.size() + needed > maxEntries) throw new RateLimitException(windowSeconds);
        if (account == null) account = counters.computeIfAbsent(accountKey, ignored -> new Counter(now));
        if (ip == null) ip = counters.computeIfAbsent(ipKey, ignored -> new Counter(now));
        account.reservations++;
        ip.reservations++;
        ip.count++; // IP policy counts every admitted attempt, including successful logins.
        return new Admission(accountKey, ipKey);
    }

    public final class Admission implements AutoCloseable {
        private final String accountKey;
        private final String ipKey;
        private boolean completed;

        private Admission(String accountKey, String ipKey) {
            this.accountKey = accountKey;
            this.ipKey = ipKey;
        }

        public void failed() { complete(this, true, false); }
        public void succeeded() { complete(this, false, true); }
        @Override public void close() { complete(this, false, false); }
    }

    private synchronized void complete(Admission admission, boolean failed, boolean succeeded) {
        if (admission.completed) return;
        admission.completed = true;
        Counter account = counters.get(admission.accountKey);
        Counter ip = counters.get(admission.ipKey);
        if (account == null || ip == null) throw new IllegalStateException("Rate limiter reservation lost");
        account.reservations--;
        ip.reservations--;
        if (failed) account.count++;
        if (succeeded) account.count = 0;
        if (account.count == 0 && account.reservations == 0) counters.remove(admission.accountKey);
    }

    private int retryAfter(Counter counter, Instant now) {
        return (int) Math.max(1, Duration.between(now, counter.start.plusSeconds(windowSeconds)).getSeconds());
    }

    private void cleanup(Instant now) {
        Iterator<Counter> it = counters.values().iterator();
        while (it.hasNext()) {
            Counter counter = it.next();
            if (counter.reservations == 0 && !now.isBefore(counter.start.plusSeconds(windowSeconds))) it.remove();
        }
    }

    private static String keyDigest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
