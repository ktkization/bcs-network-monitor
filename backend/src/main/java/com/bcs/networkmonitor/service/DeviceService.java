package com.bcs.networkmonitor.service;

import com.bcs.networkmonitor.dto.DeviceDetailResponse;
import com.bcs.networkmonitor.dto.DeviceListItemResponse;
import com.bcs.networkmonitor.dto.DeviceRegistrationRequest;
import com.bcs.networkmonitor.entity.Device;

import java.util.List;

public interface DeviceService {
    Device registerDevice(DeviceRegistrationRequest request);
    List<DeviceListItemResponse> listAllDevices();
    DeviceDetailResponse getDeviceDetail(Long id);
}
