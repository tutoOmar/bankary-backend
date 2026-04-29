INSERT INTO persona (persona_id, nombre, identificacion, direccion, telefono)
VALUES
  ('a1b2c3d4-0001-0001-0001-000000000001', 'Jose Lema',         'ID-001', 'Otavalo sn y principal',  '098254785'),
  ('a1b2c3d4-0002-0002-0002-000000000002', 'Marianela Montalvo','ID-002', 'Amazonas y NNUU',         '097548965'),
  ('a1b2c3d4-0003-0003-0003-000000000003', 'Juan Osorio',       'ID-003', '13 junio y Equinoccial',  '098874587')
ON CONFLICT DO NOTHING;

INSERT INTO cliente (persona_id, contrasena, estado)
VALUES
  ('a1b2c3d4-0001-0001-0001-000000000001', '$2a$10$1Y8CjL1J2x5H7a0B3n6M4.v5c8x3n9A6b4d2e7h5j8k1m3p4q6r8s', true),
  ('a1b2c3d4-0002-0002-0002-000000000002', '$2a$10$1Y8CjL1J2x5H7a0B3n6M4.v5c8x3n9A6b4d2e7h5j8k1m3p4q6r8s', true),
  ('a1b2c3d4-0003-0003-0003-000000000003', '$2a$10$1Y8CjL1J2x5H7a0B3n6M4.v5c8x3n9A6b4d2e7h5j8k1m3p4q6r8s', true)
ON CONFLICT DO NOTHING;
