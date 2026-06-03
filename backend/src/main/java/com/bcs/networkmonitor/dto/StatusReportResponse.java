package com.bcs.networkmonitor.dto;

import com.bcs.networkmonitor.entity.DeviceStatus;

import java.time.Instant;

public record StatusReportResponse(
        Long id,
        Instant reportedAt,
        DeviceStatus status,
        String message
) {}
