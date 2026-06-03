import { Badge } from "@/components/ui/badge";
import { DeviceStatus } from "@/types";
import { statusColor } from "@/utils/stale";

interface StatusBadgeProps {
  status: DeviceStatus;
}

const dotColor: Record<DeviceStatus, string> = {
  ONLINE: "bg-green-500",
  OFFLINE: "bg-red-500",
  DEGRADED: "bg-yellow-500",
};

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <Badge variant="outline" className={`gap-1.5 ${statusColor(status)}`}>
      <span className={`h-1.5 w-1.5 rounded-full ${dotColor[status]}`} />
      {status}
    </Badge>
  );
}
