package com.bcs.networkmonitor.service;

import com.bcs.networkmonitor.config.AppProperties;
import com.bcs.networkmonitor.dto.DeviceDetailResponse;
import com.bcs.networkmonitor.dto.DeviceListItemResponse;
import com.bcs.networkmonitor.dto.DeviceRegistrationRequest;
import com.bcs.networkmonitor.dto.StatusReportResponse;
import com.bcs.networkmonitor.entity.Device;
import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.StatusReport;
import com.bcs.networkmonitor.exception.DuplicateResourceException;
import com.bcs.networkmonitor.exception.ResourceNotFoundException;
import com.bcs.networkmonitor.repository.DeviceRepository;
import com.bcs.networkmonitor.repository.StatusReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepository;
    private final StatusReportRepository statusReportRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public Device registerDevice(DeviceRegistrationRequest request) {
        if (deviceRepository.existsByUniqueId(request.uniqueId())) {
            log.warn("Duplicate device registration: uniqueId={}", request.uniqueId());
            throw new DuplicateResourceException("Device with uniqueId already exists");
        }
        Device device = Device.builder()
                .uniqueId(request.uniqueId())
                .name(request.name())
                .deviceType(request.deviceType())
                .hostname(request.hostname())
                .ipAddress(request.ipAddress())
                .location(request.location())
                .registeredAt(Instant.now())
                .build();
        Device saved = deviceRepository.save(device);
        log.info("Device registered: id={}, uniqueId={}", saved.getId(), saved.getUniqueId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceListItemResponse> listAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDetailResponse getDeviceDetail(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Device not found: id={}", id);
                    return new ResourceNotFoundException("Device not found");
                });

        List<StatusReport> recentReports = statusReportRepository
                .findTop20ByDeviceIdOrderByReportedAtDesc(id);

        List<StatusReportResponse> recentReportResponses = recentReports.stream()
                .map(this::toReportResponse)
                .toList();

        StatusReport lastReport = recentReports.isEmpty() ? null : recentReports.getFirst();
        boolean stale = isStale(lastReport);
        log.debug("Device stale check: id={}, reportCount={}, stale={}", id, recentReports.size(), stale);

        return toDetailResponse(device, lastReport, stale, recentReportResponses);
    }

    private DeviceListItemResponse toListItem(Device device) {
        StatusReport lastReport = statusReportRepository
                .findTopByDeviceIdOrderByReportedAtDesc(device.getId())
                .orElse(null);

        boolean stale = isStale(lastReport);
        DeviceStatus currentStatus = lastReport != null ? lastReport.getStatus() : DeviceStatus.OFFLINE;

        return new DeviceListItemResponse(
                device.getId(), device.getUniqueId(), device.getName(),
                device.getDeviceType(), device.getHostname(), device.getLocation(),
                currentStatus, lastReport != null ? lastReport.getReportedAt() : null, stale
        );
    }

    private DeviceDetailResponse toDetailResponse(Device device, StatusReport lastReport, boolean stale, List<StatusReportResponse> recentReports) {
        DeviceStatus currentStatus = lastReport != null ? lastReport.getStatus() : DeviceStatus.OFFLINE;

        return new DeviceDetailResponse(
                device.getId(), device.getUniqueId(), device.getName(),
                device.getDeviceType(), device.getHostname(), device.getIpAddress(),
                device.getLocation(), device.getRegisteredAt(),
                currentStatus, lastReport != null ? lastReport.getReportedAt() : null,
                stale, recentReports
        );
    }

    private StatusReportResponse toReportResponse(StatusReport report) {
        return new StatusReportResponse(
                report.getId(), report.getReportedAt(),
                report.getStatus(), report.getMessage()
        );
    }

    private boolean isStale(StatusReport lastReport) {
        if (lastReport == null) return true;
        return Duration.between(lastReport.getReportedAt(), Instant.now())
                .compareTo(appProperties.getStaleThreshold()) > 0;
    }
}
