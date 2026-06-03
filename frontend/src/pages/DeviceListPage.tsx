import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Loader2, AlertCircle, PlusCircle } from "lucide-react";
import { fetchDevices } from "@/api/devices";
import type { DeviceListItem } from "@/types";
import { StatusBadge } from "@/components/StatusBadge";
import { StaleIndicator } from "@/components/StaleIndicator";
import { getRelativeTime } from "@/utils/stale";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

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

  if (loading) {
    return (
      <div className="mx-auto flex max-w-5xl items-center justify-center py-24" role="status" aria-label="Loading devices">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-8">
        <div className="flex items-center gap-3 rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-destructive">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <p className="text-sm">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Network Devices</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          {devices.length} device{devices.length !== 1 ? "s" : ""} monitored
        </p>
      </div>

      {devices.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-xl border border-dashed py-16 text-center">
          <Loader2 className="mb-4 h-10 w-10 text-muted-foreground/50" />
          <h3 className="text-lg font-semibold">No devices registered yet</h3>
          <p className="mt-1 max-w-sm text-sm text-muted-foreground">
            Get started by registering your first network device.
          </p>
          <Button className="mt-6" onClick={() => navigate("/devices/register")}>
            <PlusCircle className="mr-1.5 h-4 w-4" />
            Register your first device
          </Button>
        </div>
      ) : (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-base font-semibold">All Devices</CardTitle>
            <Button size="sm" onClick={() => navigate("/devices/register")}>
              <PlusCircle className="mr-1.5 h-4 w-4" />
              Register Device
            </Button>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
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
                    className="cursor-pointer"
                    onClick={() => navigate(`/devices/${device.id}`)}
                  >
                    <TableCell className="font-medium">{device.name}</TableCell>
                    <TableCell>{device.deviceType}</TableCell>
                    <TableCell className="font-mono text-sm">{device.hostname}</TableCell>
                    <TableCell>{device.location}</TableCell>
                    <TableCell>
                      <StatusBadge status={device.currentStatus} />
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {getRelativeTime(device.lastReportTimestamp)}
                    </TableCell>
                    <TableCell>
                      <StaleIndicator stale={device.stale} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
