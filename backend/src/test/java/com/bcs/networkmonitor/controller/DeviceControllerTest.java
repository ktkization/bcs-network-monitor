package com.bcs.networkmonitor.controller;

import com.bcs.networkmonitor.dto.DeviceDetailResponse;
import com.bcs.networkmonitor.dto.DeviceListItemResponse;
import com.bcs.networkmonitor.dto.DeviceRegistrationRequest;
import com.bcs.networkmonitor.dto.StatusReportRequest;
import com.bcs.networkmonitor.entity.DeviceStatus;
import com.bcs.networkmonitor.entity.DeviceType;
import com.bcs.networkmonitor.exception.DuplicateResourceException;
import com.bcs.networkmonitor.exception.ResourceNotFoundException;
import com.bcs.networkmonitor.service.DeviceService;
import com.bcs.networkmonitor.service.StatusReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DeviceService deviceService;

    @MockitoBean
    private StatusReportService statusReportService;

    @Test
    void registerDevice_shouldReturn201() throws Exception {
        DeviceRegistrationRequest request = new DeviceRegistrationRequest(
                "CPE-001", "Test CPE", DeviceType.CPE,
                "cpe-001.local", null, "Building A"
        );

        when(deviceService.registerDevice(any())).thenAnswer(inv -> {
            DeviceRegistrationRequest req = inv.getArgument(0);
            return com.bcs.networkmonitor.entity.Device.builder()
                    .id(1L)
                    .uniqueId(req.uniqueId())
                    .name(req.name())
                    .deviceType(req.deviceType())
                    .hostname(req.hostname())
                    .location(req.location())
                    .registeredAt(Instant.now())
                    .build();
        });

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uniqueId").value("CPE-001"))
                .andExpect(jsonPath("$.name").value("Test CPE"));
    }

    @Test
    void registerDevice_shouldReturn409OnDuplicate() throws Exception {
        DeviceRegistrationRequest request = new DeviceRegistrationRequest(
                "CPE-001", "Test CPE", DeviceType.CPE,
                "cpe-001.local", null, "Building A"
        );

        when(deviceService.registerDevice(any()))
                .thenThrow(new DuplicateResourceException("Device with uniqueId already exists"));

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void listDevices_shouldReturn200() throws Exception {
        DeviceListItemResponse item = new DeviceListItemResponse(
                1L, "CPE-001", "Test CPE", DeviceType.CPE,
                "cpe-001.local", "Building A",
                DeviceStatus.ONLINE, Instant.now(), false
        );

        Page<DeviceListItemResponse> page = new PageImpl<>(List.of(item));
        when(deviceService.listAllDevices(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uniqueId").value("CPE-001"))
                .andExpect(jsonPath("$.content[0].stale").value(false))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getDeviceDetail_shouldReturn200() throws Exception {
        DeviceDetailResponse detail = new DeviceDetailResponse(
                1L, "CPE-001", "Test CPE", DeviceType.CPE,
                "cpe-001.local", "192.168.1.1", "Building A",
                Instant.now(), DeviceStatus.ONLINE, Instant.now(), false,
                Collections.emptyList()
        );

        when(deviceService.getDeviceDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test CPE"))
                .andExpect(jsonPath("$.ipAddress").value("192.168.1.1"));
    }

    @Test
    void getDeviceDetail_shouldReturn404WhenNotFound() throws Exception {
        when(deviceService.getDeviceDetail(99L))
                .thenThrow(new ResourceNotFoundException("Device not found"));

        mockMvc.perform(get("/api/devices/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitReport_shouldReturn201() throws Exception {
        StatusReportRequest request = new StatusReportRequest(DeviceStatus.ONLINE, "OK");

        when(statusReportService.submitReport(eq(1L), any()))
                .thenReturn(com.bcs.networkmonitor.entity.StatusReport.builder()
                        .id(1L)
                        .reportedAt(Instant.now())
                        .status(DeviceStatus.ONLINE)
                        .message("OK")
                        .build());

        mockMvc.perform(post("/api/devices/1/status-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ONLINE"));
    }

    @Test
    void submitReport_shouldReturn404WhenDeviceNotFound() throws Exception {
        StatusReportRequest request = new StatusReportRequest(DeviceStatus.ONLINE, null);

        when(statusReportService.submitReport(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Device not found"));

        mockMvc.perform(post("/api/devices/99/status-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
