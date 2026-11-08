package com.flowops.reporting.application;

import com.flowops.approvals.domain.AssignmentState;
import com.flowops.approvals.domain.TaskStatus;
import com.flowops.approvals.infrastructure.persistence.SpringDataTaskRepository;
import com.flowops.operations.infrastructure.persistence.SpringDataIncidentRepository;
import com.flowops.reporting.api.dto.DashboardMetricsDto;
import com.flowops.requests.domain.RequestStatus;
import com.flowops.requests.infrastructure.persistence.SpringDataRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DashboardService {
    public DashboardService(SpringDataRequestRepository requestRepository, SpringDataTaskRepository taskRepository, SpringDataIncidentRepository incidentRepository) {
        this.requestRepository = requestRepository;
        this.taskRepository = taskRepository;
        this.incidentRepository = incidentRepository;
    }


    private final SpringDataRequestRepository requestRepository;
    private final SpringDataTaskRepository taskRepository;
    private final SpringDataIncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsDto getMetrics() {
        long submitted = requestRepository.countByFirstSubmittedAtIsNotNull();
        long pendingReq = requestRepository.countByStatus(RequestStatus.PENDING);
        long approvedReq = requestRepository.countByStatus(RequestStatus.APPROVED);
        long rejectedReq = requestRepository.countByStatus(RequestStatus.REJECTED);
        long returnedReq = requestRepository.countByStatus(RequestStatus.RETURNED);
        long cancelledReq = requestRepository.countByStatus(RequestStatus.CANCELLED);

        long pendingTasks = taskRepository.countByStatus(TaskStatus.PENDING);
        long needsAssignment = taskRepository.countByAssignmentStateAndStatus(AssignmentState.NEEDS_ASSIGNMENT, TaskStatus.PENDING);
        long overdueTasks = taskRepository.countOverdueTasks(Instant.now());
        long openIncidents = incidentRepository.countByStatus("OPEN");

        return DashboardMetricsDto.builder()
                .totalSubmittedRequests(submitted)
                .pendingRequests(pendingReq)
                .approvedRequests(approvedReq)
                .rejectedRequests(rejectedReq)
                .returnedRequests(returnedReq)
                .cancelledRequests(cancelledReq)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .needsAssignmentTasks(needsAssignment)
                .openIncidents(openIncidents)
                .build();
    }

}
