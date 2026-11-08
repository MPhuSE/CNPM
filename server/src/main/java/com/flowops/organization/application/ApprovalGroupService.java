package com.flowops.organization.application;

import com.flowops.organization.api.dto.ApprovalGroupDto;
import com.flowops.organization.infrastructure.persistence.ApprovalGroupJpaEntity;
import com.flowops.organization.infrastructure.persistence.SpringDataApprovalGroupRepository;
import com.flowops.shared.application.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class ApprovalGroupService {
    public ApprovalGroupService(SpringDataApprovalGroupRepository approvalGroupRepository) {
        this.approvalGroupRepository = approvalGroupRepository;
    }


    private final SpringDataApprovalGroupRepository approvalGroupRepository;

    @Transactional(readOnly = true)
    public PageResponse<ApprovalGroupDto> getApprovalGroups(int page, int size) {
        Page<ApprovalGroupJpaEntity> groupPage = approvalGroupRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(
                groupPage.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                groupPage.getNumber(),
                groupPage.getSize(),
                groupPage.getTotalElements()
        );
    }

    private ApprovalGroupDto toDto(ApprovalGroupJpaEntity entity) {
        return ApprovalGroupDto.builder()
                .id(String.valueOf(entity.getId()))
                .code(entity.getCode())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .memberCount(entity.getMembers().size())
                .members(entity.getMembers().stream()
                        .map(m -> ApprovalGroupDto.MemberSummaryDto.builder()
                                .id(String.valueOf(m.getUser().getId()))
                                .username(m.getUser().getUsername())
                                .fullName(m.getUser().getFullName())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

}
