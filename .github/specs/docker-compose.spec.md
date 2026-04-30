---
id: SPEC-003
status: IMPLEMENTED
feature: docker-compose
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002]
---

# Spec: docker-compose — Infra local para prueba técnica

## 1. REQUERIMIENTOS
Levantar localmente:
- cliente-service (puerto 8080)
- cuenta-service (puerto 8081)
- PostgreSQL para cliente-service (puerto 5432)
- PostgreSQL para cuenta-service (puerto 5433)
- RabbitMQ con management UI (puertos 5672 y 15672)
- Red interna compartida entre servicios
- Maven build para cada servicio, Dockerfile por servicio

## 2. DISEÑO / CONFIGURACIÓN RECOMENDADA

docker-compose versión 3.8 (archivo en la raíz `docker-compose.yml`).

Servicios mínimos:

- rabbitmq:
  - image: rabbitmq:3-management
  - ports: "5672:5672", "15672:15672"
  - environment: RABBITMQ_DEFAULT_USER=guest, RABBITMQ_DEFAULT_PASS=guest
  - healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
  - networks: pruebatecnica-net

- postgres-cliente:
  - image: postgres:15
  - environment: POSTGRES_DB=cliente_db, POSTGRES_USER=user, POSTGRES_PASSWORD=pass
  - ports: "5432:5432"
  - healthcheck:
    test: ["CMD-SHELL", "pg_isready -U user -d cliente_db"]
    interval: 10s
    timeout: 5s
    retries: 5
  - volumes:
    - cliente-db-data
    - ./BaseDatos.sql:/docker-entrypoint-initdb.d/01_schema.sql
    - ./seed_clientes.sql:/docker-entrypoint-initdb.d/02_seed.sql
  - networks: pruebatecnica-net

- postgres-cuenta:
  - image: postgres:15
  - environment: POSTGRES_DB=cuenta_db, POSTGRES_USER=user, POSTGRES_PASSWORD=pass
  - ports: "5433:5432"
  - healthcheck:
    test: ["CMD-SHELL", "pg_isready -U user -d cuenta_db"]
    interval: 10s
    timeout: 5s
    retries: 5
  - volumes:
    - cuenta-db-data
    - ./seed_cuentas.sql:/docker-entrypoint-initdb.d/02_seed.sql
  - networks: pruebatecnica-net

- cliente-service:
  - build: ./cliente-service
  - ports: "8080:8080"
  - restart: on-failure
  - environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-cliente:5432/cliente_db
    - SPRING_DATASOURCE_USERNAME=user
    - SPRING_DATASOURCE_PASSWORD=pass
    - SPRING_RABBITMQ_HOST=rabbitmq
  - depends_on:
    postgres-cliente:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
  - networks: pruebatecnica-net

- cuenta-service:
  - build: ./cuenta-service
  - ports: "8081:8081"
  - restart: on-failure
  - environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-cuenta:5432/cuenta_db
    - SPRING_DATASOURCE_USERNAME=user
    - SPRING_DATASOURCE_PASSWORD=pass
    - SPRING_RABBITMQ_HOST=rabbitmq
    - SERVER_PORT=8081
  - depends_on:
    postgres-cuenta:
      condition: service_healthy
    rabbitmq:
      condition: service_healthy
  - networks: pruebatecnica-net

Volúmenes:
- cliente-db-data
- cuenta-db-data

Network:
- pruebatecnica-net (bridge)

## 3. LISTA DE TAREAS / CHECKS
- [ ] Crear docker-compose.yml en la raíz con la configuración anterior
- [ ] Crear Dockerfile multi-stage en cada servicio con esta estructura:
    - Stage 1 (build): image `maven:3.9-eclipse-temurin-17`, copiar fuentes, ejecutar `mvn clean package -DskipTests`
    - Stage 2 (runtime): image `eclipse-temurin:17-jre-alpine`, copiar JAR del stage 1 (usualmente en `target/*.jar`), `EXPOSE` del puerto correspondiente, `ENTRYPOINT ["java","-jar","app.jar"]`
    - Usar Alpine en runtime para imagen final liviana (~180MB vs ~600MB full JDK)
- [ ] Comprobar variables de entorno y application.yml para perfiles docker/local
- [ ] Añadir scripts de inicialización SQL si requiere seed (BaseDatos.sql)
- [ ] Verificar que RabbitMQ exchange cliente.exchange sea declarado por los servicios al arrancar (auto-declare)
- [ ] Crear seed_clientes.sql en la raíz con INSERTs para cliente-service (Personas y Clientes). Incluir comentario sobre reemplazo de hashes BCrypt:

      -- Personas
      INSERT INTO persona (persona_id, nombre, genero, edad, identificacion, direccion, telefono)
      VALUES
        (gen_random_uuid(), 'Jose Lema',          null, null, 'ID-001', 'Otavalo sn y principal',  '098254785'),
        (gen_random_uuid(), 'Marianela Montalvo',  null, null, 'ID-002', 'Amazonas y NNUU',         '097548965'),
        (gen_random_uuid(), 'Juan Osorio',         null, null, 'ID-003', '13 junio y Equinoccial',  '098874587')
      ON CONFLICT DO NOTHING;

      -- Clientes (referencia persona por identificacion)
      INSERT INTO cliente (persona_id, contrasena, estado)
      SELECT p.persona_id,
             '$2a$10$HASH_PLACEHOLDER',  -- BCrypt de 1234/5678/1245 según cliente
             true
      FROM persona p
      WHERE p.identificacion IN ('ID-001','ID-002','ID-003')
      ON CONFLICT DO NOTHING;

      Nota: los hashes BCrypt reales de las contraseñas (1234, 5678, 1245) deben generarse antes del seed y reemplazar HASH_PLACEHOLDER.

- [ ] Crear seed_cuentas.sql en la raíz con INSERTs para cuenta-service (ClienteSnapshot y Cuentas). Incluir nota sobre UUIDs a sincronizar con seed_clientes.sql:

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

      Nota: los UUID-JOSE, UUID-MARIANELA, UUID-JUAN deben ser los mismos UUIDs generados en seed_clientes.sql. Actualizar tras ejecutar seed de cliente-service.

- [ ] Agregar nota general: "Los seeds usan ON CONFLICT DO NOTHING para ser idempotentes. Si Spring Boot ya creó tablas con datos previos, el script no falla ni duplica registros
