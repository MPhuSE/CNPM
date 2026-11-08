-- V4__operations.sql
-- Module M5: Operations, Audit, Notifications, Incidents, Receipts (4 tables)

CREATE TABLE audit_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_key UUID NOT NULL UNIQUE,
    actor_id BIGINT REFERENCES users(id),
    request_id BIGINT REFERENCES requests(id),
    revision_id BIGINT,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notifications (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_key UUID NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    notification_type VARCHAR(40) NOT NULL,
    request_id BIGINT REFERENCES requests(id),
    task_id BIGINT,
    title VARCHAR(250) NOT NULL,
    message TEXT,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_notifications_event_user_type UNIQUE (event_key, user_id, notification_type)
);

CREATE TABLE workflow_incidents (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id BIGINT NOT NULL,
    revision_id BIGINT NOT NULL,
    workflow_version_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    task_id BIGINT,
    incident_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    operation_key UUID NOT NULL,
    error_code VARCHAR(100) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    resolved_by BIGINT REFERENCES users(id),
    resolution_reason TEXT,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    CONSTRAINT uq_incidents_op_type UNIQUE (operation_key, incident_type),
    CONSTRAINT fk_incidents_request_ver FOREIGN KEY (request_id, workflow_version_id) REFERENCES requests(id, workflow_version_id),
    CONSTRAINT fk_incidents_step_ver FOREIGN KEY (step_id, workflow_version_id) REFERENCES workflow_steps(id, workflow_version_id),
    CONSTRAINT fk_incidents_revision FOREIGN KEY (revision_id, request_id) REFERENCES request_revisions(id, request_id),
    CONSTRAINT fk_incidents_task FOREIGN KEY (task_id, request_id) REFERENCES tasks(id, request_id)
);

CREATE TABLE command_receipts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    actor_id BIGINT NOT NULL REFERENCES users(id),
    command_type VARCHAR(40) NOT NULL,
    client_key UUID NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    request_id BIGINT REFERENCES requests(id),
    task_id BIGINT,
    response_code INTEGER,
    response_body JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT uq_command_receipts UNIQUE (actor_id, command_type, client_key)
);
