INSERT INTO cliente_snapshot (cliente_id, nombre)
VALUES
  ('a1b2c3d4-0001-0001-0001-000000000001', 'Jose Lema'),
  ('a1b2c3d4-0002-0002-0002-000000000002', 'Marianela Montalvo'),
  ('a1b2c3d4-0003-0003-0003-000000000003', 'Juan Osorio')
ON CONFLICT DO NOTHING;

INSERT INTO cuenta (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id)
VALUES
  (gen_random_uuid(), '478758', 'AHORRO',    2000, 2000, true, 'a1b2c3d4-0001-0001-0001-000000000001'),
  (gen_random_uuid(), '225487', 'CORRIENTE',  100,  100, true, 'a1b2c3d4-0002-0002-0002-000000000002'),
  (gen_random_uuid(), '495878', 'AHORRO',       0,    0, true, 'a1b2c3d4-0003-0003-0003-000000000003'),
  (gen_random_uuid(), '496825', 'AHORRO',     540,  540, true, 'a1b2c3d4-0002-0002-0002-000000000002')
ON CONFLICT DO NOTHING;
