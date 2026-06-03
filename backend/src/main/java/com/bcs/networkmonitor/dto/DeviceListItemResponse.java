package com.bcs.networkmonitor.dto;

import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;

import java.time.Instant;

public record DeviceListItemResponse(
        Long id,
        String uniqueId,
        String name,
        DeviceType deviceType,
        String hostname,
        String location,
        DeviceStatus currentStatus,
        Instant lastReportTimestamp,
        boolean stale
) {}
