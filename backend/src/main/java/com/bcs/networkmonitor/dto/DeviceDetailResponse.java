package com.bcs.networkmonitor.dto;

import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;

import java.time.Instant;
import java.util.List;

public record DeviceDetailResponse(
        Long id,
        String uniqueId,
        String name,
        DeviceType deviceType,
        String hostname,
        String ipAddress,
        String location,
        Instant registeredAt,
        DeviceStatus currentStatus,
        Instant lastReportTimestamp,
        boolean stale,
        List<StatusReportResponse> recentReports
) {}
