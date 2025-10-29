-- =========================================================
-- V10__add_audit_fields_to_user_role.sql
-- Agrega campos de auditoría a la tabla user_role
-- para compatibilidad con triggers universales de auditoría
-- =========================================================

SET search_path TO semvis_sk, public;

ALTER TABLE semvis_sk.user_role
    ADD COLUMN IF NOT EXISTS created_by CITEXT,
    ADD COLUMN IF NOT EXISTS updated_by CITEXT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

COMMENT ON COLUMN semvis_sk.user_role.created_by IS 'Usuario que creó el registro';
COMMENT ON COLUMN semvis_sk.user_role.updated_by IS 'Último usuario que modificó el registro';
COMMENT ON COLUMN semvis_sk.user_role.created_at IS 'Fecha de creación del registro';
COMMENT ON COLUMN semvis_sk.user_role.updated_at IS 'Fecha de última modificación del registro';

-- Actualizar valores existentes (opcional)
UPDATE semvis_sk.user_role
   SET created_at = NOW(), updated_at = NOW(),
       created_by = COALESCE(created_by, 'system'),
       updated_by = COALESCE(updated_by, 'system');