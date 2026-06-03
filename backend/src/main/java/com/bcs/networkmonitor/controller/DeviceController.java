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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Validated
public class DeviceController {
    private final DeviceService deviceService;
    private final StatusReportService statusReportService;

    @PostMapping
    public ResponseEntity<DeviceResponse> register(@Valid @RequestBody DeviceRegistrationRequest request) {
        Device device = deviceService.registerDevice(request);
        DeviceResponse response = new DeviceResponse(
                device.getId(), device.getUniqueId(), device.getName(),
                device.getDeviceType(), device.getHostname(), device.getIpAddress(),
                device.getLocation(), device.getRegisteredAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeviceListItemResponse>> listAll() {
        return ResponseEntity.ok(deviceService.listAllDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceDetail(id));
    }

    @PostMapping("/{id}/status-reports")
    public ResponseEntity<StatusReportResponse> submitReport(
            @PathVariable Long id,
            @Valid @RequestBody StatusReportRequest request) {
        StatusReport report = statusReportService.submitReport(id, request);
        StatusReportResponse response = new StatusReportResponse(
                report.getId(), report.getReportedAt(), report.getStatus(), report.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
