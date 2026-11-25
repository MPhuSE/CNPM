# FlowOps decisions — authentication scope (SCRUM-75)

This file records only the auth decisions used by SCRUM-75. It does not replace the full team decision log.

## AD08 — Session-backed JWT (approved by Owner)

- Access JWT: signed `sub` (user ID), `sid` (session UUID), `authVersion`, `iat`, `exp`. Access TTL is 15 minutes, capped at absolute session expiry.
- Every authenticated Bearer request checks current user active state, user/session authVersion, session ownership, expiry and revocation in PostgreSQL. Authorities come from current `user_roles`, not JWT claims.
- Login creates a session with absolute 7-day expiry. Refresh rotates a 256-bit SecureRandom secret under user → session row locks. Only SHA-256 hash is persisted. Old secret returns 401 without clearing the cookie; no token family or session-wide replay revoke.
- Logout revokes the current `sid`; successful password change increments user authVersion and revokes every session in one transaction.
- Auth POST endpoints require exact allowed Origin and `X-FlowOps-CSRF: 1`; refresh cookie is HttpOnly, SameSite=Lax, host-only, path `/api/v1/auth`, Secure in production.

## TBD09 — provisional, Owner has not closed policy

- BCrypt strength 12, configurable. New password: at least 15 Unicode code points, at most 72 UTF-8 bytes. Existing passwords remain valid for login.
- Login limit proposal: 5 failed logins per username and 30 admitted attempts (successful or failed) per `getRemoteAddr()` IP per 15 minutes, configurable; 429 with Retry-After. Admission reserves account capacity atomically and releases it on request completion; no global lock is held during BCrypt or database work. Counter keys are SHA-256 digests, and the entry map is bounded, process-local, and reset on restart. Multi-instance deployments need a shared limiter and trusted proxy IP handling before production.
- Production needs a private `FLOWOPS_JWT_SECRET`, explicit `FLOWOPS_CORS_ORIGINS`, HTTPS and `FLOWOPS_COOKIE_SECURE=true`.

## Baseline blockers outside approved SCRUM-75 schema changes

- V1 omits `lock_version` from `departments` and `approval_groups`, while both JPA entities inherit it from `BaseEntity`. Hibernate `ddl-auto: validate` fails on a fresh 21-table database. Auth integration tests use an isolated test-container-only compatibility shim to exercise auth; production requires an approved forward migration or mapping decision.
- V7 seeds demo accounts in the common Flyway location, and its BCrypt hash does not match README `admin123`. Those Leader-added demo credentials are not a SCRUM-75 acceptance contract. The auth test instead creates an isolated short legacy password to verify non-retroactive login policy. V7 remains unchanged; Owner can resolve demo credentials and profile/seed strategy separately before demo or production deployment.

## Acceptance trace (SCRUM-75 only)

| Requirement | Auth implementation | Evidence |
|---|---|---|
| FR01 | Login, refresh rotation, per-session logout, password change | `AuthSessionIntegrationTest` login/refresh/logout/password cases |
| FR03 authentication portion | Live user/session validation and current DB roles for Bearer requests | `SessionAuthenticationService`, JWT negative cases |
| NFR04 | BCrypt, 15 Unicode character/72 UTF-8 byte policy, Origin/CSRF, 256-bit refresh secret hash, rate limiting | Password boundary, Origin/CSRF, cookie, 429 tests |
| AT01 authentication portion | Invalid JWT/session, generic login failure, revocation and race checks | JWT, concurrent refresh, login/password and refresh/logout race tests |

Last Admin, CRUD and task invalidation remain SCRUM-76. IDOR/contextual policy and business-race checks remain SCRUM-77.
