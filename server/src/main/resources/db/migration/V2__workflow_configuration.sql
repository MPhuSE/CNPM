-- V2__workflow_configuration.sql
-- Module M2: Workflow Configuration (4 tables)

CREATE TABLE workflow_definitions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE workflow_versions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workflow_definition_id BIGINT NOT NULL REFERENCES workflow_definitions(id),
    version_no INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    form_schema JSONB NOT NULL,
    start_step_id BIGINT,
    change_note TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    published_by BIGINT REFERENCES users(id),
    published_at TIMESTAMPTZ,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workflow_versions_def_version UNIQUE (workflow_definition_id, version_no)
);

CREATE TABLE workflow_steps (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workflow_version_id BIGINT NOT NULL REFERENCES workflow_versions(id),
    step_key VARCHAR(100) NOT NULL,
    step_name VARCHAR(200) NOT NULL,
    display_order INTEGER NOT NULL,
    assignee_type VARCHAR(30) NOT NULL,
    assignee_user_id BIGINT REFERENCES users(id),
    assignee_group_id BIGINT REFERENCES approval_groups(id),
    sla_hours INTEGER,
    allow_request_changes BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_workflow_steps_id_version UNIQUE (id, workflow_version_id),
    CONSTRAINT uq_workflow_steps_version_key UNIQUE (workflow_version_id, step_key),
    CONSTRAINT uq_workflow_steps_version_order UNIQUE (workflow_version_id, display_order)
);

ALTER TABLE workflow_versions
    ADD CONSTRAINT fk_workflow_versions_start_step
    FOREIGN KEY (start_step_id, id) REFERENCES workflow_steps(id, workflow_version_id);

CREATE TABLE workflow_transitions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workflow_version_id BIGINT NOT NULL REFERENCES workflow_versions(id),
    from_step_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    to_step_id BIGINT,
    condition_expression JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    priority INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_transitions_from_step_priority UNIQUE (from_step_id, priority),
    CONSTRAINT fk_transitions_from_step FOREIGN KEY (from_step_id, workflow_version_id) REFERENCES workflow_steps(id, workflow_version_id),
    CONSTRAINT fk_transitions_to_step FOREIGN KEY (to_step_id, workflow_version_id) REFERENCES workflow_steps(id, workflow_version_id)
);
