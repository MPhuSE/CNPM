-- V6__immutable_guards.sql
-- Trigger functions ensuring immutability for published versions and append-only tables

-- 1. Guard published versions
CREATE OR REPLACE FUNCTION trg_guard_published_version()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = 'PUBLISHED' AND NEW.status = 'DRAFT' THEN
        RAISE EXCEPTION 'Cannot revert PUBLISHED workflow version back to DRAFT.';
    END IF;
    IF OLD.status = 'ARCHIVED' AND NEW.status != 'ARCHIVED' THEN
        RAISE EXCEPTION 'Cannot modify ARCHIVED workflow version.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_workflow_versions_guard
    BEFORE UPDATE ON workflow_versions
    FOR EACH ROW
    EXECUTE FUNCTION trg_guard_published_version();

-- 2. Guard append-only on request_revisions
CREATE OR REPLACE FUNCTION trg_guard_append_only_revisions()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        RAISE EXCEPTION 'Table request_revisions is immutable (INSERT ONLY).';
    ELSIF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'Table request_revisions is immutable (Cannot delete revisions).';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_request_revisions_append_only
    BEFORE UPDATE OR DELETE ON request_revisions
    FOR EACH ROW
    EXECUTE FUNCTION trg_guard_append_only_revisions();

-- 3. Guard append-only on audit_logs
CREATE OR REPLACE FUNCTION trg_guard_append_only_audit()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Table audit_logs is append-only.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_logs_append_only
    BEFORE UPDATE OR DELETE ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION trg_guard_append_only_audit();

-- 4. Guard append-only on request_events
CREATE OR REPLACE FUNCTION trg_guard_append_only_events()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Table request_events is append-only.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_request_events_append_only
    BEFORE UPDATE OR DELETE ON request_events
    FOR EACH ROW
    EXECUTE FUNCTION trg_guard_append_only_events();
