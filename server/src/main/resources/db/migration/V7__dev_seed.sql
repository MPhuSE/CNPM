-- V7__dev_seed.sql
-- Seed data for development environment

-- 1. Roles
INSERT INTO roles (id, code, name) OVERRIDING SYSTEM VALUE VALUES
(1, 'ROLE_ADMIN', 'Quản trị viên'),
(2, 'ROLE_EMPLOYEE', 'Nhân viên'),
(3, 'ROLE_APPROVER', 'Người phê duyệt');

-- 2. Departments
INSERT INTO departments (id, code, name, is_active) OVERRIDING SYSTEM VALUE VALUES
(1, 'BOD', 'Ban Giám đốc', true),
(2, 'IT', 'Phòng Công nghệ thông tin', true),
(3, 'FIN', 'Phòng Tài chính - Kế toán', true),
(4, 'SALES', 'Phòng Kinh doanh', true);

-- 3. Users
-- Passwords: BCrypt hash for "admin123", "approver123", "employee123" ($2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt2sW2C)
INSERT INTO users (id, username, email, password_hash, full_name, department_id, is_active) OVERRIDING SYSTEM VALUE VALUES
(1, 'admin', 'admin@flowops.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt2sW2C', 'Quản trị viên hệ thống', 1, true),
(2, 'approver', 'approver@flowops.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt2sW2C', 'Nguyễn Văn Phê Duyệt', 2, true),
(3, 'employee', 'employee@flowops.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt2sW2C', 'Trần Thị Nhân Viên', 4, true),
(4, 'manager_alice', 'alice@flowops.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt2sW2C', 'Alice Quản Lý', 4, true),
(5, 'finance_bob', 'bob@flowops.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt2sW2C', 'Bob Tài Chính', 3, true),
(6, 'it_charlie', 'charlie@flowops.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt2sW2C', 'Charlie IT Lead', 2, true);

-- Update manager_id hierarchy
UPDATE users SET manager_id = 4 WHERE id = 3; -- employee's manager is Alice
UPDATE users SET manager_id = 1 WHERE id IN (2, 4, 5, 6);

UPDATE departments SET manager_id = 1 WHERE id = 1;
UPDATE departments SET manager_id = 6 WHERE id = 2;
UPDATE departments SET manager_id = 5 WHERE id = 3;
UPDATE departments SET manager_id = 4 WHERE id = 4;

-- 4. User Roles
INSERT INTO user_roles (user_id, role_id, assigned_by) VALUES
(1, 1, 1), -- admin has ROLE_ADMIN
(1, 2, 1), -- admin has ROLE_EMPLOYEE
(2, 3, 1), -- approver has ROLE_APPROVER
(3, 2, 1), -- employee has ROLE_EMPLOYEE
(4, 2, 1), -- manager_alice has ROLE_EMPLOYEE
(4, 3, 1), -- manager_alice has ROLE_APPROVER
(5, 3, 1), -- finance_bob has ROLE_APPROVER
(6, 3, 1); -- it_charlie has ROLE_APPROVER

-- 5. Approval Groups
INSERT INTO approval_groups (id, code, name, is_active) OVERRIDING SYSTEM VALUE VALUES
(1, 'FINANCE', 'Ban Tài chính', true),
(2, 'ACCOUNTING', 'Phòng Kế toán', true),
(3, 'IT_SUPPORT', 'Đội IT Support', true);

INSERT INTO approval_group_members (group_id, user_id, assigned_by) VALUES
(1, 5, 1), -- finance_bob in FINANCE
(2, 5, 1), -- finance_bob in ACCOUNTING
(3, 6, 1); -- it_charlie in IT_SUPPORT

-- 6. Three Standard Demo Workflows (WF10 in SAD)

-- Workflow 1: PROCUREMENT (Mua sắm thiết bị)
INSERT INTO workflow_definitions (id, code, name, description, created_by) OVERRIDING SYSTEM VALUE VALUES
(1, 'PROCUREMENT', 'Mua sắm thiết bị', 'Quy trình mua sắm thiết bị: Quản lý trực tiếp -> Tài chính (nếu >= 10M) -> END', 1);

INSERT INTO workflow_versions (id, workflow_definition_id, version_no, status, form_schema, created_by, published_by, published_at) OVERRIDING SYSTEM VALUE VALUES
(1, 1, 1, 'PUBLISHED', '{
  "fields": [
    {"key": "item", "label": "Thiết bị yêu cầu", "type": "text", "required": true, "maxLength": 200, "affectsApproval": true},
    {"key": "quantity", "label": "Số lượng", "type": "number", "min": 1, "required": true, "affectsApproval": true},
    {"key": "amount", "label": "Tổng tiền", "type": "vnd", "min": 1, "required": true, "affectsApproval": true},
    {"key": "receiptUrl", "label": "Link chứng từ tham khảo", "type": "url", "required": false, "affectsApproval": false}
  ]
}'::jsonb, 1, 1, NOW());

INSERT INTO workflow_steps (id, workflow_version_id, step_key, step_name, display_order, assignee_type, sla_hours, allow_request_changes) OVERRIDING SYSTEM VALUE VALUES
(1, 1, 'MANAGER_APPROVAL', 'Quản lý trực tiếp duyệt', 1, 'REQUESTER_MANAGER', 24, true),
(2, 1, 'FINANCE_APPROVAL', 'Ban Tài chính duyệt', 2, 'GROUP', 48, true);

