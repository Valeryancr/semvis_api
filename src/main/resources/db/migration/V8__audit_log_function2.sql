-- =========================================================
-- audit_trigger_func()
-- Auditoría universal: registra solo los cambios (OLD → NEW)
-- Compatible con AuditorAware (Spring JPA)
-- =========================================================

CREATE OR REPLACE FUNCTION semvis_sk.audit_trigger_func()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_user       CITEXT;
    v_ip         INET;
    v_entity     TEXT := TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME;
    v_entity_id  BIGINT;
    ignored_cols TEXT[] := ARRAY['created_at', 'updated_at', 'created_by', 'updated_by'];
    jnew         JSONB;
    jold         JSONB;
    jdetails     JSONB := '{}'::jsonb;
BEGIN
    -- =====================================================
    -- 1. Usuario de auditoría
    -- =====================================================
    v_user := COALESCE(
        COALESCE(NEW.updated_by, OLD.updated_by),
        COALESCE(NEW.created_by, OLD.created_by),
        semvis_sk.current_app_username(),
        current_user::citext
    );

    -- =====================================================
    -- 2. Dirección IP del cliente (si fue seteada en la sesión)
    -- =====================================================
    BEGIN
        v_ip := current_setting('semvis_sk.app_ip')::inet;
    EXCEPTION WHEN others THEN
        v_ip := NULL;
    END;

    -- =====================================================
    -- 3. Procesar según la operación
    -- =====================================================

    -- INSERT: guarda solo el nuevo registro
    IF TG_OP = 'INSERT' THEN
        jnew := to_jsonb(NEW);
        v_entity_id := COALESCE(
            (to_jsonb(NEW)->>'id')::bigint,
            (to_jsonb(NEW)->>'visita_id')::bigint,
            (to_jsonb(NEW)->>'user_id')::bigint
        );
        jdetails := jsonb_build_object('new', jnew - ignored_cols);

    -- UPDATE: guarda únicamente los campos que cambiaron
    ELSIF TG_OP = 'UPDATE' THEN
        jnew := to_jsonb(NEW);
        jold := to_jsonb(OLD);
        v_entity_id := COALESCE(
            (to_jsonb(NEW)->>'id')::bigint,
            (to_jsonb(NEW)->>'visita_id')::bigint,
            (to_jsonb(NEW)->>'user_id')::bigint,
            (to_jsonb(OLD)->>'id')::bigint,
            (to_jsonb(OLD)->>'visita_id')::bigint,
            (to_jsonb(OLD)->>'user_id')::bigint
        );

        jdetails := (
            SELECT jsonb_object_agg(key,
                jsonb_build_object(
                    'old', jold->key,
                    'new', jnew->key
                )
            )
            FROM jsonb_each(jnew - ignored_cols)
            WHERE (jnew->key) IS DISTINCT FROM (jold->key)
        );

        jdetails := COALESCE(jdetails, '{}'::jsonb);

    -- DELETE: guarda solo los datos eliminados
    ELSIF TG_OP = 'DELETE' THEN
        jold := to_jsonb(OLD);
        v_entity_id := COALESCE(
            (to_jsonb(OLD)->>'id')::bigint,
            (to_jsonb(OLD)->>'visita_id')::bigint,
            (to_jsonb(OLD)->>'user_id')::bigint
        );
        jdetails := jsonb_build_object('old', jold - ignored_cols);
    END IF;

    -- =====================================================
    -- 4. Insertar registro en audit_log
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
$function$;