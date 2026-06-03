package com.bcs.networkmonitor.service;

import com.bcs.networkmonitor.dto.StatusReportRequest;
import com.bcs.networkmonitor.entity.Device;
import com.bcs.networkmonitor.entity.StatusReport;
import com.bcs.networkmonitor.exception.ResourceNotFoundException;
import com.bcs.networkmonitor.repository.DeviceRepository;
import com.bcs.networkmonitor.repository.StatusReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusReportServiceImpl implements StatusReportService {
    private final StatusReportRepository statusReportRepository;
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional
    public StatusReport submitReport(Long deviceId, StatusReportRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> {
                    log.warn("Status report failed - device not found: id={}", deviceId);
                    return new ResourceNotFoundException("Device not found");
                });

        StatusReport report = StatusReport.builder()
                .device(device)
                .reportedAt(Instant.now())
                .status(request.status())
                .message(request.message())
                .build();

        StatusReport saved = statusReportRepository.save(report);
        log.info("Status report submitted: deviceId={}, status={}", deviceId, request.status());
        log.debug("Status report saved: id={}", saved.getId());
        return saved;
    }
}
