package com.bcs.networkmonitor.dto;

import com.bcs.networkmonitor.entity.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusReportRequest(
        @NotNull DeviceStatus status,
        @Size(max = 1000) String message
) {}
