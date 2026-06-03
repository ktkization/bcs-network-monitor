import { describe, it, expect, vi } from "vitest";
import { registerDevice, fetchDevices, fetchDeviceDetail, submitStatusReport } from "./devices";
import api from "./client";

vi.mock("./client", () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

describe("devices API", () => {
  it("registerDevice calls POST /devices with data", async () => {
    const data = {
      uniqueId: "CPE-001",
      name: "Test",
      deviceType: "CPE" as const,
      hostname: "test.local",
      location: "Building A",
    };
    const mockResponse = { data: { id: 1, ...data, registeredAt: "2024-01-01T00:00:00Z" } };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const result = await registerDevice(data);

    expect(api.post).toHaveBeenCalledWith("/devices", data);
    expect(result.data.id).toBe(1);
  });

  it("fetchDevices calls GET /devices", async () => {
    const mockResponse = { data: [] };
    vi.mocked(api.get).mockResolvedValueOnce(mockResponse);

    const result = await fetchDevices();

    expect(api.get).toHaveBeenCalledWith("/devices");
    expect(result.data).toEqual([]);
  });

  it("fetchDeviceDetail calls GET /devices/:id", async () => {
    const mockResponse = { data: { id: 1, name: "Test" } };
    vi.mocked(api.get).mockResolvedValueOnce(mockResponse);

    const result = await fetchDeviceDetail(1);

    expect(api.get).toHaveBeenCalledWith("/devices/1");
    expect(result.data.id).toBe(1);
  });

  it("submitStatusReport calls POST /devices/:id/status-reports", async () => {
    const mockResponse = { data: { id: 1, status: "ONLINE" } };
    vi.mocked(api.post).mockResolvedValueOnce(mockResponse);

    const result = await submitStatusReport(1, { status: "ONLINE", message: "OK" });

    expect(api.post).toHaveBeenCalledWith("/devices/1/status-reports", { status: "ONLINE", message: "OK" });
    expect(result.data.status).toBe("ONLINE");
  });
});
