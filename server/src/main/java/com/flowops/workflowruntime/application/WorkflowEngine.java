package com.flowops.workflowruntime.application;

import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestRevisionJpaEntity;
import com.flowops.workflowconfig.domain.TargetType;
import com.flowops.workflowconfig.infrastructure.persistence.SpringDataWorkflowTransitionRepository;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowTransitionJpaEntity;
import com.flowops.workflowruntime.domain.RoutingResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowEngine {
    public WorkflowEngine(SpringDataWorkflowTransitionRepository transitionRepository, RuleEvaluator ruleEvaluator, AssignmentResolver assignmentResolver) {
        this.transitionRepository = transitionRepository;
        this.ruleEvaluator = ruleEvaluator;
        this.assignmentResolver = assignmentResolver;
    }


    private final SpringDataWorkflowTransitionRepository transitionRepository;
    private final RuleEvaluator ruleEvaluator;
    private final AssignmentResolver assignmentResolver;

    public RoutingResult evaluateNext(WorkflowStepJpaEntity currentStep, RequestJpaEntity request, RequestRevisionJpaEntity revision) {
        List<WorkflowTransitionJpaEntity> transitions = transitionRepository.findByFromStepIdOrderByPriorityAsc(currentStep.getId());

        if (transitions.isEmpty()) {
            return RoutingResult.endApproved();
        }

        for (WorkflowTransitionJpaEntity transition : transitions) {
            boolean matches = ruleEvaluator.evaluate(transition.getConditionExpression(), revision.getFormData());
            if (matches) {
                if (transition.getTargetType() == TargetType.END_APPROVED || transition.getToStep() == null) {
                    return RoutingResult.endApproved();
                }

                WorkflowStepJpaEntity nextStep = transition.getToStep();
                RoutingResult result = RoutingResult.builder()
                        .targetType(TargetType.STEP)
                        .nextStep(nextStep)
                        .build();

                assignmentResolver.resolveAssignment(nextStep, request, result);
                return result;
            }
        }

        return RoutingResult.error("Không tìm thấy nhánh chuyển tiếp phù hợp từ bước: " + currentStep.getStepName());
    }

}
