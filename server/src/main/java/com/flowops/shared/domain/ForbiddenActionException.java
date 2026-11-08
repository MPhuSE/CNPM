package com.flowops.shared.domain;

public class ForbiddenActionException extends DomainException {
    public ForbiddenActionException(String code, String message) {
        super(code, message);
    }
}
