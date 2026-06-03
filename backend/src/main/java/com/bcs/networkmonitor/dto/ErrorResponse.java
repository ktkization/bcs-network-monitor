package com.bcs.networkmonitor.dto;

import java.time.Instant;

public record ErrorResponse(
        String message,
        Instant timestamp
) {}
