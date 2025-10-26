-- =========================================================
-- V3__add_audit_fields_to_refresh_token.sql
-- Añade columnas de auditoría a la tabla refresh_token
-- para mantener consistencia con AuditFields.java
-- =========================================================

ALTER TABLE semvis_sk.refresh_token
ADD COLUMN IF NOT EXISTS created_by citext,
ADD COLUMN IF NOT EXISTS updated_by citext,
ADD COLUMN IF NOT EXISTS created_at timestamptz DEFAULT NOW() NOT NULL,
ADD COLUMN IF NOT EXISTS updated_at timestamptz DEFAULT NOW() NOT NULL;

-- (Opcional) Crea trigger de auditoría igual al resto
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_refresh_token_audit') THEN
    CREATE TRIGGER trg_refresh_token_audit
    BEFORE INSERT OR UPDATE ON semvis_sk.refresh_token
    FOR EACH ROW EXECUTE FUNCTION semvis_sk.audit_touch_both_username();
  END IF;
END$$;