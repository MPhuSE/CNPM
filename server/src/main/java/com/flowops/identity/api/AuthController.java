package com.flowops.identity.api;

import com.flowops.identity.api.dto.LoginRequest;
import com.flowops.identity.api.dto.LoginResponse;
import com.flowops.identity.api.dto.ChangePasswordRequest;
import com.flowops.identity.application.AuthService;
import com.flowops.shared.infrastructure.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Đăng nhập, đăng xuất và quản lý phiên làm việc")
public class AuthController {
    public AuthController(AuthService authService, @Value("${flowops.auth.cookie.secure:true}") boolean secureCookie,
                          Environment environment) {
        this.authService = authService;
        this.secureCookie = secureCookie || environment.acceptsProfiles(Profiles.of("prod"));
    }


    private final AuthService authService;
    private final boolean secureCookie; // dọc câu hình từ application
    private static final String COOKIE_NAME = "flowops_refresh";

    @PostMapping("/auth/login")
    @Operation(summary = "Đăng nhập hệ thống")
    @SecurityRequirements
    @Parameters({@Parameter(name = "Origin", in = ParameterIn.HEADER, required = true),
            @Parameter(name = "X-FlowOps-CSRF", in = ParameterIn.HEADER, required = true, example = "1")})
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        AuthService.AuthResult result = authService.login(request, servletRequest.getRemoteAddr());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(result).toString())
                .body(LoginResponse.builder().accessToken(result.accessToken()).user(result.user()).build());
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Gia hạn access token và xoay vòng refresh secret")
    @SecurityRequirements
    @Parameters({@Parameter(name = "Origin", in = ParameterIn.HEADER, required = true),
            @Parameter(name = "X-FlowOps-CSRF", in = ParameterIn.HEADER, required = true, example = "1")})
    public ResponseEntity<Map<String, String>> refresh(
            // Nếu cookie không tồn tại, Spring truyền null vào method thay vì tự động từ chối request do thiếu cookie
            @CookieValue(name = COOKIE_NAME, required = false) String cookie) {
        AuthService.AuthResult result = authService.refresh(cookie);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(result).toString())
                .body(Map.of("accessToken", result.accessToken()));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    public ResponseEntity<LoginResponse.UserDto> getCurrentUser() {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Đăng xuất và thu hồi phiên")
    @Parameters({@Parameter(name = "Origin", in = ParameterIn.HEADER, required = true),
            @Parameter(name = "X-FlowOps-CSRF", in = ParameterIn.HEADER, required = true, example = "1")})
    public ResponseEntity<Void> logout() {
        authService.logout(SecurityUtils.getRequiredSessionPrincipal());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearCookie().toString()).build();
    }

    @PostMapping("/auth/password")
    @Operation(summary = "Đổi mật khẩu và thu hồi toàn bộ phiên")
    @Parameters({@Parameter(name = "Origin", in = ParameterIn.HEADER, required = true),
            @Parameter(name = "X-FlowOps-CSRF", in = ParameterIn.HEADER, required = true, example = "1")})
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.getRequiredSessionPrincipal(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearCookie().toString()).build();
    }

    //Hàm helper/mapper chuyển đổi dữ liệu trong AuthResult thành obj cookie
    private ResponseCookie refreshCookie(AuthService.AuthResult result) {
        long remaining = Math.max(0, Duration.between(Instant.now(), result.expiresAt()).getSeconds());
        return ResponseCookie.from(COOKIE_NAME, result.refreshCookie()).httpOnly(true).secure(secureCookie)
                .sameSite("Lax").path("/api/v1/auth").maxAge(Duration.ofSeconds(remaining)).build();
    }

    private ResponseCookie clearCookie() {
        return ResponseCookie.from(COOKIE_NAME, "").httpOnly(true).secure(secureCookie)
                .sameSite("Lax").path("/api/v1/auth").maxAge(Duration.ZERO).build();
    }

}
