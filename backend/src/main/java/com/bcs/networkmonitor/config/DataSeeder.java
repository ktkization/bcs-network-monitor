package com.bcs.networkmonitor.config;

import com.bcs.networkmonitor.entity.Device;
import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;
import com.bcs.networkmonitor.entity.StatusReport;
import com.bcs.networkmonitor.repository.DeviceRepository;
import com.bcs.networkmonitor.repository.StatusReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AppProperties appProperties;
    private final DeviceRepository deviceRepository;
    private final StatusReportRepository statusReportRepository;

    private static final int DEVICE_COUNT = 50;

    private static final DeviceType[] TYPES = DeviceType.values();

    private static final String[] LOCATIONS = {
            "Building A, Floor 1",
            "Building B, Floor 2",
            "Building C, Floor 1",
            "Building D, Floor 3",
            "Building E, Floor 2",
            "Building F, Floor 1",
            "Building G, Floor 3",
            "Building H, Floor 2",
            "Building I, Floor 1",
            "Building J, Floor 3",
    };

    private static final String[] TYPE_PREFIXES = {
            "CPE", "RTR", "SW", "AP", "FW", "ONT"
    };

    private static final String[] TYPE_NAMES = {
            "Customer Premises Equipment",
            "Core Router",
            "Distribution Switch",
            "Access Point",
            "Firewall",
            "Optical Network Terminal"
    };

    @Override
    @Transactional
    public void run(String... args) {
        if (!appProperties.getSeedData().isEnabled()) {
            log.debug("Data seeding is disabled. Set app.seed-data.enabled=true to enable.");
            return;
        }

        if (deviceRepository.count() > 0) {
            log.info("Database already contains {} device(s). Skipping seed.", deviceRepository.count());
            return;
        }

        log.info("Seeding {} deterministic devices for pagination demo...", DEVICE_COUNT);

        Instant now = Instant.now();

        for (int i = 0; i < DEVICE_COUNT; i++) {
            DeviceType type = TYPES[i % TYPES.length];
            String location = LOCATIONS[i % LOCATIONS.length];
            String uniqueId = String.format("%s-%03d", TYPE_PREFIXES[i % TYPE_PREFIXES.length], i + 1);
            String name = String.format("%s %d", TYPE_NAMES[i % TYPE_NAMES.length], i + 1);
            String hostname = String.format("%s-%03d.network.local", TYPE_PREFIXES[i % TYPE_PREFIXES.length].toLowerCase(), i + 1);
            String ipAddress = String.format("10.%d.%d.%d", (i / 256) % 256, (i / 16) % 16, (i % 256));

            // Registration spread over past 30 days (each device 12 hours apart)
            Instant registeredAt = now.minus(i * 12L, ChronoUnit.HOURS);

            Device device = Device.builder()
                    .uniqueId(uniqueId)
                    .name(name)
                    .deviceType(type)
                    .hostname(hostname)
                    .ipAddress(ipAddress)
                    .location(location)
                    .registeredAt(registeredAt)
                    .build();

            Device savedDevice = deviceRepository.save(device);

            seedReports(savedDevice, i, now);
        }

        log.info("Seeding complete. {} devices and {} status reports inserted.",
                deviceRepository.count(), statusReportRepository.count());
    }

    private void seedReports(Device device, int index, Instant now) {
        int scenario = index % 5;
        Instant latestReportAt = null;
        DeviceStatus latestStatus = null;

        switch (scenario) {
            case 0 -> {
                // 1 recent report (5 min ago) → ONLINE, not stale
                latestReportAt = now.minus(5, ChronoUnit.MINUTES);
                latestStatus = DeviceStatus.ONLINE;
                saveReport(device, latestReportAt, latestStatus, "All systems operational");
            }
            case 1 -> {
                // 2 reports: recent (10 min) + older (40 min) → ONLINE, not stale
                saveReport(device, now.minus(40, ChronoUnit.MINUTES), DeviceStatus.DEGRADED, "High latency detected");
                latestReportAt = now.minus(10, ChronoUnit.MINUTES);
                latestStatus = DeviceStatus.ONLINE;
                saveReport(device, latestReportAt, latestStatus, "Performance restored");
            }
            case 2 -> {
                // 3 reports: old (2h, 3h, 5h) → DEGRADED, stale
                saveReport(device, now.minus(5, ChronoUnit.HOURS), DeviceStatus.ONLINE, "Initial startup");
                saveReport(device, now.minus(3, ChronoUnit.HOURS), DeviceStatus.DEGRADED, "Packet loss detected");
                latestReportAt = now.minus(2, ChronoUnit.HOURS);
                latestStatus = DeviceStatus.DEGRADED;
                saveReport(device, latestReportAt, latestStatus, "Intermittent connectivity");
            }
            case 3 -> {
                // 1 report very old (24h) → OFFLINE, stale
                latestReportAt = now.minus(24, ChronoUnit.HOURS);
                latestStatus = DeviceStatus.OFFLINE;
                saveReport(device, latestReportAt, latestStatus, "Connection timeout");
            }
            case 4 -> {
                // No reports → OFFLINE, stale (by default)
                // nothing to save
            }
        }

        // Update denormalized columns on device
        if (latestReportAt != null) {
            device.setLastReportAt(latestReportAt);
            device.setCurrentStatus(latestStatus);
            deviceRepository.save(device);
        }
    }

    private void saveReport(Device device, Instant reportedAt, DeviceStatus status, String message) {
        StatusReport report = StatusReport.builder()
                .device(device)
                .reportedAt(reportedAt)
                .status(status)
                .message(message)
                .build();
        statusReportRepository.save(report);
    }
}
