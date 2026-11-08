package com.flowops.workflowconfig.api.dto;

import com.flowops.workflowconfig.domain.AssigneeType;
import com.flowops.workflowconfig.domain.TargetType;
import com.flowops.workflowconfig.domain.VersionStatus;

import java.time.Instant;
import java.util.List;

public class WorkflowVersionDetailDto {
    private String id;
    private String workflowDefinitionId;
    private Integer versionNo;
    private VersionStatus status;
    private Object formSchema;
    private String startStepKey;
    private String changeNote;
    private Long lockVersion;
    private Instant publishedAt;
    private List<StepDto> steps;
    private List<TransitionDto> transitions;

    public WorkflowVersionDetailDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWorkflowDefinitionId() { return workflowDefinitionId; }
    public void setWorkflowDefinitionId(String workflowDefinitionId) { this.workflowDefinitionId = workflowDefinitionId; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public VersionStatus getStatus() { return status; }
    public void setStatus(VersionStatus status) { this.status = status; }
    public Object getFormSchema() { return formSchema; }
    public void setFormSchema(Object formSchema) { this.formSchema = formSchema; }
    public String getStartStepKey() { return startStepKey; }
    public void setStartStepKey(String startStepKey) { this.startStepKey = startStepKey; }
    public String getChangeNote() { return changeNote; }
    public void setChangeNote(String changeNote) { this.changeNote = changeNote; }
    public Long getLockVersion() { return lockVersion; }
    public void setLockVersion(Long lockVersion) { this.lockVersion = lockVersion; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public List<StepDto> getSteps() { return steps; }
    public void setSteps(List<StepDto> steps) { this.steps = steps; }
    public List<TransitionDto> getTransitions() { return transitions; }
    public void setTransitions(List<TransitionDto> transitions) { this.transitions = transitions; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final WorkflowVersionDetailDto dto = new WorkflowVersionDetailDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder workflowDefinitionId(String defId) { dto.setWorkflowDefinitionId(defId); return this; }
        public Builder versionNo(Integer versionNo) { dto.setVersionNo(versionNo); return this; }
        public Builder status(VersionStatus status) { dto.setStatus(status); return this; }
        public Builder formSchema(Object formSchema) { dto.setFormSchema(formSchema); return this; }
        public Builder startStepKey(String startStepKey) { dto.setStartStepKey(startStepKey); return this; }
        public Builder changeNote(String changeNote) { dto.setChangeNote(changeNote); return this; }
        public Builder lockVersion(Long lockVersion) { dto.setLockVersion(lockVersion); return this; }
        public Builder publishedAt(Instant publishedAt) { dto.setPublishedAt(publishedAt); return this; }
        public Builder steps(List<StepDto> steps) { dto.setSteps(steps); return this; }
        public Builder transitions(List<TransitionDto> transitions) { dto.setTransitions(transitions); return this; }
        public WorkflowVersionDetailDto build() { return dto; }
    }

    public static class StepDto {
        private String id;
        private String stepKey;
        private String stepName;
        private Integer displayOrder;
        private AssigneeType assigneeType;
        private String assigneeUserId;
        private String assigneeGroupId;
        private Integer slaHours;
        private Boolean allowRequestChanges;

        public StepDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getStepKey() { return stepKey; }
        public void setStepKey(String stepKey) { this.stepKey = stepKey; }
        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        public AssigneeType getAssigneeType() { return assigneeType; }
        public void setAssigneeType(AssigneeType assigneeType) { this.assigneeType = assigneeType; }
        public String getAssigneeUserId() { return assigneeUserId; }
        public void setAssigneeUserId(String assigneeUserId) { this.assigneeUserId = assigneeUserId; }
        public String getAssigneeGroupId() { return assigneeGroupId; }
        public void setAssigneeGroupId(String assigneeGroupId) { this.assigneeGroupId = assigneeGroupId; }
        public Integer getSlaHours() { return slaHours; }
        public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }
        public Boolean getAllowRequestChanges() { return allowRequestChanges; }
        public void setAllowRequestChanges(Boolean allowRequestChanges) { this.allowRequestChanges = allowRequestChanges; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final StepDto dto = new StepDto();
            public Builder id(String id) { dto.setId(id); return this; }
            public Builder stepKey(String stepKey) { dto.setStepKey(stepKey); return this; }
            public Builder stepName(String stepName) { dto.setStepName(stepName); return this; }
            public Builder displayOrder(Integer displayOrder) { dto.setDisplayOrder(displayOrder); return this; }
            public Builder assigneeType(AssigneeType type) { dto.setAssigneeType(type); return this; }
            public Builder assigneeUserId(String userId) { dto.setAssigneeUserId(userId); return this; }
            public Builder assigneeGroupId(String groupId) { dto.setAssigneeGroupId(groupId); return this; }
            public Builder slaHours(Integer slaHours) { dto.setSlaHours(slaHours); return this; }
            public Builder allowRequestChanges(Boolean allow) { dto.setAllowRequestChanges(allow); return this; }
            public StepDto build() { return dto; }
        }
    }

    public static class TransitionDto {
        private String id;
        private String fromStepKey;
        private TargetType targetType;
        private String toStepKey;
        private Object conditionExpression;
        private Boolean isDefault;
        private Integer priority;

        public TransitionDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFromStepKey() { return fromStepKey; }
        public void setFromStepKey(String fromStepKey) { this.fromStepKey = fromStepKey; }
        public TargetType getTargetType() { return targetType; }
        public void setTargetType(TargetType targetType) { this.targetType = targetType; }
        public String getToStepKey() { return toStepKey; }
        public void setToStepKey(String toStepKey) { this.toStepKey = toStepKey; }
        public Object getConditionExpression() { return conditionExpression; }
        public void setConditionExpression(Object conditionExpression) { this.conditionExpression = conditionExpression; }
        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final TransitionDto dto = new TransitionDto();
            public Builder id(String id) { dto.setId(id); return this; }
            public Builder fromStepKey(String fromStepKey) { dto.setFromStepKey(fromStepKey); return this; }
            public Builder targetType(TargetType targetType) { dto.setTargetType(targetType); return this; }
            public Builder toStepKey(String toStepKey) { dto.setToStepKey(toStepKey); return this; }
            public Builder conditionExpression(Object expr) { dto.setConditionExpression(expr); return this; }
            public Builder isDefault(Boolean isDefault) { dto.setIsDefault(isDefault); return this; }
            public Builder priority(Integer priority) { dto.setPriority(priority); return this; }
            public TransitionDto build() { return dto; }
        }
    }
}
