package com.flowops.identity.application;

public class RateLimitException extends RuntimeException {
    private final int retryAfterSeconds;
    public RateLimitException(int retryAfterSeconds) {
        super("Thử đăng nhập quá nhiều lần");
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public int getRetryAfterSeconds() { return retryAfterSeconds; }
}
