-- =========================================================
-- V6__add_filename_and_uploaded_by_to_visita_reporte.sql
-- =========================================================

ALTER TABLE semvis_sk.visita_reporte
ADD COLUMN IF NOT EXISTS nombre_archivo TEXT,
ADD COLUMN IF NOT EXISTS usuario_carga_id BIGINT REFERENCES semvis_sk."user"(id) ON DELETE SET NULL;