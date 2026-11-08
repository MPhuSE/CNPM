export interface DashboardMetrics {
  totalSubmittedRequests: number;
  pendingRequests: number;
  approvedRequests: number;
  rejectedRequests: number;
  returnedRequests: number;
  cancelledRequests: number;
  pendingTasks: number;
  overdueTasks: number;
  needsAssignmentTasks: number;
  openIncidents: number;
}
