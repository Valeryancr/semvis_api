-- ============================================================
-- V5__add_force_password_change_and_cliente_telefono.sql
-- ============================================================
-- Autor: SkyNet / Proyecto SemVis
-- Fecha: 2025-10-24
-- Objetivo:
--  - Agregar campo must_change_password a la tabla semvis_sk.user
--  - Asegurar columna telefono exista en semvis_sk.cliente
--  - Mantener consistencia con AuditFields
-- ============================================================

-- ===============================
-- 1️⃣ Campo must_change_password
-- ===============================
ALTER TABLE semvis_sk."user"
ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN semvis_sk."user".must_change_password IS
'Indica si el usuario debe cambiar su contraseña temporal al iniciar sesión.';

-- ===============================
-- 2️⃣ Campo telefono en cliente
-- ===============================
ALTER TABLE semvis_sk.cliente
ADD COLUMN IF NOT EXISTS telefono TEXT;

COMMENT ON COLUMN semvis_sk.cliente.telefono IS
'Número de contacto telefónico del cliente.';