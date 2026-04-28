-- seed_clientes.sql
-- Seed mínimo para cliente-service (Personas + Clientes)
-- ATENCIÓN: Reemplazar los placeholders de BCrypt ('$2a$10$HASH_PLACEHOLDER')
-- por hashes reales generados antes de ejecutar este seed (ver nota al final).

-- Personas
INSERT INTO persona (persona_id, nombre, genero, edad, identificacion, direccion, telefono, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'Jose Lema',           NULL, NULL, 'ID-001', 'Otavalo sn y principal', '098254785', now(), now()),
  (gen_random_uuid(), 'Marianela Montalvo',  NULL, NULL, 'ID-002', 'Amazonas y NNUU',        '097548965', now(), now()),
  (gen_random_uuid(), 'Juan Osorio',         NULL, NULL, 'ID-003', '13 junio y Equinoccial', '098874587', now(), now())
ON CONFLICT DO NOTHING;

-- Clientes (referencia persona por identificacion)
-- Nota: la columna en el schema es `contrasena_hash` (hash BCrypt esperado)
INSERT INTO cliente (persona_id, contrasena_hash, estado, created_at, updated_at)
SELECT p.persona_id,
       '$2a$10$HASH_PLACEHOLDER',  -- BCrypt de 1234/5678/1245 según cliente
       true,
       now(),
       now()
FROM persona p
WHERE p.identificacion IN ('ID-001','ID-002','ID-003')
ON CONFLICT DO NOTHING;

-- Nota final:
-- Los hashes BCrypt reales de las contraseñas (p.ej. 1234, 5678, 1245) deben generarse
-- previamente (por ejemplo con bcrypt-cli, una pequeña util en Node/Python o bcrypttools)
-- y reemplazar '$2a$10$HASH_PLACEHOLDER' en las filas correspondientes antes de ejecutar.
-- El script usa ON CONFLICT DO NOTHING para ser idempotente.
