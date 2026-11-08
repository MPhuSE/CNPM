package com.flowops.identity.api;

import com.flowops.identity.api.dto.LoginRequest;
import com.flowops.identity.api.dto.LoginResponse;
import com.flowops.identity.application.AuthService;
import com.flowops.shared.infrastructure.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Đăng nhập, đăng xuất và quản lý phiên làm việc")
public class AuthController {
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    private final AuthService authService;

    @PostMapping("/auth/login")
    @Operation(summary = "Đăng nhập hệ thống")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    public ResponseEntity<LoginResponse.UserDto> getCurrentUser() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Đăng xuất và thu hồi phiên")
    public ResponseEntity<Void> logout() {
        SecurityUtils.getCurrentUserId().ifPresent(authService::logout);
        return ResponseEntity.ok().build();
    }

}
