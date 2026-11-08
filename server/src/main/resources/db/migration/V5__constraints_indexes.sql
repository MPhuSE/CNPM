-- V5__constraints_indexes.sql
-- Check constraints and partial indexes specified in FlowOps design

-- Check constraints
ALTER TABLE workflow_versions
    ADD CONSTRAINT chk_workflow_version_status
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));

ALTER TABLE workflow_steps
    ADD CONSTRAINT chk_step_assignee_type
    CHECK (assignee_type IN ('USER', 'GROUP', 'REQUESTER_MANAGER', 'DEPARTMENT_MANAGER'));

ALTER TABLE workflow_transitions
    ADD CONSTRAINT chk_transition_target_type
    CHECK (target_type IN ('STEP', 'END_APPROVED'));

ALTER TABLE requests
    ADD CONSTRAINT chk_request_status
    CHECK (status IN ('DRAFT', 'PENDING', 'RETURNED', 'APPROVED', 'REJECTED', 'CANCELLED'));

ALTER TABLE tasks
    ADD CONSTRAINT chk_task_status
    CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'));

ALTER TABLE tasks
    ADD CONSTRAINT chk_task_assignment_state
    CHECK (assignment_state IN ('ASSIGNED', 'GROUP_OPEN', 'NEEDS_ASSIGNMENT'));

ALTER TABLE workflow_incidents
    ADD CONSTRAINT chk_incident_status
    CHECK (status IN ('OPEN', 'RESOLVED', 'CLOSED_CANCELLED'));

-- Partial unique indexes
CREATE UNIQUE INDEX uq_users_lower_email ON users (LOWER(email));

-- One DRAFT per workflow definition
CREATE UNIQUE INDEX uq_one_draft_version ON workflow_versions (workflow_definition_id)
    WHERE status = 'DRAFT';

-- One PUBLISHED per workflow definition
CREATE UNIQUE INDEX uq_one_published_version ON workflow_versions (workflow_definition_id)
    WHERE status = 'PUBLISHED';

-- One PENDING task per request
CREATE UNIQUE INDEX uq_one_pending_task_per_request ON tasks (request_id)
    WHERE status = 'PENDING';

-- One default transition per step
CREATE UNIQUE INDEX uq_one_default_transition ON workflow_transitions (from_step_id)
    WHERE is_default = TRUE;

-- One OPEN incident per request & incident_type
CREATE UNIQUE INDEX uq_one_open_incident_per_request ON workflow_incidents (request_id, incident_type)
    WHERE status = 'OPEN';

-- One TASK_OVERDUE notification per task & user
CREATE UNIQUE INDEX uq_task_overdue_notification ON notifications (task_id, user_id)
    WHERE notification_type = 'TASK_OVERDUE';

-- Performance indexes for filtering and queries
CREATE INDEX idx_requests_requester_status ON requests (requester_id, status, first_submitted_at);
CREATE INDEX idx_requests_status_submitted ON requests (status, first_submitted_at);
CREATE INDEX idx_tasks_assignee_status_due ON tasks (assignee_id, status, due_at);
CREATE INDEX idx_tasks_group_status_due ON tasks (candidate_group_id, status, due_at);
CREATE INDEX idx_request_events_request_created ON request_events (request_id, created_at);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id, created_at);
CREATE INDEX idx_audit_logs_actor ON audit_logs (actor_id, created_at);
CREATE INDEX idx_notifications_user_read ON notifications (user_id, read_at, created_at);
