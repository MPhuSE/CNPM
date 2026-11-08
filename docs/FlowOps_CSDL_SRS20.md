# Thiết kế cơ sở dữ liệu FlowOps đồng bộ SRS 2.0

**Mã:** FLOWOPS-DB · **Phiên bản:** 2.0 DRAFT · **Ngày:** 04/11/2026  
**Nguồn yêu cầu:** Đặc tả yêu cầu phần mềm FlowOps (SRS 2.0) (2).docx, bản dự thảo 02/11/2026.  
**Phạm vi:** thiết kế dữ liệu PostgreSQL cho MVP; mã sơ đồ tại `FlowOps_DB_SRS20.dbml`. Thiết kế này thay thế nội dung 13 bảng cũ. Đây là thiết kế đề xuất được rà soát theo SRS, chưa phải xác nhận hệ thống đã triển khai hoặc các DEC/TBD đã được phê duyệt.

## 1. Kết luận thiết kế và các thay đổi

Mô hình gồm **21 bảng**. Ba role quyền hệ thống là `ROLE_ADMIN`, `ROLE_EMPLOYEE`, `ROLE_APPROVER`. Tài chính, Kế toán, IT là nhóm duyệt. Trưởng bộ phận và quản lý trực tiếp là quan hệ tổ chức. Một tài khoản có thể có nhiều role; Admin không có quyền duyệt thay và người gửi không được tự claim/duyệt.

| Nội dung cũ | Thiết kế sửa |
|---|---|
| Nhóm duyệt gộp vào roles | `approval_groups`, `approval_group_members` độc lập |
| Ghi đè requests.form_data | Working copy ở `request_edits`, mỗi lần gửi vào `request_revisions` bất biến |
| Không biết quyết định áp cho dữ liệu nào | `tasks.revision_id`; `revision_approvals` ghi hiệu lực và nguồn quyết định kế thừa |
| Nhánh chỉ đến bước | `target_type` STEP hoặc END_APPROVED; đích END có `to_step_id=NULL` |
| Gửi lại luôn cùng bước | So diff với revision trước; thay đổi ảnh hưởng thì duyệt lại từ start |
| Chỉ lưu phòng ban | Snapshot phòng ban, quản lý trực tiếp, trưởng bộ phận và nhãn nguồn |
| Thiếu người thì hồ sơ không có đường xử lý | Task PENDING với `assignment_state=NEEDS_ASSIGNMENT`, ghi incident; không gán nhóm Admin |
| Timeline bắt buộc gắn task | `request_events` có request, revision/task tùy sự kiện |
| Chỉ một marker quá hạn/task | Chống trùng từng sự kiện/người/loại; thêm unique quá hạn từng task/người |
| Chưa thiết kế thu hồi phiên và retry | `user_sessions`, `auth_version`, `command_receipts` |

### 1.1 Các quyết định đề xuất để xử lý khoảng trống SRS

- **DB-DEC01 — thiếu người nhận:** tạo đúng một task ở bước cần duyệt, trạng thái PENDING nhưng chưa được phép quyết định; `assignment_state=NEEDS_ASSIGNMENT`, `assignee_id=NULL`. SLA vẫn chạy từ lúc tạo task. Admin chỉ được xem trong hàng đợi điều phối rồi gán người hợp lệ. Đây là phương án cụ thể hóa FR13/FR15; cần cập nhật hình FR13 và tiêu chí FR09/FR11 của SRS để nhất quán.
- **DB-DEC02 — người thay thế:** với GROUP, người mới phải là thành viên hợp lệ của chính nhóm; với USER hoặc quản lý nguồn bị khóa/thiếu/tự duyệt, SRS chưa xác định người thay thế. Mặc định an toàn: giữ cờ, chưa cho gán tùy ý. Danh sách người thay thế được tổ chức phê duyệt cần bổ sung qua thay đổi SRS/SAD; không sửa snapshot hoặc version đã publish để lách thẩm quyền.
- **DB-DEC03 — quyền đọc sau quyết định:** TBD07 chưa chốt. Thiết kế giữ lịch sử và nguồn quyết định để hỗ trợ cả hai lựa chọn; quyền đọc do policy, không mặc nhiên cấp chỉ vì có lịch sử.
- **DB-DEC04 — tự duyệt:** trước khi TBD08 được chốt, dùng hành vi FR13 hiện tại: Cần phân công; không tự nhảy lên cấp trên.
- **DB-DEC05 — bổ sung:** thực hiện nhánh restart/continue theo FR11, nhưng phân loại trường mẫu còn chờ DEC01. Trường thiếu phân loại luôn xem là ảnh hưởng; nếu mọi trường đều ảnh hưởng, mọi thay đổi đều quay về bước đầu.
- **DB-DEC06 — giới hạn và lưu giữ:** TTL phiên, khóa sau đăng nhập sai, thời hạn giữ hồ sơ/receipt/notification và RPO/RTO vẫn chờ TBD09/DEC04. Không tự xóa dữ liệu trước khi có chính sách được xác nhận.

### 1.2 Quy ước

- ID nghiệp vụ: BIGINT identity; định danh phiên, sự kiện và khóa client: UUID. Trong migration dùng `GENERATED ALWAYS AS IDENTITY` cho các ID số được đánh `increment` ở DBML.
- Thời gian: TIMESTAMPTZ; hiển thị UTC+7. Các mốc first_submitted_at và created_at của task bất biến.
- Trạng thái: VARCHAR có CHECK trong migration để dễ thay đổi có kiểm soát. JSONB phải kiểm object/array phù hợp; không chứa mã thực thi.
- FK mặc định NO ACTION/RESTRICT. Không cascading xóa tài khoản, hồ sơ đã gửi hoặc log. Chỉ xóa nháp chưa gửi và working copy theo thủ tục mục 4.
- `updated_at` do ứng dụng/trigger cập nhật nhất quán; `lock_version` phục vụ phát hiện ghi đè, không tự thay thế khóa và kiểm quyền.
- Schema hỗ trợ URL, không upload hoặc lưu tệp vật lý. URL trong revision không bảo đảm nội dung đích bên ngoài bất biến.
- DBML là nguồn sơ đồ, không phải migration đầy đủ. Các partial index, trigger, CHECK liên bảng và giao dịch dưới đây phải được thực hiện riêng trước triển khai.

