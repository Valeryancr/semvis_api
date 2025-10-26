-- =========================================================
-- V2__fix_audit_citext.sql
-- Corrige columnas auditadas que quedaron con tipo USER-DEFINED
-- y las normaliza a public.citext
-- =========================================================

SET search_path TO semvis_sk;

-- Corrige columnas auditadas
DO $$
DECLARE 
    r RECORD;
BEGIN
    FOR r IN
        SELECT table_schema, table_name, column_name
        FROM information_schema.columns
        WHERE table_schema = 'semvis_sk'
          AND column_name IN ('created_by', 'updated_by')
          AND udt_name <> 'citext'
    LOOP
        EXECUTE format('ALTER TABLE %I.%I ALTER COLUMN %I TYPE citext USING %I::citext;',
                       r.table_schema, r.table_name, r.column_name, r.column_name);
        RAISE NOTICE 'Columna corregida: %.%', r.table_name, r.column_name;
    END LOOP;
END $$;