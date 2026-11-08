package com.flowops.workflowruntime.application;

import com.flowops.approvals.domain.AssignmentState;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import com.flowops.workflowconfig.domain.AssigneeType;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import com.flowops.workflowruntime.domain.RoutingResult;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AssignmentResolver {

    public void resolveAssignment(WorkflowStepJpaEntity step, RequestJpaEntity request, RoutingResult result) {
        AssigneeType type = step.getAssigneeType();

        switch (type) {
            case USER -> {
                UserJpaEntity user = step.getAssigneeUser();
                if (user != null && Boolean.TRUE.equals(user.getIsActive()) && !isSelfApproval(user, request)) {
                    result.setAssignmentState(AssignmentState.ASSIGNED);
                    result.setAssignee(user);
                } else {
                    result.setAssignmentState(AssignmentState.NEEDS_ASSIGNMENT);
                }
            }
            case GROUP -> {
                result.setAssignmentState(AssignmentState.GROUP_OPEN);
                result.setCandidateGroup(step.getAssigneeGroup());
            }
            case REQUESTER_MANAGER -> {
                UserJpaEntity manager = request.getSourceManager() != null
                        ? request.getSourceManager()
                        : (request.getRequester() != null ? request.getRequester().getManager() : null);

                if (manager != null && Boolean.TRUE.equals(manager.getIsActive()) && !isSelfApproval(manager, request)) {
                    result.setAssignmentState(AssignmentState.ASSIGNED);
                    result.setAssignee(manager);
                } else {
                    result.setAssignmentState(AssignmentState.NEEDS_ASSIGNMENT);
                }
            }
            case DEPARTMENT_MANAGER -> {
                UserJpaEntity deptManager = request.getSourceDepartmentManager() != null
                        ? request.getSourceDepartmentManager()
                        : (request.getSourceDepartment() != null ? request.getSourceDepartment().getManager() : null);

                if (deptManager != null && Boolean.TRUE.equals(deptManager.getIsActive()) && !isSelfApproval(deptManager, request)) {
                    result.setAssignmentState(AssignmentState.ASSIGNED);
                    result.setAssignee(deptManager);
                } else {
                    result.setAssignmentState(AssignmentState.NEEDS_ASSIGNMENT);
                }
            }
        }
    }

    private boolean isSelfApproval(UserJpaEntity approver, RequestJpaEntity request) {
        if (approver == null || request.getRequester() == null) return false;
        return Objects.equals(approver.getId(), request.getRequester().getId());
    }
}