## 2. Danh mục bảng

| Nhóm | Bảng |
|---|---|
| Tổ chức và xác thực — 7 | departments, users, roles, user_roles, approval_groups, approval_group_members, user_sessions |
| Cấu hình — 4 | workflow_definitions, workflow_versions, workflow_steps, workflow_transitions |
| Runtime — 6 | requests, request_revisions, request_edits, tasks, revision_approvals, request_events |
| Vận hành — 4 | audit_logs, notifications, workflow_incidents, command_receipts |

`requests` là hồ sơ kiêm đối tượng thực thi. Không thêm workflow_instances trong MVP. Quay lại bước đầu sau bổ sung vẫn trên cùng request và version, chỉ có revision mới; không xóa task/quyết định cũ.

## 3. Từ điển dữ liệu

Cột không có `not null` được phép NULL. Trong sơ đồ, một số FK vòng được thể hiện đầy đủ; migration cần tạo bảng trước rồi `ALTER TABLE ADD CONSTRAINT`, không tạo theo thứ tự văn bản một cách máy móc.

### 3.1 departments

Phòng ban; trưởng bộ phận là chức danh, không phải role.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `code` | `varchar(50)` | not null, unique | — |
| `name` | `varchar(150)` | not null | — |
| `parent_id` | `bigint` | NULL được phép | Phòng ban cha; kiểm tra cả chu trình gián tiếp. |
| `manager_id` | `bigint` | NULL được phép | Người giữ chức danh hiện tại; snapshot vào hồ sơ khi gửi. |
| `is_active` | `boolean` | not null, default: true | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

### 3.2 users

Tài khoản giữ lịch sử bằng vô hiệu hóa; không hard delete.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `username` | `varchar(100)` | not null, unique | — |
| `email` | `varchar(255)` | not null | Migration tạo unique lower(email). |
| `password_hash` | `varchar(255)` | not null | Băm mật khẩu, không mã hóa có thể giải ngược. |
| `full_name` | `varchar(200)` | not null | — |
| `department_id` | `bigint` | NULL được phép | — |
| `manager_id` | `bigint` | NULL được phép | Quản lý trực tiếp hiện tại. |
| `is_active` | `boolean` | not null, default: true | — |
| `auth_version` | `bigint` | not null, default: 0 | Tăng khi đổi mật khẩu hoặc vô hiệu tài khoản để thu hồi tất cả phiên. |
| `lock_version` | `bigint` | not null, default: 0 | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

### 3.3 roles

Chỉ seed ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_APPROVER; không dùng role làm nhóm nghiệp vụ.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `code` | `varchar(30)` | not null, unique | — |
| `name` | `varchar(100)` | not null | — |

### 3.4 user_roles

Quyền nhiều vai trò; mọi thay đổi có audit.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `user_id` | `bigint` | not null | — |
| `role_id` | `bigint` | not null | — |
| `assigned_by` | `bigint` | not null | — |
| `assigned_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(user_id, role_id) [pk]`.

### 3.5 approval_groups

Nhóm Tài chính, Kế toán, IT độc lập với role.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `code` | `varchar(80)` | not null, unique | — |
| `name` | `varchar(150)` | not null | — |
| `is_active` | `boolean` | not null, default: true | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

### 3.6 approval_group_members

Thành viên hiện tại; kiểm tra active và ROLE_APPROVER lúc thêm, claim, quyết định.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `group_id` | `bigint` | not null | — |
| `user_id` | `bigint` | not null | — |
| `assigned_by` | `bigint` | not null | — |
| `assigned_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(group_id, user_id) [pk]`.

### 3.7 user_sessions

Hỗ trợ logout từng phiên và thu hồi nhiều phiên; không lưu token rõ.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `uuid` | pk | Sinh UUID ở ứng dụng; dùng sid trong token hoặc mã phiên đã băm. |
| `user_id` | `bigint` | not null | — |
| `token_hash` | `varchar(128)` | not null, unique | Băm secret phiên/refresh token nếu dùng. |
| `auth_version` | `bigint` | not null | Bản auth_version của user tại lúc cấp. |
| `expires_at` | `timestamptz` | not null | — |
| `revoked_at` | `timestamptz` | NULL được phép | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `user_id`.

### 3.8 workflow_definitions

Danh mục workflow; không phụ thuộc loại quy trình trong engine.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `code` | `varchar(100)` | not null, unique | — |
| `name` | `varchar(200)` | not null | — |
| `description` | `text` | NULL được phép | — |
| `is_active` | `boolean` | not null, default: true | — |
| `created_by` | `bigint` | not null | — |
| `lock_version` | `bigint` | not null, default: 0 | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

### 3.9 workflow_versions

Cấu hình bất biến sau publish; trigger bảo vệ cả steps/transitions.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `workflow_definition_id` | `bigint` | not null | — |
| `version_no` | `integer` | not null | — |
| `status` | `varchar(20)` | not null, default: 'DRAFT' | DRAFT / PUBLISHED / ARCHIVED. |
| `form_schema` | `jsonb` | not null | Object fields; includes affects_approval. URL chỉ tham chiếu. |
| `start_step_id` | `bigint` | NULL được phép | Chỉ NULL khi còn nháp. |
| `change_note` | `text` | NULL được phép | — |
| `created_by` | `bigint` | not null | — |
| `published_by` | `bigint` | NULL được phép | — |
| `published_at` | `timestamptz` | NULL được phép | — |
| `lock_version` | `bigint` | not null, default: 0 | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(workflow_definition_id, version_no) [unique]`.

### 3.10 workflow_steps

Không có ID đa hình: dùng assignee_user_id hoặc assignee_group_id.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `workflow_version_id` | `bigint` | not null | — |
| `step_key` | `varchar(100)` | not null | — |
| `step_name` | `varchar(200)` | not null | — |
| `display_order` | `integer` | not null | Thứ tự hiển thị, không thay thế đồ thị routing. |
| `assignee_type` | `varchar(30)` | not null | USER / GROUP / REQUESTER_MANAGER / DEPARTMENT_MANAGER. |
| `assignee_user_id` | `bigint` | NULL được phép | Chỉ USER có giá trị. |
| `assignee_group_id` | `bigint` | NULL được phép | Chỉ GROUP có giá trị. |
| `sla_hours` | `integer` | NULL được phép | NULL nghĩa là không đặt SLA; nếu có phải dương. |
| `allow_request_changes` | `boolean` | not null, default: true | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(id, workflow_version_id) [unique]`; `(workflow_version_id, step_key) [unique]`; `(workflow_version_id, display_order) [unique]`.

