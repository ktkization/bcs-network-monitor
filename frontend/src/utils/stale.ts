export const formatDate = (iso: string | null): string => {
  if (!iso) return "Never";
  return new Date(iso).toLocaleString();
};

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
