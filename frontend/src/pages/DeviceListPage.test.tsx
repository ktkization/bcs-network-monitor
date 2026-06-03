import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import DeviceListPage from "./DeviceListPage";
import * as devicesApi from "@/api/devices";

const mockedNavigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockedNavigate,
  };
});

vi.mock("@/api/devices", () => ({
  fetchDevices: vi.fn(),
}));

function mockPage(content: any[]) {
  return {
    data: {
      content,
      totalElements: content.length,
      totalPages: 1,
      number: 0,
      size: 20,
      first: true,
      last: true,
      empty: content.length === 0,
    },
  };
}

describe("DeviceListPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("shows loading state initially", () => {
    vi.mocked(devicesApi.fetchDevices).mockReturnValue(new Promise(() => {}));
    render(<DeviceListPage />);
    expect(screen.getByRole("status", { name: /loading devices/i })).toBeInTheDocument();
  });

  it("renders empty state when no devices", async () => {
    vi.mocked(devicesApi.fetchDevices).mockResolvedValueOnce(mockPage([]) as any);
    render(<DeviceListPage />);
    await waitFor(() => {
      expect(screen.getByText(/no devices registered yet/i)).toBeInTheDocument();
    });
  });

  it("renders device list with status and stale indicators", async () => {
    const devices = [
      {
        id: 1,
        uniqueId: "CPE-001",
        name: "Test CPE",
        deviceType: "CPE",
        hostname: "cpe-001.local",
        location: "Building A",
        currentStatus: "ONLINE",
        lastReportTimestamp: new Date().toISOString(),
        stale: false,
      },
      {
        id: 2,
        uniqueId: "RTR-001",
        name: "Old Router",
        deviceType: "ROUTER",
        hostname: "rtr-001.local",
        location: "Building B",
        currentStatus: "OFFLINE",
        lastReportTimestamp: null,
        stale: true,
      },
    ];
    vi.mocked(devicesApi.fetchDevices).mockResolvedValueOnce(mockPage(devices) as any);
    render(<DeviceListPage />);

    await waitFor(() => {
      expect(screen.getByText("Test CPE")).toBeInTheDocument();
    });

    expect(screen.getByText("Old Router")).toBeInTheDocument();
    expect(screen.getByText("ONLINE")).toBeInTheDocument();
    expect(screen.getByText("OFFLINE")).toBeInTheDocument();
    expect(screen.getByText("STALE")).toBeInTheDocument();
  });

  it("navigates to device detail on row click", async () => {
    const devices = [
      {
        id: 1,
        uniqueId: "CPE-001",
        name: "Test CPE",
        deviceType: "CPE",
        hostname: "cpe-001.local",
        location: "Building A",
        currentStatus: "ONLINE",
        lastReportTimestamp: new Date().toISOString(),
        stale: false,
      },
    ];
    vi.mocked(devicesApi.fetchDevices).mockResolvedValueOnce(mockPage(devices) as any);
    render(<DeviceListPage />);

    await waitFor(() => {
      expect(screen.getByText("Test CPE")).toBeInTheDocument();
    });

    const row = screen.getByText("Test CPE").closest("tr");
    expect(row).toBeTruthy();
    await userEvent.click(row!);
    expect(mockedNavigate).toHaveBeenCalledWith("/devices/1");
  });

  it("navigates to register page when Register Device button is clicked", async () => {
    vi.mocked(devicesApi.fetchDevices).mockResolvedValueOnce(mockPage([]) as any);
    render(<DeviceListPage />);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: /register/i })).toBeInTheDocument();
    });

    await userEvent.click(screen.getByRole("button", { name: /register/i }));
    expect(mockedNavigate).toHaveBeenCalledWith("/devices/register");
  });

  it("renders pagination controls when multiple pages", async () => {
    vi.mocked(devicesApi.fetchDevices).mockResolvedValueOnce({
      data: {
        content: [
          {
            id: 1,
            uniqueId: "CPE-001",
            name: "Test CPE",
            deviceType: "CPE",
            hostname: "cpe-001.local",
            location: "Building A",
            currentStatus: "ONLINE",
            lastReportTimestamp: new Date().toISOString(),
            stale: false,
          },
        ],
        totalElements: 25,
        totalPages: 2,
        number: 0,
        size: 20,
        first: true,
        last: false,
        empty: false,
      },
    } as any);
    render(<DeviceListPage />);

    await waitFor(() => {
      expect(screen.getByText(/page 1 of 2/i)).toBeInTheDocument();
    });

    expect(screen.getByRole("button", { name: /previous/i })).toBeDisabled();
    expect(screen.getByRole("button", { name: /next/i })).toBeEnabled();
  });

  it("navigates to next page on Next click", async () => {
    vi.mocked(devicesApi.fetchDevices).mockResolvedValueOnce({
      data: {
        content: [
          {
            id: 1,
            uniqueId: "CPE-001",
            name: "Test CPE",
            deviceType: "CPE",
            hostname: "cpe-001.local",
            location: "Building A",
            currentStatus: "ONLINE",
            lastReportTimestamp: new Date().toISOString(),
            stale: false,
          },
        ],
        totalElements: 25,
        totalPages: 2,
        number: 0,
        size: 20,
        first: true,
        last: false,
        empty: false,
      },
    } as any);

    vi.mocked(devicesApi.fetchDevices).mockResolvedValueOnce({
      data: {
        content: [
          {
            id: 2,
            uniqueId: "RTR-001",
            name: "Test Router",
            deviceType: "ROUTER",
            hostname: "rtr-001.local",
            location: "Building B",
            currentStatus: "ONLINE",
            lastReportTimestamp: new Date().toISOString(),
            stale: false,
          },
        ],
        totalElements: 25,
        totalPages: 2,
        number: 1,
        size: 20,
        first: false,
        last: true,
        empty: false,
      },
    } as any);

    render(<DeviceListPage />);

    await waitFor(() => {
      expect(screen.getByText(/page 1 of 2/i)).toBeInTheDocument();
    });

    await userEvent.click(screen.getByRole("button", { name: /next/i }));

    await waitFor(() => {
      expect(screen.getByText(/page 2 of 2/i)).toBeInTheDocument();
    });

    expect(devicesApi.fetchDevices).toHaveBeenLastCalledWith({ page: 1, size: 20 });
  });
});
