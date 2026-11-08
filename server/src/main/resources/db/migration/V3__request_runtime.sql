-- V3__request_runtime.sql
-- Module M3 & M4: Requests and Workflow Runtime (6 tables)

CREATE TABLE requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_code VARCHAR(100) UNIQUE,
    workflow_version_id BIGINT NOT NULL REFERENCES workflow_versions(id),
    requester_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(250) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    source_department_id BIGINT REFERENCES departments(id),
    source_manager_id BIGINT REFERENCES users(id),
    source_department_manager_id BIGINT REFERENCES users(id),
    source_context JSONB,
    current_revision_id BIGINT,
    current_step_id BIGINT,
    first_submitted_at TIMESTAMPTZ,
    last_submitted_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancel_reason TEXT,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_requests_id_version UNIQUE (id, workflow_version_id)
);

CREATE TABLE request_revisions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES requests(id),
    revision_no INTEGER NOT NULL,
    form_data JSONB NOT NULL,
    changed_fields JSONB NOT NULL DEFAULT '[]'::jsonb,
    restart_required BOOLEAN NOT NULL,
    resume_step_id BIGINT NOT NULL REFERENCES workflow_steps(id),
    submitted_by BIGINT NOT NULL REFERENCES users(id),
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_request_revisions_req_no UNIQUE (request_id, revision_no),
    CONSTRAINT uq_request_revisions_id_req UNIQUE (id, request_id)
);

ALTER TABLE requests
    ADD CONSTRAINT fk_requests_current_revision
    FOREIGN KEY (current_revision_id, id) REFERENCES request_revisions(id, request_id);

ALTER TABLE requests
    ADD CONSTRAINT fk_requests_current_step
    FOREIGN KEY (current_step_id, workflow_version_id) REFERENCES workflow_steps(id, workflow_version_id);

CREATE TABLE request_edits (
    request_id BIGINT PRIMARY KEY REFERENCES requests(id),
    base_revision_id BIGINT,
    form_data JSONB NOT NULL,
    updated_by BIGINT NOT NULL REFERENCES users(id),
    lock_version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_request_edits_base_revision FOREIGN KEY (base_revision_id, request_id) REFERENCES request_revisions(id, request_id)
);

CREATE TABLE tasks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id BIGINT NOT NULL,
    workflow_version_id BIGINT NOT NULL,
    workflow_step_id BIGINT NOT NULL,
    revision_id BIGINT NOT NULL,
    assignment_state VARCHAR(20) NOT NULL,
    assignee_id BIGINT REFERENCES users(id),
    candidate_group_id BIGINT REFERENCES approval_groups(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    action_taken VARCHAR(30),
    decision_reason TEXT,
    claimed_at TIMESTAMPTZ,
    completed_by BIGINT REFERENCES users(id),
    completed_at TIMESTAMPTZ,
    due_at TIMESTAMPTZ,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tasks_id_request UNIQUE (id, request_id),
    CONSTRAINT fk_tasks_request_version FOREIGN KEY (request_id, workflow_version_id) REFERENCES requests(id, workflow_version_id),
    CONSTRAINT fk_tasks_step_version FOREIGN KEY (workflow_step_id, workflow_version_id) REFERENCES workflow_steps(id, workflow_version_id),
    CONSTRAINT fk_tasks_revision FOREIGN KEY (revision_id, request_id) REFERENCES request_revisions(id, request_id)
);

CREATE TABLE revision_approvals (
    request_id BIGINT NOT NULL,
    revision_id BIGINT NOT NULL,
    workflow_step_id BIGINT NOT NULL REFERENCES workflow_steps(id),
    source_task_id BIGINT NOT NULL,
    is_inherited BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (revision_id, workflow_step_id),
    CONSTRAINT fk_revision_approvals_rev FOREIGN KEY (revision_id, request_id) REFERENCES request_revisions(id, request_id),
    CONSTRAINT fk_revision_approvals_task FOREIGN KEY (source_task_id, request_id) REFERENCES tasks(id, request_id)
);

CREATE TABLE request_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_key UUID NOT NULL UNIQUE,
    request_id BIGINT NOT NULL REFERENCES requests(id),
    revision_id BIGINT,
    task_id BIGINT,
    actor_id BIGINT REFERENCES users(id),
    event_type VARCHAR(40) NOT NULL,
    reason TEXT,
    old_assignee_id BIGINT REFERENCES users(id),
    new_assignee_id BIGINT REFERENCES users(id),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_request_events_revision FOREIGN KEY (revision_id, request_id) REFERENCES request_revisions(id, request_id),
    CONSTRAINT fk_request_events_task FOREIGN KEY (task_id, request_id) REFERENCES tasks(id, request_id)
);
