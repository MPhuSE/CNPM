/**
 * Format string/number to VND currency display
 */
export function formatVND(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === "") return "0 ₫";
  const num = typeof value === "string" ? BigInt(value.replace(/\D/g, "") || "0") : BigInt(Math.floor(value));
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(num);
}

/**
 * Format ISO datetime string to VN timezone (UTC+7)
 */
export function formatDateTime(isoString: string | null | undefined): string {
  if (!isoString) return "—";
  const date = new Date(isoString);
  return new Intl.DateTimeFormat("vi-VN", {
    timeZone: "Asia/Ho_Chi_Minh",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

/**
 * Map Request status code to Vietnamese label and badge color
 */
export function getRequestStatusMeta(status: string) {
  switch (status) {
    case "DRAFT":
      return { label: "Bản nháp", color: "gray" as const };
    case "PENDING":
      return { label: "Đang chờ duyệt", color: "blue" as const };
    case "RETURNED":
      return { label: "Cần bổ sung", color: "amber" as const };
    case "APPROVED":
      return { label: "Đã duyệt", color: "emerald" as const };
    case "REJECTED":
      return { label: "Từ chối", color: "rose" as const };
    case "CANCELLED":
      return { label: "Đã hủy", color: "slate" as const };
    default:
      return { label: status, color: "gray" as const };
  }
}

/**
 * Map Task assignment & action status to metadata
 */
export function getTaskStatusMeta(status: string, assignmentState?: string) {
  if (status === "COMPLETED") return { label: "Hoàn tất", color: "emerald" as const };
  if (status === "CANCELLED") return { label: "Đã đóng", color: "slate" as const };

  switch (assignmentState) {
    case "GROUP_OPEN":
      return { label: "Chờ nhận việc", color: "indigo" as const };
    case "NEEDS_ASSIGNMENT":
      return { label: "Cần phân công", color: "rose" as const };
    case "ASSIGNED":
    default:
      return { label: "Đang xử lý", color: "blue" as const };
  }
}
