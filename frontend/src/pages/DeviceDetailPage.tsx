import { useEffect, useState, type ChangeEvent } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { fetchDeviceDetail, submitStatusReport } from "@/api/devices";
import type { DeviceDetail, StatusReport as StatusReportType } from "@/types";
import { DeviceStatus } from "@/types";
import { StatusBadge } from "@/components/StatusBadge";
import { StaleIndicator } from "@/components/StaleIndicator";
import { formatDate } from "@/utils/stale";
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
import { Separator } from "@/components/ui/separator";

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

  if (loading) return <div className="p-8 text-center text-muted-foreground">Loading device...</div>;
  if (error) return <div className="p-8 text-center text-destructive">{error}</div>;
  if (!device) return <div className="p-8 text-center text-muted-foreground">Device not found.</div>;

  return (
    <div className="mx-auto max-w-6xl p-6 space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="outline" onClick={() => navigate("/devices")}>
          Back
        </Button>
        <h1 className="text-2xl font-bold">{device.name}</h1>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>Device Information</CardTitle>
          <div className="flex items-center gap-2">
            <StatusBadge status={device.currentStatus} />
            <StaleIndicator stale={device.stale} />
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
            <div>
              <div className="text-muted-foreground">Unique ID</div>
              <div className="font-mono">{device.uniqueId}</div>
            </div>
            <div>
              <div className="text-muted-foreground">Type</div>
              <div>{device.deviceType}</div>
            </div>
            <div>
              <div className="text-muted-foreground">Hostname</div>
              <div className="font-mono">{device.hostname}</div>
            </div>
            <div>
              <div className="text-muted-foreground">IP Address</div>
              <div className="font-mono">{device.ipAddress || "N/A"}</div>
            </div>
            <div>
              <div className="text-muted-foreground">Location</div>
              <div>{device.location}</div>
            </div>
            <div>
              <div className="text-muted-foreground">Registered</div>
              <div>{formatDate(device.registeredAt)}</div>
            </div>
            <div>
              <div className="text-muted-foreground">Last Report</div>
              <div>{formatDate(device.lastReportTimestamp)}</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Recent Status Reports</h2>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger render={<Button />}>
            Submit Status Report
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
      </div>

      <Separator />

      {device.recentReports.length === 0 ? (
        <div className="text-center text-muted-foreground py-8">No status reports yet.</div>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Timestamp</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Message</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {device.recentReports.map((report: StatusReportType) => (
              <TableRow key={report.id}>
                <TableCell>{formatDate(report.reportedAt)}</TableCell>
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
    </div>
  );
}
