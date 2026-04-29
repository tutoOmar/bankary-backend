-- seed_cuentas.sql
-- ClienteSnapshot (replica mínima recibida vía evento, precargada para dev)
INSERT INTO cliente_snapshot (cliente_id, nombre, last_updated)
VALUES
  ('UUID-JOSE',       'Jose Lema',         NOW()),
  ('UUID-MARIANELA',  'Marianela Montalvo', NOW()),
  ('UUID-JUAN',       'Juan Osorio',        NOW())
ON CONFLICT DO NOTHING;

-- Cuentas
INSERT INTO cuenta (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id)
VALUES
  (gen_random_uuid(), '478758', 'AHORRO',    2000, 2000, true, 'UUID-JOSE'),
  (gen_random_uuid(), '225487', 'CORRIENTE',  100,  100, true, 'UUID-MARIANELA'),
  (gen_random_uuid(), '495878', 'AHORRO',       0,    0, true, 'UUID-JUAN'),
  (gen_random_uuid(), '496825', 'AHORRO',     540,  540, true, 'UUID-MARIANELA')
ON CONFLICT DO NOTHING;

-- Nota: los UUID-JOSE, UUID-MARIANELA, UUID-JUAN deben ser los mismos UUIDs generados en seed_clientes.sql.
-- Actualizar tras ejecutar seed de cliente-service.
