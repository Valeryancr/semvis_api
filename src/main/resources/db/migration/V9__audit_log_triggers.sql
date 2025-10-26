-- flyway:executeInTransaction=false

-- =========================================================
-- V9__create_audit_triggers.sql
-- Crea triggers universales de auditoría para todas las tablas
-- del esquema semvis_sk, excepto audit_log
-- =========================================================

SET search_path TO semvis_sk, public;

DO $$
DECLARE
    r RECORD;
    trg_name TEXT;
BEGIN
    FOR r IN
        SELECT table_schema, table_name
        FROM information_schema.tables
        WHERE table_schema = 'semvis_sk'
          AND table_type = 'BASE TABLE'
          AND table_name NOT IN ('audit_log', 'flyway_schema_history')
    LOOP
        trg_name := 'trg_audit_' || r.table_name;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_trigger t
            JOIN pg_class c ON t.tgrelid = c.oid
            JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE n.nspname = r.table_schema
              AND c.relname = r.table_name
              AND t.tgname = trg_name
        ) THEN
            EXECUTE format(
                'CREATE TRIGGER %I
                   AFTER INSERT OR UPDATE OR DELETE
                   ON %I.%I
                   FOR EACH ROW
                   EXECUTE FUNCTION semvis_sk.audit_trigger_func();',
                trg_name, r.table_schema, r.table_name
            );
            RAISE NOTICE  'Trigger creado: % para tabla %.%', trg_name, r.table_schema, r.table_name;
        ELSE
            RAISE NOTICE 'Trigger % ya existía en %.%', trg_name, r.table_schema, r.table_name;
        END IF;
    END LOOP;
END $$;