### 3.11 workflow_transitions

Nhánh có thể dẫn tới STEP hoặc END_APPROVED; đúng một mặc định cuối ở bước có nhánh.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `workflow_version_id` | `bigint` | not null | — |
| `from_step_id` | `bigint` | not null | — |
| `target_type` | `varchar(20)` | not null | STEP / END_APPROVED. |
| `to_step_id` | `bigint` | NULL được phép | NULL khi END_APPROVED; bắt buộc khi STEP. |
| `condition_expression` | `jsonb` | not null | Object predicate hoặc all/any; mặc định là object rỗng. |
| `is_default` | `boolean` | not null, default: false | Phải khớp condition_expression rỗng. |
| `priority` | `integer` | not null | Giá trị nhỏ xét trước; mặc định có số lớn nhất. |
| `created_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(from_step_id, priority) [unique]`.

### 3.12 requests

Một hồ sơ kiêm runtime; không cần bảng workflow_instances trong MVP.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `request_code` | `varchar(100)` | unique | NULL ở nháp; cấp lần đầu gửi thành công. Hủy nháp vẫn không cấp mã chính thức. |
| `workflow_version_id` | `bigint` | not null | Nháp có thể đổi version sau xác nhận; khóa cứng khi gửi đầu. |
| `requester_id` | `bigint` | not null | — |
| `title` | `varchar(250)` | not null | — |
| `status` | `varchar(20)` | not null, default: 'DRAFT' | DRAFT / PENDING / RETURNED / APPROVED / REJECTED / CANCELLED. |
| `source_department_id` | `bigint` | NULL được phép | Snapshot ID phòng ban lúc gửi đầu. |
| `source_manager_id` | `bigint` | NULL được phép | Snapshot ID quản lý trực tiếp lúc gửi đầu. |
| `source_department_manager_id` | `bigint` | NULL được phép | Snapshot ID trưởng bộ phận lúc gửi đầu. |
| `source_context` | `jsonb` | NULL được phép | Snapshot tên/mã nguồn để hiển thị không phụ thuộc đổi tên hiện tại. |
| `current_revision_id` | `bigint` | NULL được phép | Revision đã gửi hiện hành; NULL trước gửi đầu. |
| `current_step_id` | `bigint` | NULL được phép | Bước đang xử lý hoặc bước vừa trả. |
| `first_submitted_at` | `timestamptz` | NULL được phép | Bất biến; dùng dashboard, không ghi đè khi gửi lại. |
| `last_submitted_at` | `timestamptz` | NULL được phép | — |
| `completed_at` | `timestamptz` | NULL được phép | — |
| `cancel_reason` | `text` | NULL được phép | Bắt buộc nếu CANCELLED. |
| `lock_version` | `bigint` | not null, default: 0 | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(id, workflow_version_id) [unique]`; `(requester_id, status, first_submitted_at)`; `(status, first_submitted_at)`.

### 3.13 request_revisions

INSERT ONLY: mỗi lần gửi một bản dữ liệu hoàn chỉnh; URL nằm trong form_data.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `request_id` | `bigint` | not null | — |
| `revision_no` | `integer` | not null | Bắt đầu 1; tăng dưới khóa hồ sơ. |
| `form_data` | `jsonb` | not null | — |
| `changed_fields` | `jsonb` | not null | Array các khóa đã đổi; revision 1 dùng array rỗng. |
| `restart_required` | `boolean` | not null | Revision đầu TRUE; sau đó tính theo schema và diff. |
| `resume_step_id` | `bigint` | not null | Start step nếu duyệt lại, bước trả nếu tiếp tục. |
| `submitted_by` | `bigint` | not null | — |
| `submitted_at` | `timestamptz` | not null | — |

PK/Index: `(request_id, revision_no) [unique]`; `(id, request_id) [unique]`.

### 3.14 request_edits

Một working copy cho nháp hoặc RETURNED; không coi là revision đã gửi.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `request_id` | `bigint` | pk | — |
| `base_revision_id` | `bigint` | NULL được phép | NULL ở nháp; bản gốc đang bổ sung nếu RETURNED. |
| `form_data` | `jsonb` | not null | — |
| `updated_by` | `bigint` | not null | — |
| `lock_version` | `bigint` | not null, default: 0 | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

### 3.15 tasks

Task chờ có thể chưa phân công; không fallback thành nhóm Admin.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `request_id` | `bigint` | not null | — |
| `workflow_version_id` | `bigint` | not null | Dùng composite FK kiểm version của bước. |
| `workflow_step_id` | `bigint` | not null | — |
| `revision_id` | `bigint` | not null | Bản dữ liệu task này xét, không sửa sang revision khác. |
| `assignment_state` | `varchar(20)` | not null | ASSIGNED / GROUP_OPEN / NEEDS_ASSIGNMENT. |
| `assignee_id` | `bigint` | NULL được phép | NULL ở GROUP_OPEN hoặc NEEDS_ASSIGNMENT. |
| `candidate_group_id` | `bigint` | NULL được phép | Giữ nguồn nhóm cả sau claim; kiểm quyền theo assignment_state. |
| `status` | `varchar(20)` | not null, default: 'PENDING' | PENDING / COMPLETED / CANCELLED. |
| `action_taken` | `varchar(30)` | NULL được phép | APPROVE / REJECT / REQUEST_CHANGES khi COMPLETED. |
| `decision_reason` | `text` | NULL được phép | Bắt buộc khi REJECT hoặc REQUEST_CHANGES. |
| `claimed_at` | `timestamptz` | NULL được phép | — |
| `completed_by` | `bigint` | NULL được phép | — |
| `completed_at` | `timestamptz` | NULL được phép | Kết thúc task; bắt buộc cả khi CANCELLED. |
| `due_at` | `timestamptz` | NULL được phép | Tính lúc tạo kể cả NEEDS_ASSIGNMENT; claim/reassign không thay. |
| `lock_version` | `bigint` | not null, default: 0 | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `updated_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(id, request_id) [unique]`; `(assignee_id, status, due_at)`; `(candidate_group_id, status, due_at)`; `(request_id, created_at)`.

### 3.16 revision_approvals

INSERT ONLY: quyết định APPROVE có hiệu lực cho từng revision và bước; cho phép truy nguồn quyết định được kế thừa.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `request_id` | `bigint` | not null | — |
| `revision_id` | `bigint` | not null | — |
| `workflow_step_id` | `bigint` | not null | — |
| `source_task_id` | `bigint` | not null | Task đã COMPLETED APPROVE, cùng hồ sơ/bước; có thể xét revision trước. |
| `is_inherited` | `boolean` | not null | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(revision_id, workflow_step_id) [pk]`.

