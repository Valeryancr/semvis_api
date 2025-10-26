-- =========================================================
-- V4__ajuste_trigger_cliente_nombre.sql
-- Ajuste: sincronizar nombre del cliente solo con primer_nombre + apellido1
-- =========================================================

-- 1️⃣ Eliminar el trigger anterior (si existe)
DROP TRIGGER IF EXISTS trg_user_sync_cliente ON semvis_sk."user";

-- 2️⃣ Redefinir la función con nueva lógica
CREATE OR REPLACE FUNCTION semvis_sk.sync_cliente_nombre()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  UPDATE semvis_sk.cliente c
  SET nombre = btrim(coalesce(NEW.primer_nombre, '') || ' ' || coalesce(NEW.apellido1, ''))
  WHERE c.user_id = NEW.id;
  RETURN NEW;
END$$;

-- 3️⃣ Crear nuevamente el trigger con la función actualizada
CREATE TRIGGER trg_user_sync_cliente
AFTER UPDATE OF primer_nombre, apellido1
ON semvis_sk."user"
FOR EACH ROW EXECUTE FUNCTION semvis_sk.sync_cliente_nombre();