package com.flowops.identity.api;

import com.flowops.identity.api.dto.UserDto;
import com.flowops.identity.application.UserService;
import com.flowops.shared.application.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Quản lý danh sách người dùng và phân quyền")
public class UserController {
    public UserController(UserService userService) {
        this.userService = userService;
    }


    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy danh sách người dùng có phân trang")
    public ResponseEntity<PageResponse<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userService.getUsers(page, size));
    }

}