### 3.17 request_events

Timeline INSERT ONLY cấp hồ sơ; không yêu cầu phải có task. Cũng lưu reassign trước/sau.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `event_key` | `uuid` | not null, unique | Một sự kiện logic; nguồn chống trùng thông báo. |
| `request_id` | `bigint` | not null | — |
| `revision_id` | `bigint` | NULL được phép | NULL cho hủy nháp chưa gửi. |
| `task_id` | `bigint` | NULL được phép | NULL cho sự kiện không có task. |
| `actor_id` | `bigint` | NULL được phép | NULL khi hệ thống thực hiện. |
| `event_type` | `varchar(40)` | not null | SUBMITTED / RESUBMITTED / CLAIMED / APPROVED / REJECTED / CHANGES_REQUESTED / CANCELLED / REASSIGNED / ASSIGNMENT_REQUIRED / ROUTING_FAILED / INCIDENT_RESOLVED / TASK_OVERDUE. |
| `reason` | `text` | NULL được phép | Lý do nghiệp vụ; không ghi secret. |
| `old_assignee_id` | `bigint` | NULL được phép | — |
| `new_assignee_id` | `bigint` | NULL được phép | — |
| `details` | `jsonb` | not null | Object snapshot/diff và dữ liệu sự kiện. |
| `created_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(request_id, created_at)`.

### 3.18 audit_logs

Audit INSERT ONLY cả hành động quản trị không gắn hồ sơ; không gộp với timeline.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `event_key` | `uuid` | not null, unique | Có thể dùng cùng event_key với request_events, không FK bắt buộc. |
| `actor_id` | `bigint` | NULL được phép | — |
| `request_id` | `bigint` | NULL được phép | — |
| `revision_id` | `bigint` | NULL được phép | — |
| `action` | `varchar(80)` | not null | — |
| `entity_type` | `varchar(60)` | not null | — |
| `entity_id` | `varchar(100)` | not null | Hỗ trợ bigint/UUID của đối tượng khác nhau. |
| `details` | `jsonb` | not null | Before/after tối thiểu, không password/token. |
| `ip_address` | `varchar(45)` | NULL được phép | — |
| `created_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(entity_type, entity_id, created_at)`; `(actor_id, created_at)`; `(request_id, created_at)`.

### 3.19 notifications

Một sự kiện, người nhận, loại chỉ một thông báo. Riêng quá hạn một người/task một lần.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `event_key` | `uuid` | not null | Định danh sự kiện; có thể là sự kiện hệ thống không gắn request_events. |
| `user_id` | `bigint` | not null | — |
| `notification_type` | `varchar(40)` | not null | TASK_ASSIGNED / TASK_OVERDUE / REQUEST_APPROVED / REQUEST_REJECTED / CHANGES_REQUESTED / TASK_REASSIGNED / ASSIGNMENT_REQUIRED / ROUTING_FAILED. |
| `request_id` | `bigint` | NULL được phép | — |
| `task_id` | `bigint` | NULL được phép | — |
| `title` | `varchar(250)` | not null | — |
| `message` | `text` | NULL được phép | — |
| `read_at` | `timestamptz` | NULL được phép | NULL nghĩa chưa đọc. |
| `created_at` | `timestamptz` | not null, default: `now()` | — |

PK/Index: `(event_key, user_id, notification_type) [unique]`; `(user_id, read_at, created_at)`.

### 3.20 workflow_incidents

Sự cố vận hành hiện tại và lịch sử giải quyết; cờ hiển thị được suy từ sự cố OPEN.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `request_id` | `bigint` | not null | — |
| `revision_id` | `bigint` | not null | — |
| `workflow_version_id` | `bigint` | not null | — |
| `step_id` | `bigint` | not null | — |
| `task_id` | `bigint` | NULL được phép | Có task khi cần phân công; lỗi routing có thể chưa có task kế. |
| `incident_type` | `varchar(30)` | not null | ASSIGNMENT_REQUIRED / ROUTING_ERROR. |
| `status` | `varchar(20)` | not null, default: 'OPEN' | OPEN / RESOLVED / CLOSED_CANCELLED. |
| `operation_key` | `uuid` | not null | Liên kết lần thao tác lỗi; chống ghi lặp khi khôi phục. |
| `error_code` | `varchar(100)` | not null | — |
| `details` | `jsonb` | not null | — |
| `resolved_by` | `bigint` | NULL được phép | — |
| `resolution_reason` | `text` | NULL được phép | — |
| `opened_at` | `timestamptz` | not null, default: `now()` | — |
| `closed_at` | `timestamptz` | NULL được phép | — |

