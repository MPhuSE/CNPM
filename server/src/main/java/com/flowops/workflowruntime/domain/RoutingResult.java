package com.flowops.workflowruntime.domain;

import com.flowops.approvals.domain.AssignmentState;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.organization.infrastructure.persistence.ApprovalGroupJpaEntity;
import com.flowops.workflowconfig.domain.TargetType;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;

public class RoutingResult {
    private TargetType targetType;
    private WorkflowStepJpaEntity nextStep;
    private AssignmentState assignmentState;
    private UserJpaEntity assignee;
    private ApprovalGroupJpaEntity candidateGroup;
    private boolean isEndApproved;
    private boolean isRoutingError;
    private String errorMessage;

    public RoutingResult() {}

    public static RoutingResult endApproved() {
        RoutingResult r = new RoutingResult();
        r.setTargetType(TargetType.END_APPROVED);
        r.setEndApproved(true);
        return r;
    }

    public static RoutingResult error(String message) {
        RoutingResult r = new RoutingResult();
        r.setRoutingError(true);
        r.setErrorMessage(message);
        return r;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RoutingResult r = new RoutingResult();
        public Builder targetType(TargetType targetType) { r.setTargetType(targetType); return this; }
        public Builder nextStep(WorkflowStepJpaEntity nextStep) { r.setNextStep(nextStep); return this; }
        public Builder assignmentState(AssignmentState state) { r.setAssignmentState(state); return this; }
        public Builder assignee(UserJpaEntity assignee) { r.setAssignee(assignee); return this; }
        public Builder candidateGroup(ApprovalGroupJpaEntity candidateGroup) { r.setCandidateGroup(candidateGroup); return this; }
        public Builder isEndApproved(boolean isEndApproved) { r.setEndApproved(isEndApproved); return this; }
        public Builder isRoutingError(boolean isRoutingError) { r.setRoutingError(isRoutingError); return this; }
        public Builder errorMessage(String msg) { r.setErrorMessage(msg); return this; }
        public RoutingResult build() { return r; }
    }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    public WorkflowStepJpaEntity getNextStep() { return nextStep; }
    public void setNextStep(WorkflowStepJpaEntity nextStep) { this.nextStep = nextStep; }
    public AssignmentState getAssignmentState() { return assignmentState; }
    public void setAssignmentState(AssignmentState assignmentState) { this.assignmentState = assignmentState; }
    public UserJpaEntity getAssignee() { return assignee; }
    public void setAssignee(UserJpaEntity assignee) { this.assignee = assignee; }
    public ApprovalGroupJpaEntity getCandidateGroup() { return candidateGroup; }
    public void setCandidateGroup(ApprovalGroupJpaEntity candidateGroup) { this.candidateGroup = candidateGroup; }
    public boolean isEndApproved() { return isEndApproved; }
    public void setEndApproved(boolean endApproved) { isEndApproved = endApproved; }
    public boolean isRoutingError() { return isRoutingError; }
    public void setRoutingError(boolean routingError) { isRoutingError = routingError; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
