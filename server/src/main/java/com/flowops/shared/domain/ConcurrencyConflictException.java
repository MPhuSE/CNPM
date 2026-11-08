package com.flowops.shared.domain;

public class ConcurrencyConflictException extends DomainException {
    public ConcurrencyConflictException(String code, String message) {
        super(code, message);
    }
}
