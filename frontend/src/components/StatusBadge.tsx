import { Badge } from "@/components/ui/badge";
import { DeviceStatus } from "@/types";
import { statusColor } from "@/utils/stale";

interface StatusBadgeProps {
  status: DeviceStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <Badge variant="outline" className={statusColor(status)}>
      {status}
    </Badge>
  );
}