UPDATE workflow_steps SET assignee_group_id = 1 WHERE id = 2; -- FINANCE group
UPDATE workflow_versions SET start_step_id = 1 WHERE id = 1;

-- Transitions for PROCUREMENT:
-- Step 1 (Manager): If amount >= 10,000,000 -> Step 2 (Finance), Else -> END_APPROVED
INSERT INTO workflow_transitions (id, workflow_version_id, from_step_id, target_type, to_step_id, condition_expression, is_default, priority) OVERRIDING SYSTEM VALUE VALUES
(1, 1, 1, 'STEP', 2, '{"field": "amount", "op": "GTE", "value": "10000000"}'::jsonb, false, 1),
(2, 1, 1, 'END_APPROVED', NULL, '{}'::jsonb, true, 2),
(3, 1, 2, 'END_APPROVED', NULL, '{}'::jsonb, true, 1);

-- Workflow 2: EXPENSE_REIMBURSEMENT (Hoàn ứng chi phí)
INSERT INTO workflow_definitions (id, code, name, description, created_by) OVERRIDING SYSTEM VALUE VALUES
(2, 'EXPENSE_REIMBURSEMENT', 'Hoàn ứng chi phí', 'Quy trình hoàn chi phí: Quản lý trực tiếp -> Kế toán -> END', 1);

INSERT INTO workflow_versions (id, workflow_definition_id, version_no, status, form_schema, created_by, published_by, published_at) OVERRIDING SYSTEM VALUE VALUES
(2, 2, 1, 'PUBLISHED', '{
  "fields": [
    {"key": "expenseDate", "label": "Ngày chi", "type": "date", "required": true, "affectsApproval": true},
    {"key": "purpose", "label": "Mục đích chi", "type": "text", "required": true, "maxLength": 250, "affectsApproval": true},
    {"key": "amount", "label": "Số tiền hoàn ứng", "type": "vnd", "min": 1, "required": true, "affectsApproval": true},
    {"key": "receiptUrl", "label": "Hóa đơn / Chứng từ điện tử", "type": "url", "required": true, "affectsApproval": true}
  ]
}'::jsonb, 1, 1, NOW());

INSERT INTO workflow_steps (id, workflow_version_id, step_key, step_name, display_order, assignee_type, sla_hours, allow_request_changes) OVERRIDING SYSTEM VALUE VALUES
(3, 2, 'MANAGER_APPROVAL', 'Quản lý trực tiếp duyệt', 1, 'REQUESTER_MANAGER', 24, true),
(4, 2, 'ACCOUNTING_APPROVAL', 'Phòng Kế toán duyệt', 2, 'GROUP', 48, true);

UPDATE workflow_steps SET assignee_group_id = 2 WHERE id = 4; -- ACCOUNTING group
UPDATE workflow_versions SET start_step_id = 3 WHERE id = 2;

INSERT INTO workflow_transitions (id, workflow_version_id, from_step_id, target_type, to_step_id, condition_expression, is_default, priority) OVERRIDING SYSTEM VALUE VALUES
(4, 2, 3, 'STEP', 4, '{}'::jsonb, true, 1),
(5, 2, 4, 'END_APPROVED', NULL, '{}'::jsonb, true, 1);

-- Workflow 3: ACCESS_REQUEST (Cấp quyền truy cập hệ thống)
INSERT INTO workflow_definitions (id, code, name, description, created_by) OVERRIDING SYSTEM VALUE VALUES
(3, 'ACCESS_REQUEST', 'Cấp quyền truy cập hệ thống', 'Quy trình xin cấp quyền: Trưởng bộ phận -> IT Support -> END', 1);

INSERT INTO workflow_versions (id, workflow_definition_id, version_no, status, form_schema, created_by, published_by, published_at) OVERRIDING SYSTEM VALUE VALUES
(3, 3, 1, 'PUBLISHED', '{
  "fields": [
    {"key": "systemName", "label": "Hệ thống cần truy cập", "type": "text", "required": true, "maxLength": 100, "affectsApproval": true},
    {"key": "accessLevel", "label": "Mức quyền đề nghị", "type": "text", "required": true, "maxLength": 100, "affectsApproval": true},
    {"key": "justification", "label": "Lý do công việc", "type": "text", "required": true, "maxLength": 500, "affectsApproval": true}
  ]
}'::jsonb, 1, 1, NOW());

INSERT INTO workflow_steps (id, workflow_version_id, step_key, step_name, display_order, assignee_type, sla_hours, allow_request_changes) OVERRIDING SYSTEM VALUE VALUES
(5, 3, 'DEPT_MANAGER_APPROVAL', 'Trưởng bộ phận phê duyệt', 1, 'DEPARTMENT_MANAGER', 24, true),
(6, 3, 'IT_SUPPORT_APPROVAL', 'Đội IT cấp quyền', 2, 'GROUP', 48, true);

UPDATE workflow_steps SET assignee_group_id = 3 WHERE id = 6; -- IT_SUPPORT group
UPDATE workflow_versions SET start_step_id = 5 WHERE id = 3;

INSERT INTO workflow_transitions (id, workflow_version_id, from_step_id, target_type, to_step_id, condition_expression, is_default, priority) OVERRIDING SYSTEM VALUE VALUES
(6, 3, 5, 'STEP', 6, '{}'::jsonb, true, 1),
(7, 3, 6, 'END_APPROVED', NULL, '{}'::jsonb, true, 1);
