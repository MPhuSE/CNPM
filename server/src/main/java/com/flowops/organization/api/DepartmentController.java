package com.flowops.organization.api;

import com.flowops.organization.api.dto.DepartmentDto;
import com.flowops.organization.application.DepartmentService;
import com.flowops.shared.application.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Departments", description = "Quản lý cơ cấu tổ chức và phòng ban")
public class DepartmentController {
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }


    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy danh sách phòng ban")
    public ResponseEntity<PageResponse<DepartmentDto>> getDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(departmentService.getDepartments(page, size));
    }

}