PK/Index: `(operation_key, incident_type) [unique]`; `(request_id, status)`.

### 3.21 command_receipts

Chống lặp thao tác; khóa bản ghi và ghi kết quả trong cùng transaction nghiệp vụ.

| Cột | Kiểu | Ràng buộc trong DBML | Ý nghĩa |
|---|---|---|---|
| `id` | `bigint` | pk, increment | — |
| `actor_id` | `bigint` | not null | — |
| `command_type` | `varchar(40)` | not null | SUBMIT / RESUBMIT / CLAIM / DECISION / CANCEL / REASSIGN / RECOVER. |
| `client_key` | `uuid` | not null | Client giữ cùng khóa khi retry cùng thao tác. |
| `payload_hash` | `varchar(64)` | not null | Hash payload chuẩn hóa có target/revision/version; khác payload cùng khóa trả 409. |
| `request_id` | `bigint` | NULL được phép | — |
| `task_id` | `bigint` | NULL được phép | — |
| `response_code` | `integer` | NULL được phép | Chỉ có cùng response_body sau kết quả đã commit. |
| `response_body` | `jsonb` | NULL được phép | Không secret; chỉ lưu kết quả thuộc người thao tác. |
| `created_at` | `timestamptz` | not null, default: `now()` | — |
| `completed_at` | `timestamptz` | NULL được phép | — |

PK/Index: `(actor_id, command_type, client_key) [unique]`.


## 4. Vòng đời và giao dịch

### 4.1 Tạo sửa và xóa nháp

Tạo requests DRAFT, request_code/current_revision/first_submitted_at đều NULL; workflow_version_id tham chiếu version dùng khi nhập. Tạo một request_edits với base_revision_id=NULL. Chỉ người gửi được đọc/sửa/xóa nháp. Cho lưu thiếu trường nhưng kiểm JSON cấu trúc và giới hạn đầu vào.

Nếu version ngừng nhận mới, giao diện đề nghị chuyển dữ liệu sang version hiện hành; chỉ cập nhật sau xác nhận của người dùng, giữ dữ liệu có thể chuyển và chỉ rõ trường cần sửa. Khi gửi phải kiểm version lại trong transaction để không dùng bản vừa bị ngừng nhận.

Xóa thật chỉ khi DRAFT và chưa từng gửi: xóa working copy rồi request; mọi bản ghi phụ/receipt phải được xử lý trong cùng thủ tục theo chính sách để FK không bị gãy. Không xóa lịch sử audit quản trị; audit của xóa nháp chỉ dùng entity_type/entity_id, không FK request_id đến bản đã xóa. Hủy nháp khác xóa: giữ request CANCELLED, giữ lý do/event/audit, không cấp mã chính thức.

### 4.2 Gửi lần đầu

1. Xác thực và kiểm quyền; kiểm tra key chống lặp, khóa requests và workflow definition theo thứ tự khóa thống nhất.
2. Kiểm DRAFT, version đang nhận mới, form hợp lệ. Cấp request_code bằng sequence khi transaction gửi thành công (sequence được phép có khoảng trống).
3. Chốt source_* và source_context. Thêm revision_no=1 với dữ liệu từ working copy; cập nhật current_revision_id, first_submitted_at=last_submitted_at, status=PENDING và start step.
4. Tạo task gắn revision/version/bước. Nếu thiếu người: NEEDS_ASSIGNMENT và incident ASSIGNMENT_REQUIRED; vẫn commit hồ sơ đã gửi. Admin không trở thành ứng viên duyệt.
5. Tạo SUBMITTED và sự kiện phân công nếu cần, audit và notification. Xóa working copy; ghi receipt kết quả. Tất cả cùng commit, trừ số sequence có thể đã tiêu thụ khi rollback.

### 4.3 Claim quyết định hủy và chuyển bước

Khóa requests trước tasks; kiểm phiên/role/thành viên hiện tại, requester != actor, revision và lock_version mà client đã xem. Task nhóm chưa claim không được quyết định. Claim chuyển GROUP_OPEN sang ASSIGNED, ghi người nhận và claimed_at, không đổi hạn. Candidate group nguồn được giữ lại để kiểm tra hiệu lực thành viên.

Quyết định chỉ bởi assignee hợp lệ; cập nhật một lần từ PENDING sang COMPLETED. APPROVE tạo revision_approvals cho bước và revision đang xét; xét nhánh theo priority nhỏ trước. END_APPROVED hoặc bước cuối thật sự → APPROVED. Bước tiếp → tạo task mới. REJECT → REJECTED; REQUEST_CHANGES được phép → RETURNED, khởi tạo working copy từ revision hiện hành. Đóng task, trạng thái, approval link, event, audit và notification cùng transaction.

Hủy phải theo SRS: chủ hồ sơ, DRAFT/PENDING/RETURNED, có lý do; đóng task đang PENDING, không đổi quyết định task cũ; completed_at của hồ sơ/task được ghi; đóng incidents OPEN thành CLOSED_CANCELLED và ghi sự kiện. Hủy và quyết định cạnh tranh không được tạo hai kết quả. Trạng thái cuối không mở lại.

### 4.4 Revision và gửi lại

Ở RETURNED chỉ sửa request_edits, không update revision cũ. So sánh toàn bộ giá trị theo schema đã pin với base revision, chuẩn hóa kiểu trước khi diff; client không tự quyết changed_fields/restart_required.

- Có trường affects_approval=true hoặc chưa phân loại đổi → revision mới, resume_step=start; không sao chép approval link sang revision mới. Quyết định cũ còn lịch sử nhưng không có hiệu lực trên revision mới.
- Chỉ đổi trường affects_approval=false → resume_step=bước vừa trả; copy các revision_approvals còn hợp lệ của các bước đã hoàn thành trước bước trả sang revision mới, giữ source_task_id gốc, is_inherited=true. Không kế thừa REQUEST_CHANGES hoặc REJECT. Mọi field dùng routing/người nhận bị buộc ảnh hưởng phê duyệt.
- Không đổi dữ liệu: cho gửi lại revision mới tại bước trả sau khi validate, ghi diff rỗng (phương án đề xuất để xác nhận khi chốt DEC01).

