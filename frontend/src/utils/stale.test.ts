import { describe, it, expect, vi } from "vitest";
import { formatDate, statusColor, getRelativeTime } from "./stale";

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

describe("getRelativeTime", () => {
  it('returns "Never" for null', () => {
    expect(getRelativeTime(null)).toBe("Never");
  });

  it('returns "Just now" for recent timestamps', () => {
    const now = new Date().toISOString();
    expect(getRelativeTime(now)).toBe("Just now");
  });

  it('returns seconds for very recent timestamps', () => {
    const fifteenSecAgo = new Date(Date.now() - 15 * 1000).toISOString();
    expect(getRelativeTime(fifteenSecAgo)).toBe("15 sec ago");
  });

  it('returns minutes for recent timestamps', () => {
    const fiveMinAgo = new Date(Date.now() - 5 * 60 * 1000).toISOString();
    expect(getRelativeTime(fiveMinAgo)).toBe("5 min ago");
  });

  it('returns hours for timestamps within 24h', () => {
    const twoHoursAgo = new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString();
    expect(getRelativeTime(twoHoursAgo)).toBe("2 hours ago");
  });

  it('returns days for timestamps within a week', () => {
    const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString();
    expect(getRelativeTime(threeDaysAgo)).toBe("3 days ago");
  });

  it('returns full date for timestamps older than a week', () => {
    const oldDate = "2024-01-15T10:30:00.000Z";
    const result = getRelativeTime(oldDate);
    expect(result).not.toBe("Never");
    expect(result).not.toContain("ago");
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
