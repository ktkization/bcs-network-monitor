import { useState, type FormEvent, type ChangeEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { PlusCircle } from "lucide-react";
import { registerDevice } from "@/api/devices";
import { DeviceType } from "@/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const deviceTypes = Object.values(DeviceType);

export default function RegisterDevicePage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState({
    uniqueId: "",
    name: "",
    deviceType: "" as DeviceType | "",
    hostname: "",
    ipAddress: "",
    location: "",
  });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!form.uniqueId || !form.name || !form.deviceType || !form.hostname || !form.location) {
      setError("Please fill in all required fields.");
      return;
    }
    setLoading(true);
    try {
      await registerDevice({
        uniqueId: form.uniqueId,
        name: form.name,
        deviceType: form.deviceType as DeviceType,
        hostname: form.hostname,
        ipAddress: form.ipAddress || undefined,
        location: form.location,
      });
      navigate("/devices");
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Registration failed";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 space-y-6">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-muted-foreground">
        <Link to="/devices" className="hover:text-foreground transition-colors">
          Devices
        </Link>
        <span>/</span>
        <span className="font-medium text-foreground">Register New Device</span>
      </nav>

      <div>
        <h1 className="text-3xl font-bold tracking-tight">Register New Device</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Add a new network device to the monitoring system.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base font-semibold">
            <PlusCircle className="h-5 w-5 text-primary" />
            Device Information
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="uniqueId">Unique ID *</Label>
                <Input
                  id="uniqueId"
                  value={form.uniqueId}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => setForm({ ...form, uniqueId: e.target.value })}
                  placeholder="e.g. CPE-001"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="name">Name *</Label>
                <Input
                  id="name"
                  value={form.name}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => setForm({ ...form, name: e.target.value })}
                  placeholder="e.g. Customer Premises Equipment 1"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="deviceType">Device Type *</Label>
                <Select
                  value={form.deviceType}
                  onValueChange={(v: string | null) => { if (v) setForm({ ...form, deviceType: v as DeviceType }) }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select type" />
                  </SelectTrigger>
                  <SelectContent>
                    {deviceTypes.map((t) => (
                      <SelectItem key={t} value={t}>
                        {t}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="hostname">Hostname *</Label>
                <Input
                  id="hostname"
                  value={form.hostname}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => setForm({ ...form, hostname: e.target.value })}
                  placeholder="e.g. cpe-001.network.local"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="ipAddress">IP Address</Label>
                <Input
                  id="ipAddress"
                  value={form.ipAddress}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => setForm({ ...form, ipAddress: e.target.value })}
                  placeholder="e.g. 192.168.1.1 (optional)"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="location">Location *</Label>
                <Input
                  id="location"
                  value={form.location}
                  onChange={(e: ChangeEvent<HTMLInputElement>) => setForm({ ...form, location: e.target.value })}
                  placeholder="e.g. Building A, Floor 3"
                />
              </div>
            </div>

            {error && (
              <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-3 text-sm text-destructive">
                {error}
              </div>
            )}

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={loading}>
                {loading ? "Registering..." : "Register Device"}
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate("/devices")}>
                Cancel
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
