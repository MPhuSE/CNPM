package com.flowops.identity.application;

import com.flowops.identity.api.dto.LoginRequest;
import com.flowops.identity.api.dto.LoginResponse;
import com.flowops.identity.infrastructure.persistence.RoleJpaEntity;
import com.flowops.identity.infrastructure.persistence.SpringDataUserRepository;
import com.flowops.identity.infrastructure.persistence.SpringDataUserSessionRepository;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.identity.infrastructure.persistence.UserSessionJpaEntity;
import com.flowops.shared.domain.BusinessRuleException;
import com.flowops.shared.domain.EntityNotFoundException;
import com.flowops.shared.infrastructure.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    public AuthService(SpringDataUserRepository userRepository, SpringDataUserSessionRepository sessionRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    private final SpringDataUserRepository userRepository;
    private final SpringDataUserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserJpaEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessRuleException("ACCOUNT_LOCKED", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        // Create new session
        UUID sessionId = UUID.randomUUID();
        String sessionSecret = UUID.randomUUID().toString();
        String tokenHash = hashToken(sessionSecret);

        UserSessionJpaEntity session = new UserSessionJpaEntity();
        session.setId(sessionId);
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setAuthVersion(user.getAuthVersion());
        session.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        sessionRepository.save(session);

        List<String> roleCodes = user.getRoles().stream()
                .map(RoleJpaEntity::getCode)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getUsername(),
                sessionId,
                user.getAuthVersion(),
                roleCodes
        );

        LoginResponse.UserDto userDto = LoginResponse.UserDto.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .departmentId(user.getDepartment() != null ? String.valueOf(user.getDepartment().getId()) : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .roles(roleCodes)
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .user(userDto)
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse.UserDto getCurrentUser(Long userId) {
        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng", userId));

        List<String> roleCodes = user.getRoles().stream()
                .map(RoleJpaEntity::getCode)
                .collect(Collectors.toList());

        return LoginResponse.UserDto.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .departmentId(user.getDepartment() != null ? String.valueOf(user.getDepartment().getId()) : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .roles(roleCodes)
                .build();
    }

    @Transactional
    public void logout(Long userId) {
        // Can revoke user sessions if needed
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
