package com.bcs.networkmonitor.service;

import com.bcs.networkmonitor.config.AppProperties;
import com.bcs.networkmonitor.dto.DeviceDetailResponse;
import com.bcs.networkmonitor.dto.DeviceListItemResponse;
import com.bcs.networkmonitor.dto.DeviceRegistrationRequest;
import com.bcs.networkmonitor.entity.Device;
import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;
import com.bcs.networkmonitor.entity.StatusReport;
import com.bcs.networkmonitor.exception.DuplicateResourceException;
import com.bcs.networkmonitor.exception.ResourceNotFoundException;
import com.bcs.networkmonitor.repository.DeviceRepository;
import com.bcs.networkmonitor.repository.StatusReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private StatusReportRepository statusReportRepository;

    private DeviceServiceImpl deviceService;
    private AppProperties appProperties;
    private Device testDevice;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        deviceService = new DeviceServiceImpl(deviceRepository, statusReportRepository, appProperties);
        testDevice = Device.builder()
                .id(1L)
                .uniqueId("CPE-001")
                .name("Test CPE")
                .deviceType(DeviceType.CPE)
                .hostname("cpe-001.local")
                .ipAddress("192.168.1.1")
                .location("Building A")
                .registeredAt(Instant.now())
                .build();
    }

    @Test
    void registerDevice_shouldSaveAndReturnDevice() {
        DeviceRegistrationRequest request = new DeviceRegistrationRequest(
                "CPE-001", "Test CPE", DeviceType.CPE,
                "cpe-001.local", "192.168.1.1", "Building A"
        );

        when(deviceRepository.existsByUniqueId("CPE-001")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        Device result = deviceService.registerDevice(request);

        assertThat(result).isNotNull();
        assertThat(result.getUniqueId()).isEqualTo("CPE-001");
        assertThat(result.getName()).isEqualTo("Test CPE");
    }

    @Test
    void registerDevice_shouldThrowOnDuplicate() {
        DeviceRegistrationRequest request = new DeviceRegistrationRequest(
                "CPE-001", "Test CPE", DeviceType.CPE,
                "cpe-001.local", null, "Building A"
        );

        when(deviceRepository.existsByUniqueId("CPE-001")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.registerDevice(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void listAllDevices_shouldReturnMappedList() {
        StatusReport report = StatusReport.builder()
                .id(1L)
                .device(testDevice)
                .reportedAt(Instant.now())
                .status(DeviceStatus.ONLINE)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        when(deviceRepository.findAll(eq(pageable))).thenReturn(new PageImpl<>(List.of(testDevice)));
        when(statusReportRepository.findTopByDeviceIdOrderByReportedAtDesc(1L))
                .thenReturn(Optional.of(report));

        var result = deviceService.listAllDevices(pageable);

        assertThat(result.getContent()).hasSize(1);
        DeviceListItemResponse item = result.getContent().getFirst();
        assertThat(item.currentStatus()).isEqualTo(DeviceStatus.ONLINE);
        assertThat(item.stale()).isFalse();
    }

    @Test
    void listAllDevices_shouldMarkAsStale_whenNoReport() {
        Pageable pageable = PageRequest.of(0, 20);
        when(deviceRepository.findAll(eq(pageable))).thenReturn(new PageImpl<>(List.of(testDevice)));
        when(statusReportRepository.findTopByDeviceIdOrderByReportedAtDesc(1L))
                .thenReturn(Optional.empty());

        var result = deviceService.listAllDevices(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().stale()).isTrue();
        assertThat(result.getContent().getFirst().currentStatus()).isEqualTo(DeviceStatus.OFFLINE);
    }

    @Test
    void listAllDevices_shouldMarkAsStale_whenReportOlderThan15Min() {
        StatusReport oldReport = StatusReport.builder()
                .id(1L)
                .device(testDevice)
                .reportedAt(Instant.now().minus(Duration.ofMinutes(20)))
                .status(DeviceStatus.ONLINE)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        when(deviceRepository.findAll(eq(pageable))).thenReturn(new PageImpl<>(List.of(testDevice)));
        when(statusReportRepository.findTopByDeviceIdOrderByReportedAtDesc(1L))
                .thenReturn(Optional.of(oldReport));

        var result = deviceService.listAllDevices(pageable);

        assertThat(result.getContent().getFirst().stale()).isTrue();
    }

    @Test
    void getDeviceDetail_shouldReturnDetailWithReports() {
        StatusReport report = StatusReport.builder()
                .id(1L)
                .reportedAt(Instant.now())
                .status(DeviceStatus.ONLINE)
                .message("All good")
                .build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(statusReportRepository.findTop20ByDeviceIdOrderByReportedAtDesc(1L))
                .thenReturn(List.of(report));

        DeviceDetailResponse result = deviceService.getDeviceDetail(1L);

        assertThat(result.name()).isEqualTo("Test CPE");
        assertThat(result.recentReports()).hasSize(1);
        assertThat(result.currentStatus()).isEqualTo(DeviceStatus.ONLINE);
        assertThat(result.stale()).isFalse();
        assertThat(result.ipAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    void getDeviceDetail_shouldMarkAsStaleAndOffline_whenNoReports() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(statusReportRepository.findTop20ByDeviceIdOrderByReportedAtDesc(1L))
                .thenReturn(List.of());

        DeviceDetailResponse result = deviceService.getDeviceDetail(1L);

        assertThat(result.recentReports()).isEmpty();
        assertThat(result.currentStatus()).isEqualTo(DeviceStatus.OFFLINE);
        assertThat(result.stale()).isTrue();
    }

    @Test
    void getDeviceDetail_shouldMarkAsStale_whenReportOlderThan15Min() {
        StatusReport oldReport = StatusReport.builder()
                .id(1L)
                .reportedAt(Instant.now().minus(Duration.ofMinutes(20)))
                .status(DeviceStatus.ONLINE)
                .message("Old")
                .build();

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(statusReportRepository.findTop20ByDeviceIdOrderByReportedAtDesc(1L))
                .thenReturn(List.of(oldReport));

        DeviceDetailResponse result = deviceService.getDeviceDetail(1L);

        assertThat(result.currentStatus()).isEqualTo(DeviceStatus.ONLINE);
        assertThat(result.stale()).isTrue();
    }

    @Test
    void getDeviceDetail_shouldThrowWhenNotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDeviceDetail(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
