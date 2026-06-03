import { describe, it, expect } from "vitest";
import { formatDate, statusColor } from "./stale";

describe("formatDate", () => {
  it('returns "Never" for null', () => {
    expect(formatDate(null)).toBe("Never");
  });

  it("returns a locale string for a valid ISO date", () => {
    const iso = "2024-01-15T10:30:00.000Z";
    const result = formatDate(iso);
    expect(result).not.toBe("Never");
    expect(typeof result).toBe("string");
    expect(result.length).toBeGreaterThan(0);
  });
});

describe("statusColor", () => {
  it("returns green classes for ONLINE", () => {
    expect(statusColor("ONLINE")).toContain("green");
  });

  it("returns red classes for OFFLINE", () => {
    expect(statusColor("OFFLINE")).toContain("red");
  });

  it("returns yellow classes for DEGRADED", () => {
    expect(statusColor("DEGRADED")).toContain("yellow");
  });

  it("returns gray classes for unknown status", () => {
    expect(statusColor("UNKNOWN")).toContain("gray");
  });
});
