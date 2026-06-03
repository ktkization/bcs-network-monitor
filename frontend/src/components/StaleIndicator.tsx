import { Badge } from "@/components/ui/badge";

interface StaleIndicatorProps {
  stale: boolean;
}

export function StaleIndicator({ stale }: StaleIndicatorProps) {
  if (!stale) return null;
  return (
    <Badge variant="outline" className="bg-orange-500/15 text-orange-700 dark:text-orange-400">
      STALE
    </Badge>
  );
}
