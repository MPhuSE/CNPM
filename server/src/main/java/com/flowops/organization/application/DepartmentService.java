package com.flowops.organization.application;

import com.flowops.organization.api.dto.DepartmentDto;
import com.flowops.organization.infrastructure.persistence.DepartmentJpaEntity;
import com.flowops.organization.infrastructure.persistence.SpringDataDepartmentRepository;
import com.flowops.shared.application.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class DepartmentService {
    public DepartmentService(SpringDataDepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }


    private final SpringDataDepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public PageResponse<DepartmentDto> getDepartments(int page, int size) {
        Page<DepartmentJpaEntity> deptPage = departmentRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(
                deptPage.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                deptPage.getNumber(),
                deptPage.getSize(),
                deptPage.getTotalElements()
        );
    }

    private DepartmentDto toDto(DepartmentJpaEntity entity) {
        return DepartmentDto.builder()
                .id(String.valueOf(entity.getId()))
                .code(entity.getCode())
                .name(entity.getName())
                .parentId(entity.getParent() != null ? String.valueOf(entity.getParent().getId()) : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .managerId(entity.getManager() != null ? String.valueOf(entity.getManager().getId()) : null)
                .managerName(entity.getManager() != null ? entity.getManager().getFullName() : null)
                .isActive(entity.getIsActive())
                .build();
    }

}
