package com.bcs.networkmonitor.repository;

import com.bcs.networkmonitor.TestcontainersConfiguration;
import com.bcs.networkmonitor.entity.Device;
import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;
import com.bcs.networkmonitor.entity.StatusReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceRepositoryIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private StatusReportRepository statusReportRepository;

    @Test
    void shouldSaveAndFindDeviceByUniqueId() {
        Device device = Device.builder()
                .uniqueId("CPE-001")
                .name("Test CPE")
                .deviceType(DeviceType.CPE)
                .hostname("cpe-001.local")
                .location("Building A")
                .registeredAt(Instant.now())
                .build();

        Device saved = deviceRepository.save(device);
        assertThat(saved.getId()).isNotNull();

        Optional<Device> found = deviceRepository.findByUniqueId("CPE-001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test CPE");
    }

    @Test
    void shouldCheckExistenceByUniqueId() {
        Device device = Device.builder()
                .uniqueId("RTR-001")
                .name("Test Router")
                .deviceType(DeviceType.ROUTER)
                .hostname("rtr-001.local")
                .location("Building B")
                .registeredAt(Instant.now())
                .build();

        deviceRepository.save(device);

        assertThat(deviceRepository.existsByUniqueId("RTR-001")).isTrue();
        assertThat(deviceRepository.existsByUniqueId("NONEXISTENT")).isFalse();
    }

    @Test
    void shouldFindTop20ReportsOrderedByReportedAtDesc() {
        Device device = Device.builder()
                .uniqueId("SW-001")
                .name("Test Switch")
                .deviceType(DeviceType.SWITCH)
                .hostname("sw-001.local")
                .location("Building C")
                .registeredAt(Instant.now())
                .build();

        Device savedDevice = deviceRepository.save(device);

        for (int i = 0; i < 25; i++) {
            StatusReport report = StatusReport.builder()
                    .device(savedDevice)
                    .reportedAt(Instant.now().plusSeconds(i))
                    .status(DeviceStatus.ONLINE)
                    .message("Report " + i)
                    .build();
            statusReportRepository.save(report);
        }

        List<StatusReport> reports = statusReportRepository
                .findTop20ByDeviceIdOrderByReportedAtDesc(savedDevice.getId());

        assertThat(reports).hasSize(20);
        assertThat(reports.get(0).getMessage()).isEqualTo("Report 24");
        assertThat(reports.get(19).getMessage()).isEqualTo("Report 5");
    }

    @Test
    void shouldFindTopReportByDeviceId() {
        Device device = Device.builder()
                .uniqueId("AP-001")
                .name("Test AP")
                .deviceType(DeviceType.ACCESS_POINT)
                .hostname("ap-001.local")
                .location("Building D")
                .registeredAt(Instant.now())
                .build();

        Device savedDevice = deviceRepository.save(device);

        StatusReport oldReport = StatusReport.builder()
                .device(savedDevice)
                .reportedAt(Instant.now().minusSeconds(300))
                .status(DeviceStatus.OFFLINE)
                .build();
        statusReportRepository.save(oldReport);

        StatusReport newReport = StatusReport.builder()
                .device(savedDevice)
                .reportedAt(Instant.now())
                .status(DeviceStatus.ONLINE)
                .build();
        statusReportRepository.save(newReport);

        Optional<StatusReport> topReport = statusReportRepository
                .findTopByDeviceIdOrderByReportedAtDesc(savedDevice.getId());

        assertThat(topReport).isPresent();
        assertThat(topReport.get().getStatus()).isEqualTo(DeviceStatus.ONLINE);
    }
}