Cập nhật current_revision_id, last_submitted_at; first_submitted_at giữ nguyên. Tạo một task mới gắn revision mới, tính hạn mới; xóa working copy. Nếu thiếu người vẫn tạo task NEEDS_ASSIGNMENT. Service/constraint trigger kiểm source_task cùng hồ sơ/bước, COMPLETED APPROVE, revision nguồn không mới hơn revision đích; is_inherited phải khớp việc revision task khác revision đích. Không sửa approval link cũ để đánh mất dấu vết.

### 4.5 Phân công lại và quyền hiện tại

Chỉ Admin được reassign task PENDING, có lý do, chọn người active có ROLE_APPROVER, khác requester và phù hợp thẩm quyền (DB-DEC02). Ghi old_assignee/new_assignee, group nguồn nếu có trong details; giữ task ID, revision, created_at và due_at; tăng lock_version. Nếu có incident thiếu người và người mới hợp lệ, đóng incident và gỡ cờ. Không sửa quyết định cũ.

Gỡ quyền/vô hiệu tài khoản làm các task cá nhân phụ thuộc quyền đó chuyển NEEDS_ASSIGNMENT, bỏ assignee hiện tại, ghi người cũ trong event. Rời nhóm chỉ tác động task của chính nhóm nguồn, không vô hiệu task giao theo căn cứ khác. Task nhóm chưa claim vẫn GROUP_OPEN nếu còn người hợp lệ; nếu không, chuyển NEEDS_ASSIGNMENT. Khi nhóm có thành viên mới, chỉ gỡ cờ và mở lại group task trong giao dịch re-evaluate được audit, không tạo task thứ hai. Phải có bước kiểm lại tại claim/quyết định kể cả khi job rà soát chưa chạy.

Nhóm rỗng được xét sau khi loại người gửi, tài khoản bị khóa và người không có ROLE_APPROVER. Để tránh quyền thay đổi giữa kiểm tra và commit, mọi thao tác cập nhật quyền/thành viên và quyết định phải phối hợp khóa user/group tương ứng theo cùng thứ tự; chỉ optimistic lock trên task là chưa đủ.

### 4.6 Lỗi routing và khôi phục

Không khớp nhánh do cấu hình sai hoặc lỗi đánh giá → không APPROVED. Transaction quyết định rollback; sau đó transaction ghi sự cố riêng phải khóa lại request/task và kiểm revision, bước, lock_version ban đầu còn hiệu lực. Nếu một hành động khác đã xử lý thành công thì không gắn cờ cũ. Nếu còn đúng trạng thái, tạo ROUTING_ERROR + event/audit + thông báo Admin, ghi operation_key để retry không lặp. Lỗi kết nối khiến cả ghi sự cố thất bại thì trả lỗi, giữ quyết định chưa xác nhận và ghi log vận hành để phục hồi; không tuyên bố đã lưu cờ.

Khi incident ROUTING_ERROR OPEN, task không nhận quyết định/claim thông thường; người gửi vẫn có thể hủy theo SRS. Admin khắc phục nguyên nhân kỹ thuật rồi dùng lệnh RECOVER có lý do để retry việc đánh giá theo đúng version/revision, quyết định gốc phải được người được giao gửi lại nếu chưa được commit. Admin không thay người duyệt bằng hành động recovery. Không sửa cấu hình published hoặc form_data đã gửi. Nếu lỗi cấu hình không thể khắc phục mà không đổi bản đã pin, giữ cờ và xử lý theo thay đổi nghiệp vụ được phê duyệt; MVP không tự di chuyển hồ sơ sang version khác.

### 4.7 Chống lặp thao tác và thông báo

Client gửi UUID ổn định cho cùng thao tác, khác thao tác dùng key mới. Trong transaction: INSERT command_receipts theo unique actor/command/key; nếu đã có thì khóa/đọc receipt. Cùng hash trả kết quả đã commit sau khi kiểm quyền hiện tại; khác hash trả 409. Insert receipt, biến đổi nghiệp vụ và kết quả cùng commit. Nếu transaction rollback thì receipt cũng rollback; không để receipt IN_PROGRESS đã commit mà không có đường phục hồi. Lỗi xác thực/validation không cần receipt thành công. Concurrent quyết định dùng key khác vẫn chỉ một thắng nhờ state/version guard và unique task.

Một event_key do server sinh gắn một lần chuyển nghiệp vụ. Notification unique event_key/user/type chống người nhiều nhóm bị gửi trùng. Quá hạn có unique task_id/user riêng cho type TASK_OVERDUE để reassign rồi quay lại cùng người không nhắc lại. Nếu reassign tới người mới đã quá hạn, tạo thông báo nhắc cho người mới nếu chưa có. Notification reassign vẫn là sự kiện khác được gửi một lần theo event_key.

GROUP_OPEN: nhắc các ứng viên hợp lệ trừ requester. NEEDS_ASSIGNMENT: nhắc Admin điều phối, không nhắc requester như người duyệt. Chỉ thông báo mới không cấp quyền đọc hồ sơ. Khi task đóng, không tạo nhắc quá hạn mới. Scheduler khóa task trước kiểm trạng thái rồi ghi event/notification trong cùng giao dịch; chọn batch có kiểm checkpoint/công suất để đáp ứng hạn 5 phút của SRS, không suy ra đã đạt chỉ từ cấu hình mỗi phút.

### 4.8 Xuất bản và bảo vệ lịch sử

Mọi sửa draft phải khóa workflow definition/version theo cùng thứ tự với publish. Publish validate đúng snapshot: schema, loại field, phân loại ảnh hưởng, DAG, reachable, đích END, nhánh default cuối, kiểu rule, target hợp lệ. Target quản lý theo người gửi không xác định một user tại publish, nên kiểm đầy đủ lúc runtime.

