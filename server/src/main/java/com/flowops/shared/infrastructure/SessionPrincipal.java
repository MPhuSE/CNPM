package com.flowops.shared.infrastructure;

import java.util.UUID;

public record SessionPrincipal(Long userId, UUID sid, Long authVersion) {}
