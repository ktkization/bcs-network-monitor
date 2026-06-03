import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RegisterDevicePage from "./RegisterDevicePage";
import * as devicesApi from "@/api/devices";

const mockedNavigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockedNavigate,
    Link: ({ to, children, ...props }: any) => <a href={to} {...props}>{children}</a>,
  };
});

vi.mock("@/api/devices", () => ({
  registerDevice: vi.fn(),
}));

describe("RegisterDevicePage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows validation error when required fields are empty", async () => {
    render(<RegisterDevicePage />);
    await userEvent.click(screen.getByRole("button", { name: /register device/i }));

    await waitFor(() => {
      expect(screen.getByText(/please fill in all required fields/i)).toBeInTheDocument();
    });
  });

  it("registers a device and navigates on success", async () => {
    vi.mocked(devicesApi.registerDevice).mockResolvedValueOnce({
      data: { id: 1, uniqueId: "CPE-001", name: "Test", registeredAt: "2024-01-01T00:00:00Z" },
    } as any);

    render(<RegisterDevicePage />);

    await userEvent.type(screen.getByLabelText(/unique id/i), "CPE-001");
    await userEvent.type(screen.getByRole("textbox", { name: /^Name/i }), "Test CPE");

    const typeSelect = screen.getByRole("combobox");
    await userEvent.click(typeSelect);
    const cpeOption = await screen.findByRole("option", { name: "CPE" });
    await userEvent.click(cpeOption);

    await userEvent.type(screen.getByLabelText(/hostname/i), "cpe-001.local");
    await userEvent.type(screen.getByLabelText(/location/i), "Building A");

    await userEvent.click(screen.getByRole("button", { name: /register device/i }));

    await waitFor(() => {
      expect(devicesApi.registerDevice).toHaveBeenCalledWith({
        uniqueId: "CPE-001",
        name: "Test CPE",
        deviceType: "CPE",
        hostname: "cpe-001.local",
        location: "Building A",
        ipAddress: undefined,
      });
    });

    await waitFor(() => {
      expect(mockedNavigate).toHaveBeenCalledWith("/devices");
    });
  });

  it("displays error message on registration failure", async () => {
    vi.mocked(devicesApi.registerDevice).mockRejectedValueOnce(new Error("Duplicate uniqueId"));

    render(<RegisterDevicePage />);

    await userEvent.type(screen.getByLabelText(/unique id/i), "CPE-001");
    await userEvent.type(screen.getByRole("textbox", { name: /^Name/i }), "Test CPE");

    const typeSelect = screen.getByRole("combobox");
    await userEvent.click(typeSelect);
    const cpeOption = await screen.findByRole("option", { name: "CPE" });
    await userEvent.click(cpeOption);

    await userEvent.type(screen.getByLabelText(/hostname/i), "cpe-001.local");
    await userEvent.type(screen.getByLabelText(/location/i), "Building A");

    await userEvent.click(screen.getByRole("button", { name: /register device/i }));

    await waitFor(() => {
      expect(screen.getByText(/duplicate uniqueid/i)).toBeInTheDocument();
    });
  });

  it("navigates away on cancel", async () => {
    render(<RegisterDevicePage />);
    await userEvent.click(screen.getByRole("button", { name: /cancel/i }));
    expect(mockedNavigate).toHaveBeenCalledWith("/devices");
  });
});
