package com.flowops.identity.api.dto;

import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(@NotNull String currentPassword, @NotNull String newPassword) {
    @Override public String toString() { return "ChangePasswordRequest[REDACTED]"; }
}
