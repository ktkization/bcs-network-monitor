export const DeviceType = {
  CPE: "CPE",
  ROUTER: "ROUTER",
  SWITCH: "SWITCH",
  ACCESS_POINT: "ACCESS_POINT",
  FIREWALL: "FIREWALL",
  ONT: "ONT",
} as const;

export type DeviceType = (typeof DeviceType)[keyof typeof DeviceType];

export const DeviceStatus = {
  ONLINE: "ONLINE",
  OFFLINE: "OFFLINE",
  DEGRADED: "DEGRADED",
} as const;

export type DeviceStatus = (typeof DeviceStatus)[keyof typeof DeviceStatus];

export interface Device {
  id: number;
  uniqueId: string;
  name: string;
  deviceType: DeviceType;
  hostname: string;
  ipAddress?: string;
  location: string;
  registeredAt: string;
}

export interface DeviceListItem {
  id: number;
  uniqueId: string;
  name: string;
  deviceType: DeviceType;
  hostname: string;
  location: string;
  currentStatus: DeviceStatus;
  lastReportTimestamp: string | null;
  stale: boolean;
}

export interface StatusReport {
  id: number;
  reportedAt: string;
  status: DeviceStatus;
  message?: string;
}

export interface DeviceDetail extends Device {
  currentStatus: DeviceStatus;
  lastReportTimestamp: string | null;
  stale: boolean;
  recentReports: StatusReport[];
}

export interface DeviceRegistrationRequest {
  uniqueId: string;
  name: string;
  deviceType: DeviceType;
  hostname: string;
  ipAddress?: string;
  location: string;
}

export interface StatusReportRequest {
  status: DeviceStatus;
  message?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
