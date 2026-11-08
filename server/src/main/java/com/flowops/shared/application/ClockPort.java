package com.flowops.shared.application;

import java.time.Instant;
import java.time.LocalDate;

public interface ClockPort {
    Instant now();
    LocalDate today();
}
