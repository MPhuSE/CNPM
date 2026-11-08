package com.flowops.identity.application;

import com.flowops.identity.api.dto.UserDto;
import com.flowops.identity.infrastructure.persistence.RoleJpaEntity;
import com.flowops.identity.infrastructure.persistence.SpringDataUserRepository;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.shared.application.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class UserService {
    public UserService(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private final SpringDataUserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsers(int page, int size) {
        Page<UserJpaEntity> userPage = userRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(
                userPage.getContent().stream().map(this::toDto).collect(Collectors.toList()),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements()
        );
    }

    private UserDto toDto(UserJpaEntity entity) {
        return UserDto.builder()
                .id(String.valueOf(entity.getId()))
                .username(entity.getUsername())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .departmentId(entity.getDepartment() != null ? String.valueOf(entity.getDepartment().getId()) : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .managerId(entity.getManager() != null ? String.valueOf(entity.getManager().getId()) : null)
                .managerName(entity.getManager() != null ? entity.getManager().getFullName() : null)
                .isActive(entity.getIsActive())
                .roles(entity.getRoles().stream().map(RoleJpaEntity::getCode).collect(Collectors.toList()))
                .lockVersion(entity.getLockVersion())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
