export interface NotificationItem {
  id: string;
  eventKey: string;
  type: string;
  title: string;
  message?: string | null;
  requestId?: string | null;
  taskId?: string | null;
  readAt?: string | null;
  createdAt: string;
}
