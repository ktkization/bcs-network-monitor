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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Page<DeviceListItemResponse> listAllDevices(int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return deviceRepository.findAll(pageable)
                .map(this::toListItem);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String property = mapSortProperty(parts[0]);
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    private String mapSortProperty(String property) {
        return switch (property) {
            case "stale" -> "lastReportAt";
            default -> property;
        };
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

        boolean stale = isStale(device.getLastReportAt());
        log.debug("Device stale check: id={}, reportCount={}, stale={}", id, recentReports.size(), stale);

        return toDetailResponse(device, stale, recentReportResponses);
    }

    private DeviceListItemResponse toListItem(Device device) {
        boolean stale = isStale(device.getLastReportAt());
        DeviceStatus currentStatus = device.getCurrentStatus() != null
                ? device.getCurrentStatus() : DeviceStatus.OFFLINE;

        return new DeviceListItemResponse(
                device.getId(), device.getUniqueId(), device.getName(),
                device.getDeviceType(), device.getHostname(), device.getLocation(),
                currentStatus, device.getLastReportAt(), stale
        );
    }

    private DeviceDetailResponse toDetailResponse(Device device, boolean stale, List<StatusReportResponse> recentReports) {
        DeviceStatus currentStatus = device.getCurrentStatus() != null
                ? device.getCurrentStatus() : DeviceStatus.OFFLINE;

        return new DeviceDetailResponse(
                device.getId(), device.getUniqueId(), device.getName(),
                device.getDeviceType(), device.getHostname(), device.getIpAddress(),
                device.getLocation(), device.getRegisteredAt(),
                currentStatus, device.getLastReportAt(),
                stale, recentReports
        );
    }

    private StatusReportResponse toReportResponse(StatusReport report) {
        return new StatusReportResponse(
                report.getId(), report.getReportedAt(),
                report.getStatus(), report.getMessage()
        );
    }

    private boolean isStale(Instant lastReportAt) {
        if (lastReportAt == null) return true;
        return Duration.between(lastReportAt, Instant.now())
                .compareTo(appProperties.getStaleThreshold()) > 0;
    }
}
