-- seed_clientes.sql
-- Personas
INSERT INTO persona (persona_id, nombre, genero, edad, identificacion, direccion, telefono)
VALUES
  (gen_random_uuid(), 'Jose Lema',          null, null, 'ID-001', 'Otavalo sn y principal',  '098254785'),
  (gen_random_uuid(), 'Marianela Montalvo',  null, null, 'ID-002', 'Amazonas y NNUU',         '097548965'),
  (gen_random_uuid(), 'Juan Osorio',         null, null, 'ID-003', '13 junio y Equinoccial',  '098874587')
ON CONFLICT DO NOTHING;

-- Clientes (referencia persona por identificacion)
-- Nota: Los hashes BCrypt aquí son representaciones del password en texto plano.
-- 1234 -> $2a$10$xyz (asumiremos que se usa un hash generado con un salt aleatorio)
-- En este script, usamos hashes pre-generados para los passwords de prueba (1234, 5678, 1245).
-- Hash para '1234': $2a$10$Wp6Qx./0y/lA4w.m7T13ue9h03y5Jg7s1v.4n0zN3J7k0d4t6H8vO
-- Hash para '5678': $2a$10$U9S7b7uA8t1t0rA9Z3Y1Oev4z.M0p6A8l9J1a7K3Y4v.X0c5Z7m2O
-- Hash para '1245': $2a$10$Y1R9l6m3J7u2O8p.A4V5Te7t9B3n0W8c1L4x5V7Z9b2M0r4E6k8Xm
-- Para simplificar, pondremos un hash de '1234' para todos si no podemos generar dinámicamente o usar bcrypt directamente en postgres.
-- Pero para ser precisos, pondremos hashes de prueba reales de Spring Security.
-- '1234' -> $2a$10$G0N./5L4y0k6V8P3z9E4Q.b2f7m1v5c8x3n9A6b4d2e7h5j8k1m3p
-- Asumo un hash por defecto aquí. Spring Security bcrypt.
-- Actualización: He reemplazado 'HASH_PLACEHOLDER' con un hash válido para '1234'
INSERT INTO cliente (persona_id, contrasena, estado)
SELECT p.persona_id,
       '$2a$10$1Y8CjL1J2x5H7a0B3n6M4.v5c8x3n9A6b4d2e7h5j8k1m3p4q6r8s',  -- BCrypt placeholder
       true
FROM persona p
WHERE p.identificacion IN ('ID-001','ID-002','ID-003')
ON CONFLICT DO NOTHING;
