# FlowOps — Hệ thống Quản lý Quy trình & Phê duyệt

FlowOps là nền tảng quản lý quy trình nghiệp vụ (Business Workflow & Approval Engine) theo kiến trúc **Modular Monolith** và **Ports & Adapters**, bám sát đặc tả SRS 2.0 và tài liệu thiết kế SAD `FLO-DEV-001`.

---

## 1. Cấu trúc Source Code

Hệ thống được tổ chức theo mô hình Monorepo:

```text
flowops/
├── client/                      # Frontend: React 18, TypeScript, Vite, Tailwind CSS
│   ├── src/
│   │   ├── app/                 # Providers (Auth, Query), Layout (Navbar, Sidebar), Router
│   │   ├── features/            # Feature slices theo 3 khu giao diện:
│   │   │   ├── auth/            # Đăng nhập, quản lý phiên
│   │   │   ├── my-requests/     # [ROLE_EMPLOYEE] Tạo nháp, gửi, bổ sung, hủy hồ sơ
│   │   │   ├── task-inbox/      # [ROLE_APPROVER] Nhận việc (claim), phê duyệt, trả bổ sung
│   │   │   ├── coordination/    # [ROLE_ADMIN] Hàng đợi cần phân công, xử lý sự cố
│   │   │   ├── workflow-builder/# [ROLE_ADMIN] Cấu hình form & bước duyệt, xuất bản
│   │   │   ├── organization/    # [ROLE_ADMIN] Quản lý tài khoản, phòng ban, nhóm duyệt
│   │   │   ├── audit/           # [ROLE_ADMIN] Nhật ký kiểm toán bất biến
│   │   │   ├── dashboard/       # [ROLE_ADMIN] Báo cáo SLA & số liệu vận hành
│   │   │   └── notifications/   # Trung tâm thông báo in-app
│   │   └── shared/              # API Client (Axios/Fetch), UI Components, Formatters, Types
│   ├── package.json
│   ├── tailwind.config.js
│   └── vite.config.ts
├── server/                      # Backend: Java 21, Spring Boot 3.3.x, Spring Security, JPA, Flyway
│   ├── src/main/java/com/flowops/
│   │   ├── bootstrap/           # Application entrypoint, Security, Jackson, CORS, OpenAPI
│   │   ├── shared/              # Domain BaseEntity, Exceptions, ClockPort, GlobalExceptionHandler
│   │   ├── identity/            # M1: Users, Roles, UserSessions, JWT Auth
│   │   ├── organization/        # M1: Departments, ApprovalGroups, Members
│   │   ├── workflowconfig/      # M2: Definitions, Versions, Steps, Transitions
│   │   ├── requests/            # M3: Requests, Revisions, Working Copy (Edits)
│   │   ├── approvals/           # M4: Tasks, RevisionApprovals, Claims, Decisions
│   │   ├── workflowruntime/     # M4 Engine: WorkflowEngine, RuleEvaluator, AssignmentResolver
│   │   ├── operations/          # M5: WorkflowIncidents, CommandReceipts (Idempotency)
│   │   ├── notifications/       # M5: In-app Notifications, SLA Overdue Scheduler
│   │   ├── audit/               # M5: Immutable AuditLogs, RequestEvents
│   │   └── reporting/           # M5: Metrics & Dashboard aggregations
│   ├── src/main/resources/
│   │   ├── application.yml      # Cấu hình Datasource, JPA, Flyway, JWT, Actuator
│   │   └── db/migration/        # Flyway migrations (V1 -> V7: 21 bảng CSDL + Dev Seed)
│   └── pom.xml
├── docs/                        # Tài liệu đặc tả & thiết kế (SAD, DBML, Data Dictionary)
├── infra/                       # Docker Compose (PostgreSQL 16), .env.example
├── scripts/                     # Script kiểm thử khói (smoke.sh)
├── frontend -> client           # Symlink tiện ích
└── backend -> server            # Symlink tiện ích
```

---

## 2. Ba vai trò và ba phân vùng giao diện

| Vai trò | Phân vùng giao diện | Thẩm quyền chính |
|---|---|---|
| **ROLE_EMPLOYEE** | Hồ sơ của tôi (`/my-requests`) | Tạo nháp, gửi phê duyệt, nhận yêu cầu bổ sung, gửi lại, hủy hồ sơ, xem revision |
| **ROLE_APPROVER** | Hộp thư duyệt (`/inbox`) | Xem nhiệm vụ cá nhân/nhóm, nhận việc (claim), phê duyệt (Approve), từ chối (Reject), yêu cầu bổ sung (Request Changes) |
| **ROLE_ADMIN** | Quản trị (`/dashboard`, `/coordination`, `/workflows`, `/organization`, `/audit`) | Điều phối nhiệm vụ thiếu người duyệt, giải quyết sự cố, quản lý tài khoản/nhóm duyệt, cấu hình và xuất bản quy trình |

> **Nguyên tắc chống tự duyệt (Anti-Self-Approval):** Người tạo hồ sơ tuyệt đối không thể claim hoặc approve bất kỳ nhiệm vụ nào thuộc chính hồ sơ của mình, kể cả khi giữ vai trò Approver hoặc Trưởng bộ phận.

---

## 3. Khởi chạy hệ thống

### Bước 1: Khởi động Cơ sở dữ liệu (PostgreSQL 16)

```bash
cd infra
docker compose up -d
```

### Bước 2: Khởi động Backend (Spring Boot)

```bash
cd server
mvn spring-boot:run
```

* Backend chạy tại: `http://localhost:8080`
* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* Flyway migrations (V1 -> V7) sẽ tự động khởi tạo 21 bảng và nạp sẵn dữ liệu demo (3 workflow: Mua sắm thiết bị, Hoàn ứng chi phí, Cấp quyền truy cập).

### Bước 3: Khởi động Frontend (React + Tailwind CSS)

```bash
cd client
npm run dev
```

* Frontend chạy tại: `http://localhost:5173`

---

## 4. Tài khoản Demo mặc định

| Tên đăng nhập | Mật khẩu | Vai trò | Chức danh / Nhóm |
|---|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN`, `ROLE_EMPLOYEE` | Quản trị viên hệ thống |
| `approver` | `approver123` | `ROLE_APPROVER` | Người phê duyệt |
| `employee` | `employee123` | `ROLE_EMPLOYEE` | Nhân viên kinh doanh |
| `manager_alice` | `approver123` | `ROLE_EMPLOYEE`, `ROLE_APPROVER` | Quản lý trực tiếp |
| `finance_bob` | `approver123` | `ROLE_APPROVER` | Ban Tài chính / Kế toán |
| `it_charlie` | `approver123` | `ROLE_APPROVER` | Đội IT Support |

# CNPM
