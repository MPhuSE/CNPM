package com.flowops.shared.domain;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entityName, Object id) {
        super("NOT_FOUND", String.format("Không tìm thấy %s với mã: %s", entityName, id));
    }
}
