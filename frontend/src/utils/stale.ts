export const formatDate = (iso: string | null): string => {
  if (!iso) return "Never";
  return new Date(iso).toLocaleString();
};

export function getRelativeTime(iso: string | null): string {
  if (!iso) return "Never";
  const date = new Date(iso);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffSec < 10) return "Just now";
  if (diffMin < 1) return `${diffSec} sec ago`;
  if (diffMin < 60) return `${diffMin} min ago`;
  if (diffHour < 24) return `${diffHour} hour${diffHour > 1 ? "s" : ""} ago`;
  if (diffDay < 7) return `${diffDay} day${diffDay > 1 ? "s" : ""} ago`;
  return formatDate(iso);
}

export const statusColor = (status: string): string => {
  switch (status) {
    case "ONLINE":
      return "bg-green-500/15 text-green-700 dark:text-green-400";
    case "OFFLINE":
      return "bg-red-500/15 text-red-700 dark:text-red-400";
    case "DEGRADED":
      return "bg-yellow-500/15 text-yellow-700 dark:text-yellow-400";
    default:
      return "bg-gray-500/15 text-gray-700 dark:text-gray-400";
  }
};
