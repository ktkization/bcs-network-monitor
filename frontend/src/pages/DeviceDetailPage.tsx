import { useEffect, useState, type ChangeEvent } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import { Loader2, AlertCircle, ClipboardList, PlusCircle } from "lucide-react";
import { fetchDeviceDetail, submitStatusReport } from "@/api/devices";
import type { DeviceDetail, StatusReport as StatusReportType } from "@/types";
import { DeviceStatus } from "@/types";
import { StatusBadge } from "@/components/StatusBadge";
import { StaleIndicator } from "@/components/StaleIndicator";
import { formatDate, getRelativeTime } from "@/utils/stale";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const statusAccent: Record<DeviceStatus, string> = {
  ONLINE: "border-t-green-500",
  OFFLINE: "border-t-red-500",
  DEGRADED: "border-t-yellow-500",
};

export default function DeviceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [device, setDevice] = useState<DeviceDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [reportForm, setReportForm] = useState({
    status: "" as DeviceStatus | "",
    message: "",
  });
  const [submitting, setSubmitting] = useState(false);

  const loadDevice = () => {
    if (!id) return;
    setLoading(true);
    fetchDeviceDetail(Number(id))
      .then((res) => setDevice(res.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadDevice();
  }, [id]);

  const handleSubmitReport = async () => {
    if (!id || !reportForm.status) return;
    setSubmitting(true);
    try {
      await submitStatusReport(Number(id), {
        status: reportForm.status as DeviceStatus,
        message: reportForm.message || undefined,
      });
      setDialogOpen(false);
      setReportForm({ status: "", message: "" });
      loadDevice();
    } catch {
      setError("Failed to submit report");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="mx-auto flex max-w-5xl items-center justify-center py-24" role="status" aria-label="Loading device">
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

  if (!device) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-8">
        <div className="flex items-center gap-3 rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-destructive">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <p className="text-sm">Device not found.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 space-y-6">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-muted-foreground">
        <Link to="/devices" className="hover:text-foreground transition-colors">
          Devices
        </Link>
        <span>/</span>
        <span className="font-medium text-foreground">{device.name}</span>
      </nav>

      {/* Device Info Card */}
      <Card className={`border-t-4 ${statusAccent[device.currentStatus]}`}>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-2xl font-bold tracking-tight">{device.name}</CardTitle>
            <p className="mt-1 text-sm text-muted-foreground font-mono">{device.uniqueId}</p>
          </div>
          <div className="flex items-center gap-2">
            <StatusBadge status={device.currentStatus} />
            <StaleIndicator stale={device.stale} />
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-y-6 gap-x-8 text-sm">
            <div>
              <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Type</div>
              <div className="mt-1">{device.deviceType}</div>
            </div>
            <div>
              <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Hostname</div>
              <div className="mt-1 font-mono">{device.hostname}</div>
            </div>
            <div>
              <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground">IP Address</div>
              <div className="mt-1 font-mono">{device.ipAddress || "N/A"}</div>
            </div>
            <div>
              <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Location</div>
              <div className="mt-1">{device.location}</div>
            </div>
            <div>
              <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Registered</div>
              <div className="mt-1">{formatDate(device.registeredAt)}</div>
            </div>
            <div>
              <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground">Last Report</div>
              <div className="mt-1">{getRelativeTime(device.lastReportTimestamp)}</div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Reports Section */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-base font-semibold">Recent Status Reports</CardTitle>
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger render={<Button size="sm" />}>
              <PlusCircle className="mr-1.5 h-4 w-4" />
              Submit Report
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Submit Status Report</DialogTitle>
              </DialogHeader>
              <div className="space-y-4 pt-2">
                <div className="space-y-2">
                  <Label>Status *</Label>
                  <Select
                    value={reportForm.status}
                    onValueChange={(v: string | null) => {
                      if (v) setReportForm({ ...reportForm, status: v as DeviceStatus });
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select status" />
                    </SelectTrigger>
                    <SelectContent>
                      {Object.values(DeviceStatus).map((s) => (
                        <SelectItem key={s} value={s}>
                          {s}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="message">Message</Label>
                  <Textarea
                    id="message"
                    value={reportForm.message}
                    onChange={(e: ChangeEvent<HTMLTextAreaElement>) =>
                      setReportForm({ ...reportForm, message: e.target.value })
                    }
                    placeholder="Optional message..."
                    rows={3}
                  />
                </div>
                <Button onClick={handleSubmitReport} disabled={submitting || !reportForm.status}>
                  {submitting ? "Submitting..." : "Submit"}
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </CardHeader>
        <CardContent className="p-0">
          {device.recentReports.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <ClipboardList className="mb-3 h-10 w-10 text-muted-foreground/50" />
              <h3 className="text-base font-semibold">No status reports yet</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Submit your first report to track this device&apos;s health.
              </p>
              <Button
                className="mt-4"
                size="sm"
                variant="outline"
                onClick={() => setDialogOpen(true)}
              >
                <PlusCircle className="mr-1.5 h-4 w-4" />
                Submit Report
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  <TableHead>Timestamp</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Message</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {device.recentReports.map((report: StatusReportType) => (
                  <TableRow key={report.id}>
                    <TableCell className="text-muted-foreground">
                      {formatDate(report.reportedAt)}
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={report.status} />
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {report.message || "—"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
