import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import DeviceDetailPage from "./DeviceDetailPage";
import * as devicesApi from "@/api/devices";

const mockedNavigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useParams: () => ({ id: "1" }),
    useNavigate: () => mockedNavigate,
    Link: ({ to, children, ...props }: any) => <a href={to} {...props}>{children}</a>,
  };
});

vi.mock("@/api/devices", () => ({
  fetchDeviceDetail: vi.fn(),
  submitStatusReport: vi.fn(),
}));

describe("DeviceDetailPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("shows loading state initially", () => {
    vi.mocked(devicesApi.fetchDeviceDetail).mockReturnValue(new Promise(() => {}));
    render(<DeviceDetailPage />);
    expect(screen.getByRole("status", { name: /loading device/i })).toBeInTheDocument();
  });

  it("renders device information and recent reports", async () => {
    const deviceDetail = {
      id: 1,
      uniqueId: "CPE-001",
      name: "Test CPE",
      deviceType: "CPE",
      hostname: "cpe-001.local",
      ipAddress: "192.168.1.1",
      location: "Building A",
      registeredAt: "2024-01-01T00:00:00Z",
      currentStatus: "ONLINE",
      lastReportTimestamp: new Date().toISOString(),
      stale: false,
      recentReports: [
        {
          id: 1,
          reportedAt: new Date().toISOString(),
          status: "ONLINE",
          message: "All good",
        },
      ],
    };
    vi.mocked(devicesApi.fetchDeviceDetail).mockResolvedValueOnce({ data: deviceDetail } as any);
    render(<DeviceDetailPage />);

    await waitFor(() => {
      expect(screen.getByText("CPE-001")).toBeInTheDocument();
    });

    expect(screen.getByText("192.168.1.1")).toBeInTheDocument();
    expect(screen.getByText("Building A")).toBeInTheDocument();
    expect(screen.getByText("All good")).toBeInTheDocument();
    expect(screen.queryByText("STALE")).not.toBeInTheDocument();
  });

  it("shows empty reports message when no reports exist", async () => {
    const deviceDetail = {
      id: 1,
      uniqueId: "CPE-001",
      name: "Test CPE",
      deviceType: "CPE",
      hostname: "cpe-001.local",
      location: "Building A",
      registeredAt: "2024-01-01T00:00:00Z",
      currentStatus: "OFFLINE",
      lastReportTimestamp: null,
      stale: true,
      recentReports: [],
    };
    vi.mocked(devicesApi.fetchDeviceDetail).mockResolvedValueOnce({ data: deviceDetail } as any);
    render(<DeviceDetailPage />);

    await waitFor(() => {
      expect(screen.getByText(/no status reports yet/i)).toBeInTheDocument();
    });
  });

  it("submits a status report and refreshes device data", async () => {
    const deviceDetail = {
      id: 1,
      uniqueId: "CPE-001",
      name: "Test CPE",
      deviceType: "CPE",
      hostname: "cpe-001.local",
      location: "Building A",
      registeredAt: "2024-01-01T00:00:00Z",
      currentStatus: "OFFLINE",
      lastReportTimestamp: null,
      stale: true,
      recentReports: [],
    };

    vi.mocked(devicesApi.fetchDeviceDetail)
      .mockResolvedValueOnce({ data: deviceDetail } as any)
      .mockResolvedValueOnce({
        data: {
          ...deviceDetail,
          currentStatus: "ONLINE",
          lastReportTimestamp: new Date().toISOString(),
          stale: false,
          recentReports: [
            { id: 2, reportedAt: new Date().toISOString(), status: "ONLINE", message: "Back up" },
          ],
        },
      } as any);

    vi.mocked(devicesApi.submitStatusReport).mockResolvedValueOnce({ data: { id: 2 } } as any);

    render(<DeviceDetailPage />);

    await waitFor(() => {
      expect(screen.getByText("CPE-001")).toBeInTheDocument();
    });

    await userEvent.click(screen.getAllByRole("button", { name: /submit report/i })[0]);

    const dialog = await screen.findByRole("dialog");
    expect(dialog).toBeInTheDocument();

    const selectTrigger = within(dialog).getByRole("combobox");
    await userEvent.click(selectTrigger);

    const onlineOption = await screen.findByRole("option", { name: "ONLINE" });
    await userEvent.click(onlineOption);

    await userEvent.click(within(dialog).getByRole("button", { name: /submit/i }));

    await waitFor(() => {
      expect(devicesApi.submitStatusReport).toHaveBeenCalledWith(1, { status: "ONLINE", message: undefined });
    });

    await waitFor(() => {
      expect(devicesApi.fetchDeviceDetail).toHaveBeenCalledTimes(2);
    });
  });

  it("shows breadcrumb with link back to devices", async () => {
    const deviceDetail = {
      id: 1,
      uniqueId: "CPE-001",
      name: "Test CPE",
      deviceType: "CPE",
      hostname: "cpe-001.local",
      location: "Building A",
      registeredAt: "2024-01-01T00:00:00Z",
      currentStatus: "ONLINE",
      lastReportTimestamp: new Date().toISOString(),
      stale: false,
      recentReports: [],
    };
    vi.mocked(devicesApi.fetchDeviceDetail).mockResolvedValueOnce({ data: deviceDetail } as any);
    render(<DeviceDetailPage />);

    await waitFor(() => {
      expect(screen.getByText("CPE-001")).toBeInTheDocument();
    });

    const breadcrumbLink = screen.getByRole("link", { name: /devices/i });
    expect(breadcrumbLink).toBeInTheDocument();
    expect(breadcrumbLink).toHaveAttribute("href", "/devices");
  });
});
