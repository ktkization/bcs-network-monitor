package com.bcs.networkmonitor.dto;

import com.bcs.networkmonitor.entity.DeviceType;

import java.time.Instant;

public record DeviceResponse(
        Long id,
        String uniqueId,
        String name,
        DeviceType deviceType,
        String hostname,
        String ipAddress,
        String location,
        Instant registeredAt
) {}
