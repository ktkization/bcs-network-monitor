package com.bcs.networkmonitor.service;

import com.bcs.networkmonitor.dto.StatusReportRequest;
import com.bcs.networkmonitor.entity.Device;
import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;
import com.bcs.networkmonitor.entity.StatusReport;
import com.bcs.networkmonitor.exception.ResourceNotFoundException;
import com.bcs.networkmonitor.repository.DeviceRepository;
import com.bcs.networkmonitor.repository.StatusReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusReportServiceTest {

    @Mock
    private StatusReportRepository statusReportRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private StatusReportServiceImpl statusReportService;

    @Test
    void submitReport_shouldSaveReport() {
        Device device = Device.builder()
                .id(1L)
                .uniqueId("CPE-001")
                .name("Test")
                .deviceType(DeviceType.CPE)
                .hostname("test.local")
                .location("Building A")
                .registeredAt(Instant.now())
                .build();

        StatusReportRequest request = new StatusReportRequest(DeviceStatus.ONLINE, "OK");

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(statusReportRepository.save(any(StatusReport.class))).thenAnswer(inv -> inv.getArgument(0));

        StatusReport result = statusReportService.submitReport(1L, request);

        assertThat(result.getStatus()).isEqualTo(DeviceStatus.ONLINE);
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getDevice()).isEqualTo(device);
    }

    @Test
    void submitReport_shouldThrowWhenDeviceNotFound() {
        StatusReportRequest request = new StatusReportRequest(DeviceStatus.ONLINE, null);

        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusReportService.submitReport(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
