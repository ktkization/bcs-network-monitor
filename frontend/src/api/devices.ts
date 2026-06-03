import api from "./client";
import type {
  DeviceRegistrationRequest,
  Device,
  DeviceListItem,
  DeviceDetail,
  StatusReportRequest,
  StatusReport,
} from "@/types";

export const registerDevice = (data: DeviceRegistrationRequest) =>
  api.post<Device>("/devices", data);

export const fetchDevices = () =>
  api.get<DeviceListItem[]>("/devices");

export const fetchDeviceDetail = (id: number) =>
  api.get<DeviceDetail>(`/devices/${id}`);

export const submitStatusReport = (deviceId: number, data: StatusReportRequest) =>
  api.post<StatusReport>(`/devices/${deviceId}/status-reports`, data);
