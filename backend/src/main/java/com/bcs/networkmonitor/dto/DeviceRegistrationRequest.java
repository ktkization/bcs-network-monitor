package com.bcs.networkmonitor.dto;

import com.bcs.networkmonitor.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DeviceRegistrationRequest(
        @NotBlank String uniqueId,
        @NotBlank String name,
        @NotNull DeviceType deviceType,
        @NotBlank String hostname,
        @Pattern(regexp = "^$|^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$") String ipAddress,
        @NotBlank String location
) {}
