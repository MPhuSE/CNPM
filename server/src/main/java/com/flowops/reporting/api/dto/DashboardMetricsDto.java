package com.flowops.reporting.api.dto;

public class DashboardMetricsDto {
    private long totalSubmittedRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private long returnedRequests;
    private long cancelledRequests;
    private long pendingTasks;
    private long overdueTasks;
    private long needsAssignmentTasks;
    private long openIncidents;

    public DashboardMetricsDto() {}

    public long getTotalSubmittedRequests() { return totalSubmittedRequests; }
    public void setTotalSubmittedRequests(long totalSubmittedRequests) { this.totalSubmittedRequests = totalSubmittedRequests; }
    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }
    public long getApprovedRequests() { return approvedRequests; }
    public void setApprovedRequests(long approvedRequests) { this.approvedRequests = approvedRequests; }
    public long getRejectedRequests() { return rejectedRequests; }
    public void setRejectedRequests(long rejectedRequests) { this.rejectedRequests = rejectedRequests; }
    public long getReturnedRequests() { return returnedRequests; }
    public void setReturnedRequests(long returnedRequests) { this.returnedRequests = returnedRequests; }
    public long getCancelledRequests() { return cancelledRequests; }
    public void setCancelledRequests(long cancelledRequests) { this.cancelledRequests = cancelledRequests; }
    public long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }
    public long getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(long overdueTasks) { this.overdueTasks = overdueTasks; }
    public long getNeedsAssignmentTasks() { return needsAssignmentTasks; }
    public void setNeedsAssignmentTasks(long needsAssignmentTasks) { this.needsAssignmentTasks = needsAssignmentTasks; }
    public long getOpenIncidents() { return openIncidents; }
    public void setOpenIncidents(long openIncidents) { this.openIncidents = openIncidents; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DashboardMetricsDto dto = new DashboardMetricsDto();
        public Builder totalSubmittedRequests(long v) { dto.setTotalSubmittedRequests(v); return this; }
        public Builder pendingRequests(long v) { dto.setPendingRequests(v); return this; }
        public Builder approvedRequests(long v) { dto.setApprovedRequests(v); return this; }
        public Builder rejectedRequests(long v) { dto.setRejectedRequests(v); return this; }
        public Builder returnedRequests(long v) { dto.setReturnedRequests(v); return this; }
        public Builder cancelledRequests(long v) { dto.setCancelledRequests(v); return this; }
        public Builder pendingTasks(long v) { dto.setPendingTasks(v); return this; }
        public Builder overdueTasks(long v) { dto.setOverdueTasks(v); return this; }
        public Builder needsAssignmentTasks(long v) { dto.setNeedsAssignmentTasks(v); return this; }
        public Builder openIncidents(long v) { dto.setOpenIncidents(v); return this; }
        public DashboardMetricsDto build() { return dto; }
    }
}
