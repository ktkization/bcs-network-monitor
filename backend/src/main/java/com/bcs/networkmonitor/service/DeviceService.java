package com.bcs.networkmonitor.service;

import com.bcs.networkmonitor.dto.DeviceDetailResponse;
import com.bcs.networkmonitor.dto.DeviceListItemResponse;
import com.bcs.networkmonitor.dto.DeviceRegistrationRequest;
import com.bcs.networkmonitor.entity.Device;
import org.springframework.data.domain.Page;

public interface DeviceService {
    Device registerDevice(DeviceRegistrationRequest request);
    Page<DeviceListItemResponse> listAllDevices(int page, int size, String sort);
    DeviceDetailResponse getDeviceDetail(Long id);
}