Archive bản PUBLISHED cũ và flush trước khi publish nháp, tất cả cùng transaction. Guard chặn mọi INSERT/UPDATE/DELETE trên steps/transitions của version PUBLISHED hoặc ARCHIVED; version chỉ được đổi trạng thái PUBLISHED→ARCHIVED, không đổi nội dung/published metadata. Bảo vệ request_revisions, revision_approvals, request_events, audit_logs chỉ ghi thêm; tasks đã quyết định không sửa revision/bước/kết quả. Việc bảo trì đặc quyền DB nằm ngoài quyền ứng dụng.

## 5. Các ràng buộc bắt buộc trong migration PostgreSQL

DBML thể hiện bảng, PK, FK và các index thông thường. Những ràng buộc dưới đây **chưa được biến thành migration đầy đủ chỉ bằng export sơ đồ**.

### 5.1 Index bổ sung

```sql
CREATE UNIQUE INDEX uq_users_email_ci ON users (lower(email));
CREATE UNIQUE INDEX uq_versions_one_draft ON workflow_versions (workflow_definition_id)
  WHERE status = 'DRAFT';
CREATE UNIQUE INDEX uq_versions_one_published ON workflow_versions (workflow_definition_id)
  WHERE status = 'PUBLISHED';
CREATE UNIQUE INDEX uq_transition_default ON workflow_transitions (from_step_id)
  WHERE is_default;
CREATE UNIQUE INDEX uq_tasks_one_pending ON tasks (request_id) WHERE status = 'PENDING';
CREATE UNIQUE INDEX uq_notifications_overdue_recipient ON notifications (task_id, user_id)
  WHERE notification_type = 'TASK_OVERDUE';
CREATE UNIQUE INDEX uq_open_incident ON workflow_incidents (request_id, incident_type)
  WHERE status = 'OPEN';
CREATE INDEX idx_tasks_sla_scan ON tasks (due_at)
  WHERE status = 'PENDING' AND due_at IS NOT NULL;
CREATE INDEX idx_notifications_unread ON notifications (user_id, created_at DESC)
  WHERE read_at IS NULL;
CREATE INDEX idx_request_events_revision ON request_events (revision_id, created_at);
```

### 5.2 CHECK và kiểm tra liên bảng

| Đối tượng | Kiểm tra cần áp dụng |
|---|---|
| roles | code chỉ thuộc ba role đã nêu; không tùy ý tạo role nghiệp vụ |
| departments/users | Không tự làm cha/quản lý; service khóa và duyệt cây để chặn chu trình gián tiếp |
| steps | assignee_type thuộc 4 loại; USER chỉ có user_id; GROUP chỉ có group_id; hai loại quản lý không có target cố định; sla_hours NULL hoặc >0 |
| transitions | STEP → to_step_id NOT NULL và khác from_step_id; END_APPROVED → to_step_id NULL; priority>0; condition là object; is_default tương đương object rỗng; đúng một default và đứng cuối kiểm khi publish |
| versions | version_no>0; state hợp lệ; published/archived có published_at/by/start; JSON schema là object |
| requests | State hợp lệ; mã nếu có không rỗng; first/last gửi cùng có hoặc cùng NULL, last>=first; hồ sơ đã gửi có code/current_revision/source_context; nháp chưa từng gửi; trạng thái cuối có completed_at; CANCELLED có lý do |
| revisions/edits | form_data object; changed_fields array; revision_no>0; revisions.submitted_by=requester; resume_step thuộc version của request; current_revision phải là revision mới nhất đã commit |
| task assignment | ASSIGNED có assignee; GROUP_OPEN không assignee và có group; NEEDS_ASSIGNMENT không assignee, group nguồn có thể giữ; trường hợp không nhóm thì direct assignment |
| tasks | State hợp lệ; PENDING không decision/completed metadata; COMPLETED có action/completed_by/completed_at; REJECT/REQUEST_CHANGES có reason; CANCELLED không action/completed_by, có completed_at; due_at NULL hoặc >=created_at |
| tasks liên bảng | Version bằng request và step (composite FK); revision cùng request (composite FK); tác vụ mới chỉ cho current_revision; status RETURNED/final không có task PENDING; completed_by=assignee khác requester và đủ quyền tại thời điểm commit |
| revision_approvals | source_task cùng request/bước, đã APPROVE; revision đích/current chain hợp lệ, source không đến từ tương lai; link kế thừa chỉ khi gửi lại không ảnh hưởng |
| events | revision/task thuộc request bằng FK; nếu có cả task/revision thì revision bằng task.revision cho claim/decision; event gửi/hủy có thể không có task; REASSIGNED có old/new phù hợp và reason |
| notifications | Khi task_id có giá trị thì request_id bắt buộc; TASK_OVERDUE cần task_id; type hợp lệ; read_at NULL hoặc >=created_at |
| incidents | State/type hợp lệ; OPEN không close metadata; RESOLVED có resolved_by/reason/closed_at; CLOSED_CANCELLED có closed_at và dấu vết hủy; request/revision/step/task nhất quán |
| audit | Nếu revision_id có giá trị thì request_id bắt buộc; không mật khẩu/token trong details; entity_type/id có giá trị |
| receipts | response_code/body/completed_at cùng có hoặc cùng NULL; không để hàng chưa hoàn thành commit trong luồng bình thường |

Composite FK có cột nullable dùng MATCH SIMPLE; bổ sung CHECK bắt buộc request_id khi có task_id/revision_id ở những bảng mà request_id cũng nullable. Tạo FK vòng `requests.current_revision_id` và `workflow_versions.start_step_id` sau bảng đích. Nếu cần constraint kiểm cuối giao dịch, dùng constraint trigger DEFERRABLE để cho phép các cập nhật trung gian hợp lệ trong transaction.

### 5.3 Chính sách khóa và bất biến

