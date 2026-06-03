package com.bcs.networkmonitor.controller;

import com.bcs.networkmonitor.dto.DeviceDetailResponse;
import com.bcs.networkmonitor.dto.DeviceListItemResponse;
import com.bcs.networkmonitor.dto.DeviceRegistrationRequest;
import com.bcs.networkmonitor.dto.DeviceResponse;
import com.bcs.networkmonitor.dto.StatusReportRequest;
import com.bcs.networkmonitor.dto.StatusReportResponse;
import com.bcs.networkmonitor.entity.Device;
import com.bcs.networkmonitor.entity.StatusReport;
import com.bcs.networkmonitor.service.DeviceService;
import com.bcs.networkmonitor.service.StatusReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Validated
@Tag(name = "Devices", description = "Device registration and status monitoring endpoints")
public class DeviceController {
    private final DeviceService deviceService;
    private final StatusReportService statusReportService;

    @Operation(summary = "Register a new device", description = "Creates a new network device in the monitoring system.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Device with this uniqueId already exists")
    })
    @PostMapping
    public ResponseEntity<DeviceResponse> register(@Valid @RequestBody DeviceRegistrationRequest request) {
        Device device = deviceService.registerDevice(request);
        DeviceResponse response = new DeviceResponse(
                device.getId(), device.getUniqueId(), device.getName(),
                device.getDeviceType(), device.getHostname(), device.getIpAddress(),
                device.getLocation(), device.getRegisteredAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List all devices", description = "Returns a paginated list of registered devices with their current status and stale indicators.")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<DeviceListItemResponse>> listAll(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort criteria (e.g., registeredAt,desc)", example = "registeredAt,desc")
            @RequestParam(defaultValue = "registeredAt,desc") String sort) {
        Pageable pageable = parsePageable(page, size, sort);
        return ResponseEntity.ok(deviceService.listAllDevices(pageable));
    }

    private Pageable parsePageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    @Operation(summary = "Get device detail", description = "Returns detailed information about a specific device, including recent status reports.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailResponse> getDetail(
            @Parameter(description = "Device ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceDetail(id));
    }

    @Operation(summary = "Submit a status report", description = "Submits a new status report for a specific device.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Report submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @PostMapping("/{id}/status-reports")
    public ResponseEntity<StatusReportResponse> submitReport(
            @Parameter(description = "Device ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody StatusReportRequest request) {
        StatusReport report = statusReportService.submitReport(id, request);
        StatusReportResponse response = new StatusReportResponse(
                report.getId(), report.getReportedAt(), report.getStatus(), report.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
