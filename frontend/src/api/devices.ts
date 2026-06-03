import api from "./client";
import type {
  DeviceRegistrationRequest,
  Device,
  DeviceListItem,
  DeviceDetail,
  StatusReportRequest,
  StatusReport,
  Page,
} from "@/types";

export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

export const registerDevice = (data: DeviceRegistrationRequest) =>
  api.post<Device>("/devices", data);

export const fetchDevices = (params?: PaginationParams) =>
  api.get<Page<DeviceListItem>>("/devices", { params });

export const fetchDeviceDetail = (id: number) =>
  api.get<DeviceDetail>(`/devices/${id}`);

export const submitStatusReport = (deviceId: number, data: StatusReportRequest) =>
  api.post<StatusReport>(`/devices/${deviceId}/status-reports`, data);
