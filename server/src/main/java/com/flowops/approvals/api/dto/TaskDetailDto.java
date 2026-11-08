package com.flowops.approvals.api.dto;

import com.flowops.approvals.domain.AssignmentState;
import com.flowops.approvals.domain.TaskStatus;

import java.time.Instant;
import java.util.List;

public class TaskDetailDto {
    private String id;
    private String requestId;
    private String requestCode;
    private String requestTitle;
    private String requesterName;
    private String workflowName;
    private String stepName;
    private String revisionId;
    private Integer revisionNo;
    private TaskStatus status;
    private AssignmentState assignmentState;
    private String assigneeName;
    private String candidateGroupName;
    private Instant dueAt;
    private boolean isOverdue;
    private Instant createdAt;
    private Long taskVersion;
    private Long requestVersion;
    private Object formData;
    private Boolean allowRequestChanges;
    private List<String> allowedActions;

    public TaskDetailDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }
    public String getRequestTitle() { return requestTitle; }
    public void setRequestTitle(String requestTitle) { this.requestTitle = requestTitle; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }
    public Integer getRevisionNo() { return revisionNo; }
    public void setRevisionNo(Integer revisionNo) { this.revisionNo = revisionNo; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public AssignmentState getAssignmentState() { return assignmentState; }
    public void setAssignmentState(AssignmentState assignmentState) { this.assignmentState = assignmentState; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public String getCandidateGroupName() { return candidateGroupName; }
    public void setCandidateGroupName(String candidateGroupName) { this.candidateGroupName = candidateGroupName; }
    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }
    public boolean isOverdue() { return isOverdue; }
    public void setOverdue(boolean overdue) { isOverdue = overdue; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Long getTaskVersion() { return taskVersion; }
    public void setTaskVersion(Long taskVersion) { this.taskVersion = taskVersion; }
    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public Object getFormData() { return formData; }
    public void setFormData(Object formData) { this.formData = formData; }
    public Boolean getAllowRequestChanges() { return allowRequestChanges; }
    public void setAllowRequestChanges(Boolean allowRequestChanges) { this.allowRequestChanges = allowRequestChanges; }
    public List<String> getAllowedActions() { return allowedActions; }
    public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TaskDetailDto dto = new TaskDetailDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder requestId(String reqId) { dto.setRequestId(reqId); return this; }
        public Builder requestCode(String code) { dto.setRequestCode(code); return this; }
        public Builder requestTitle(String title) { dto.setRequestTitle(title); return this; }
        public Builder requesterName(String name) { dto.setRequesterName(name); return this; }
        public Builder workflowName(String name) { dto.setWorkflowName(name); return this; }
        public Builder stepName(String name) { dto.setStepName(name); return this; }
        public Builder revisionId(String id) { dto.setRevisionId(id); return this; }
        public Builder revisionNo(Integer no) { dto.setRevisionNo(no); return this; }
        public Builder status(TaskStatus status) { dto.setStatus(status); return this; }
        public Builder assignmentState(AssignmentState state) { dto.setAssignmentState(state); return this; }
        public Builder assigneeName(String name) { dto.setAssigneeName(name); return this; }
        public Builder candidateGroupName(String name) { dto.setCandidateGroupName(name); return this; }
        public Builder dueAt(Instant dueAt) { dto.setDueAt(dueAt); return this; }
        public Builder isOverdue(boolean isOverdue) { dto.setOverdue(isOverdue); return this; }
        public Builder createdAt(Instant createdAt) { dto.setCreatedAt(createdAt); return this; }
        public Builder taskVersion(Long v) { dto.setTaskVersion(v); return this; }
        public Builder requestVersion(Long v) { dto.setRequestVersion(v); return this; }
        public Builder formData(Object data) { dto.setFormData(data); return this; }
        public Builder allowRequestChanges(Boolean allow) { dto.setAllowRequestChanges(allow); return this; }
        public Builder allowedActions(List<String> actions) { dto.setAllowedActions(actions); return this; }
        public TaskDetailDto build() { return dto; }
    }
}