Phối hợp khóa theo thứ tự chung của ứng dụng: các khóa tổ chức/quyền cần thiết lấy theo ID tăng dần, rồi workflow definition/version khi liên quan, rồi requests, rồi tasks/receipt cần thiết; receipt được giữ trong cùng giao dịch và mọi command phải thống nhất thứ tự. Tránh để luồng Admin đổi quyền khóa tasks trước rồi quay lại users trong khi luồng quyết định làm ngược lại. Retry transaction kỹ thuật chỉ khi chưa tạo kết quả nghiệp vụ được xác nhận; quyết định xung đột trả lỗi để người dùng tải lại.

Khóa Admin cuối phải có điểm đồng bộ chung, ví dụ khóa hàng roles ROLE_ADMIN trước khi đọc/sửa tập user_roles; mọi đường vô hiệu/gỡ quyền cùng tuân thủ. Kiểm count trước update mà không khóa không đủ.

## 6. Cấu hình mẫu kiểm chứng

- **Mua sắm:** fields item(text), quantity(integer>0), amount(VND integer>0), reason(text). Bước quản lý nguồn: priority 1 amount GTE 10000000 → Finance; priority 2 default → END_APPROVED. Finance không có nhánh → kết thúc khi duyệt. Dùng GTE, không GT.
- **Hoàn chi phí:** expense_date, amount, receipt_url(HTTPS), description; quản lý nguồn → Accounting → kết thúc. URL không ảnh hưởng chỉ khi DEC01 được xác nhận.
- **Cấp quyền:** system_name, access_level(select), duration_days(integer>0), justification; trưởng bộ phận nguồn → IT → kết thúc. Không tự cấp quyền hệ thống ngoài.
- SLA mẫu quản lý/trưởng bộ phận 24 giờ, Finance/Accounting/IT 48 giờ; tất cả bước cho phép bổ sung. Tiền/số lượng/đối tượng/quyền/thời hạn và mọi field dùng routing đều ảnh hưởng phê duyệt.
- Quá hạn suy từ task PENDING, due_at<now(); Cần phân công từ incident ASSIGNMENT_REQUIRED OPEN; Lỗi điều phối từ ROUTING_ERROR OPEN. Không dùng ba cờ này làm status kết thúc.

## 7. Truy vết và kiểm chứng trước triển khai

| SRS | Thành phần dữ liệu | Ca bắt buộc |
|---|---|---|
| FR01–03, BR01/10/11 | users, roles, user_roles, groups, members, sessions | Logout một phiên; đổi mật khẩu thu hồi tất cả; role cuối; mất nhóm chỉ ảnh hưởng đúng task; cấm tự duyệt |
| FR04–07, BR02/03/07 | definitions, versions, steps, transitions | Publish cạnh tranh; chặn sửa step/rule sau publish; nhánh END; không vòng lặp |
| FR08–12, BR04 | requests, edits, revisions | Nháp không code; gửi hai lần; trả URL tiếp tục; đổi tiền restart; dữ liệu cũ bất biến; hủy nháp chưa code |
| FR13–17, BR06/08/09 | tasks, incidents, receipts | Claim cạnh tranh; 100 quyết định cùng task một thắng; task không người; reassign giữ hạn; hủy cạnh tranh; lỗi routing không approve |
| FR18–19 | events, notifications | Cùng người nhiều nhóm chỉ một thông báo/sự kiện; overdue theo người/task; reassign quá hạn; restart không trùng |
| FR20–21 | events, audit, revision_approvals, first_submitted_at | Timeline revision/nguồn quyết định; audit quản trị; dashboard đếm theo gửi đầu không gửi lại |
| NFR01–09 | Query/index, transaction, session, backup | Load theo SRS: 50 users, 10k hồ sơ, 50k task, p95<500ms; công bố môi trường; restore và đối chiếu dữ liệu |

Đây là thiết kế được kiểm về cấu trúc sơ đồ và tính đối chiếu tài liệu. Chưa có migration/ứng dụng chạy thực, nên chưa xác nhận đã đạt constraint trigger, concurrency, hiệu năng hoặc mục tiêu phục hồi.

## 8. Chuyển từ thiết kế 13 bảng cũ

1. Chốt các DB-DEC trên với DEC/TBD của SRS; cập nhật SAD trước khi code.
2. Tách nhóm nghiệp vụ ra khỏi roles; chỉ giữ ba role; chuyển assignee_type ROLE sang GROUP, liên kết bằng ID.
3. Hồ sơ cũ chỉ có form_data hiện tại: chỉ có thể tạo revision khởi tạo từ dữ liệu còn lại; không suy diễn hoặc dựng lại dữ liệu các lần gửi trước khi không có bằng chứng. Lịch sử quyết định không rõ revision phải được đánh dấu nguồn cũ và xử lý bằng kế hoạch migration riêng, không gắn giả vào revision mới.
4. Chuyển claim/reassign/history sang events và bổ sung audit; dữ liệu thiếu người cũ/mới không tự bịa.
5. Backfill snapshot quản lý chỉ nếu có nguồn lịch sử đáng tin; nếu không, đánh dấu chưa xác minh và xử lý hồ sơ đang chạy bằng quyết định nghiệp vụ.
6. Kiểm danh sách task trùng, quan hệ cross-version, code nháp, URL và trạng thái trước khi bật ràng buộc mới; không xóa lịch sử để làm migration qua.

## 9. Dùng code dbdiagram

Mở https://dbdiagram.io → New Diagram → chọn phần code DBML, xóa ví dụ và dán toàn bộ file `FlowOps_DB_SRS20.dbml`. Tên bảng/cột trong sơ đồ khớp từ điển dữ liệu ở mục 3. Xuất hình/PDF từ giao diện nếu cần đưa vào báo cáo. Export SQL từ DBML chỉ là bước đầu, phải bổ sung mục 5 trước khi triển khai.

Tham chiếu cú pháp DBML: https://dbml.dbdiagram.io/docs/ (đối chiếu ngày 04/11/2026).
