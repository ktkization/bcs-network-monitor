import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchDevices } from "@/api/devices";
import type { DeviceListItem } from "@/types";
import { StatusBadge } from "@/components/StatusBadge";
import { StaleIndicator } from "@/components/StaleIndicator";
import { formatDate } from "@/utils/stale";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export default function DeviceListPage() {
  const [devices, setDevices] = useState<DeviceListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    fetchDevices()
      .then((res) => setDevices(res.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="p-8 text-center text-muted-foreground">Loading devices...</div>;
  if (error) return <div className="p-8 text-center text-destructive">{error}</div>;

  return (
    <div className="mx-auto max-w-6xl p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Network Devices</h1>
        <Button onClick={() => navigate("/devices/register")}>Register Device</Button>
      </div>

      {devices.length === 0 ? (
        <div className="text-center text-muted-foreground py-12">No devices registered yet.</div>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Hostname</TableHead>
              <TableHead>Location</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Last Report</TableHead>
              <TableHead>Stale</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {devices.map((device) => (
              <TableRow
                key={device.id}
                className="cursor-pointer hover:bg-muted/50"
                onClick={() => navigate(`/devices/${device.id}`)}
              >
                <TableCell className="font-medium">{device.name}</TableCell>
                <TableCell>{device.deviceType}</TableCell>
                <TableCell className="font-mono text-sm">{device.hostname}</TableCell>
                <TableCell>{device.location}</TableCell>
                <TableCell>
                  <StatusBadge status={device.currentStatus} />
                </TableCell>
                <TableCell>{formatDate(device.lastReportTimestamp)}</TableCell>
                <TableCell>
                  <StaleIndicator stale={device.stale} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
