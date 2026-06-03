package com.bcs.networkmonitor.controller;

import com.bcs.networkmonitor.TestcontainersConfiguration;
import com.bcs.networkmonitor.dto.DeviceRegistrationRequest;
import com.bcs.networkmonitor.dto.StatusReportRequest;
import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class DeviceControllerE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullDeviceLifecycle() {
        // 1. Register a device
        DeviceRegistrationRequest registration = new DeviceRegistrationRequest(
                "CPE-E2E-001", "E2E Test CPE", DeviceType.CPE,
                "cpe-e2e.local", null, "Building E2E"
        );

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                "/api/devices", registration, Map.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().get("uniqueId")).isEqualTo("CPE-E2E-001");
        Number deviceIdNum = (Number) registerResponse.getBody().get("id");
        Long deviceId = deviceIdNum.longValue();

        // 2. List devices - should contain the new device with stale=true and OFFLINE
        ResponseEntity<Map> listResponse = restTemplate.getForEntity("/api/devices", Map.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> page = listResponse.getBody();
        assertThat(page).isNotNull();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> devices = (List<Map<String, Object>>) page.get("content");
        assertThat(devices).isNotEmpty();

        Map<String, Object> listedDevice = devices.stream()
                .filter(d -> "CPE-E2E-001".equals(d.get("uniqueId")))
                .findFirst()
                .orElseThrow();
        assertThat(listedDevice.get("currentStatus")).isEqualTo("OFFLINE");
        assertThat(listedDevice.get("stale")).isEqualTo(true);

        // 3. Get device detail - should have empty recentReports
        ResponseEntity<Map> detailResponse = restTemplate.getForEntity(
                "/api/devices/{id}", Map.class, deviceId);
        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> detail = detailResponse.getBody();
        assertThat(detail.get("uniqueId")).isEqualTo("CPE-E2E-001");
        assertThat(detail.get("recentReports")).asList().isEmpty();

        // 4. Submit a status report
        StatusReportRequest reportRequest = new StatusReportRequest(DeviceStatus.ONLINE, "E2E OK");
        ResponseEntity<Map> reportResponse = restTemplate.postForEntity(
                "/api/devices/{id}/status-reports", reportRequest, Map.class, deviceId);
        assertThat(reportResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reportResponse.getBody().get("status")).isEqualTo("ONLINE");

        // 5. Get device detail again - should be ONLINE and not stale
        ResponseEntity<Map> detailAfterReport = restTemplate.getForEntity(
                "/api/devices/{id}", Map.class, deviceId);
        assertThat(detailAfterReport.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> detail2 = detailAfterReport.getBody();
        assertThat(detail2.get("currentStatus")).isEqualTo("ONLINE");
        assertThat(detail2.get("stale")).isEqualTo(false);
        assertThat(detail2.get("recentReports")).asList().hasSize(1);
    }

    @Test
    void submitReportForNonExistentDevice_shouldReturn404() {
        StatusReportRequest reportRequest = new StatusReportRequest(DeviceStatus.ONLINE, "Should fail");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/devices/99999/status-reports", reportRequest, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void registerDuplicateDevice_shouldReturn409() {
        DeviceRegistrationRequest registration = new DeviceRegistrationRequest(
                "CPE-DUP-001", "Duplicate Test", DeviceType.CPE,
                "dup.local", null, "Building DUP"
        );

        ResponseEntity<Map> firstResponse = restTemplate.postForEntity("/api/devices", registration, Map.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> secondResponse = restTemplate.postForEntity("/api/devices", registration, Map.class);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
