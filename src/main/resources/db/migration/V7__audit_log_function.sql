-- =========================================================
-- V7__audit_log_function.sql
-- Función universal de auditoría con integración JPA + Spring Security
-- =========================================================

SET search_path TO semvis_sk, public;

-- =========================================================
-- 1. Función auxiliar para leer usuario desde el contexto de sesión
-- =========================================================
CREATE OR REPLACE FUNCTION semvis_sk.current_app_username()
RETURNS CITEXT
LANGUAGE plpgsql STABLE AS $$
DECLARE
    v CITEXT;
BEGIN
    BEGIN
        v := current_setting('semvis_sk.app_username')::citext;
    EXCEPTION WHEN others THEN
        v := NULL;
    END;
    RETURN v;
END;
$$;


-- =========================================================
-- 2. Función principal de auditoría
-- =========================================================
CREATE OR REPLACE FUNCTION semvis_sk.audit_trigger_func()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user       CITEXT;
    v_ip         INET;
    v_entity     TEXT := TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME;
    v_entity_id  BIGINT;
    ignored_cols TEXT[] := ARRAY['created_at', 'updated_at', 'created_by', 'updated_by'];
    jnew         JSONB;
    jold         JSONB;
    jchanged     JSONB := '{}'::jsonb;
    jdetails     JSONB := '{}'::jsonb;
BEGIN
    -- =====================================================
    -- Obtener el usuario del contexto (orden de prioridad):
    -- 1. Campos de la entidad (JPA AuditorAware)
    -- 2. Variable de sesión (SET LOCAL semvis_sk.app_username)
    -- 3. Usuario actual de PostgreSQL
    -- =====================================================
    v_user := COALESCE(
        COALESCE(NEW.updated_by, OLD.updated_by),
        COALESCE(NEW.created_by, OLD.created_by),
        semvis_sk.current_app_username(),
        current_user::citext
    );

    -- =====================================================
    -- Obtener la IP desde la sesión si fue seteada
    -- =====================================================
    BEGIN
        v_ip := current_setting('semvis_sk.app_ip')::inet;
    EXCEPTION WHEN others THEN
        v_ip := NULL;
    END;

    -- =====================================================
    -- Procesar según la operación
    -- =====================================================
    IF TG_OP = 'INSERT' THEN
        jnew := to_jsonb(NEW);
        v_entity_id := COALESCE((NEW).id, (NEW).visita_id, (NEW).user_id, NULL);

        jdetails := jsonb_build_object(
            'row_data', jnew - ignored_cols
        );

    ELSIF TG_OP = 'UPDATE' THEN
        jnew := to_jsonb(NEW);
        jold := to_jsonb(OLD);
        v_entity_id := COALESCE((NEW).id, (NEW).visita_id, (NEW).user_id, NULL);

        -- Solo los campos que cambiaron (ignorando audit fields)
        jchanged := (
            SELECT COALESCE(jsonb_object_agg(k, jnew->k), '{}'::jsonb)
            FROM (
                SELECT key AS k
                FROM jsonb_each(jnew - ignored_cols)
                WHERE (jnew->key) IS DISTINCT FROM (jold->key)
            ) s
        );

        jdetails := jsonb_build_object(
            'old', jold - ignored_cols,
            'new', jnew - ignored_cols,
            'changed_fields', jchanged
        );

    ELSIF TG_OP = 'DELETE' THEN
        jold := to_jsonb(OLD);
        v_entity_id := COALESCE((OLD).id, (OLD).visita_id, (OLD).user_id, NULL);
        jdetails := jsonb_build_object(
            'row_data', jold - ignored_cols
        );
    END IF;

    -- =====================================================
    -- Insertar registro en audit_log
    -- =====================================================
    INSERT INTO semvis_sk.audit_log (
        usuario, accion, entidad, entidad_id, ts, ip, detalles
    )
    VALUES (
        v_user,
        TG_OP,
        v_entity,
        v_entity_id,
        NOW(),
        v_ip,
        jdetails
    );

    RETURN COALESCE(NEW, OLD);
END;
$$;