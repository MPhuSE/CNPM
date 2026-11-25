package com.flowops.identity.application;

import com.flowops.identity.infrastructure.persistence.RoleJpaEntity;
import com.flowops.identity.infrastructure.persistence.SpringDataUserRepository;
import com.flowops.identity.infrastructure.persistence.SpringDataUserSessionRepository;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.identity.infrastructure.persistence.UserSessionJpaEntity;
import com.flowops.shared.infrastructure.SessionPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SessionAuthenticationService {
    private final SpringDataUserRepository users;
    private final SpringDataUserSessionRepository sessions;

    public SessionAuthenticationService(SpringDataUserRepository users, SpringDataUserSessionRepository sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public record Authenticated(SessionPrincipal principal, List<SimpleGrantedAuthority> authorities) {}

    @Transactional(readOnly = true)
    public Authenticated authenticate(Long userId, UUID sid, Long authVersion) {
        UserJpaEntity user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        UserSessionJpaEntity session = sessions.findById(sid).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (!Boolean.TRUE.equals(user.getIsActive()) || !session.getUser().getId().equals(userId)
                || session.getRevokedAt() != null || !Instant.now().isBefore(session.getExpiresAt())
                || !authVersion.equals(user.getAuthVersion()) || !authVersion.equals(session.getAuthVersion()))
            throw new IllegalArgumentException("Invalid token");
        return new Authenticated(new SessionPrincipal(userId, sid, authVersion),
                user.getRoles().stream().map(RoleJpaEntity::getCode).map(SimpleGrantedAuthority::new).toList());
    }
}
