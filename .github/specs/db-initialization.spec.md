---
id: SPEC-004
status: IMPLEMENTED
feature: db-initialization
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002, SPEC-003]
---

# Spec: db-initialization — Inicialización y seed de base de datos

## 1. REQUERIMIENTOS

### Descripción
Garantizar que al ejecutar docker-compose up por primera vez, ambas bases de datos
(cliente_db y cuenta_db) se inicialicen con el schema correcto y los datos de ejemplo
del PDF de la prueba técnica, sin intervención manual.

### Requerimiento de Negocio
El evaluador debe poder clonar el repo, ejecutar docker-compose up y tener el sistema
funcionando con datos precargados listos para probar en Postman.

### Problema actual
Los archivos .sql están referenciados en docker-compose.yml pero no se ejecutan porque:
1. docker-entrypoint-initdb.d solo ejecuta scripts la primera vez que se crea el volumen.
   Si el volumen ya existe de una ejecución anterior, los scripts se ignoran.
2. Spring Boot con ddl-auto=update crea las tablas antes del seed, generando conflictos
   de orden de inicialización que fallan silenciosamente.
3. No hay mecanismo que garantice que el schema exista antes de que lleguen los INSERT.

### Historias de Usuario

#### HU-01: Schema y seed automático al iniciar
Como: Evaluador de la prueba técnica
Quiero: que al hacer docker-compose up el sistema arranque con datos precargados
Para: poder probar los endpoints sin configuración manual adicional
Prioridad: Alta
Estimación: S
Dependencias: SPEC-003
Capa: Infraestructura

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Primera inicialización exitosa
  Dado que: no existe ningún volumen previo de Docker
  Cuando: se ejecuta docker-compose up
  Entonces: ambas BDs se crean con sus tablas y los datos del PDF están disponibles
            antes de que los servicios Spring Boot acepten peticiones
```

**Happy Path**
```gherkin
CRITERIO-1.2: Reinicio sin duplicados
  Dado que: los volúmenes ya existen con datos previos
  Cuando: se ejecuta docker-compose restart
  Entonces: el sistema arranca sin errores y sin duplicar registros
            (los INSERT usan ON CONFLICT DO NOTHING)
```

**Error Path**
```gherkin
CRITERIO-1.3: Volumen corrupto o desactualizado
  Dado que: el evaluador necesita un estado limpio
  Cuando: ejecuta docker-compose down -v seguido de docker-compose up
  Entonces: los volúmenes se recrean y el seed se ejecuta desde cero correctamente
```

### Reglas de Negocio
1. El schema (CREATE TABLE) siempre debe ejecutarse antes que el seed (INSERT).
2. Los INSERT deben usar ON CONFLICT DO NOTHING para ser idempotentes.
3. Spring Boot debe tener ddl-auto=validate (no create/update) en el perfil Docker,
   para que no interfiera con el schema creado por los scripts SQL.
4. Los UUIDs de clientes en seed_clientes.sql deben ser fijos (hardcodeados),
   no generados dinámicamente, para que seed_cuentas.sql pueda referenciarlos.

---

## 2. DISEÑO

### Estrategia de inicialización
Separar schema y seed en archivos numerados dentro de docker-entrypoint-initdb.d.
Postgres ejecuta los archivos en orden alfabético/numérico.

Estructura de archivos en la raíz del mono-repo:
  sql/
  ├── cliente/
  │   ├── 01_schema.sql   ← CREATE TABLE persona, cliente
  │   └── 02_seed.sql     ← INSERT clientes del PDF con UUIDs fijos
  └── cuenta/
      ├── 01_schema.sql   ← CREATE TABLE cuenta, movimiento, cliente_snapshot
      └── 02_seed.sql     ← INSERT cuentas y cliente_snapshot del PDF

### Montaje en Docker Compose
postgres-cliente:
  volumes:
    - cliente-db-data:/var/lib/postgresql/data
    - ./sql/cliente:/docker-entrypoint-initdb.d   ← monta la carpeta completa

postgres-cuenta:
  volumes:
    - cuenta-db-data:/var/lib/postgresql/data
    - ./sql/cuenta:/docker-entrypoint-initdb.d

### Configuración Spring Boot por perfil
En application-docker.yml de cada servicio:
  spring:
    jpa:
      hibernate:
        ddl-auto: validate   ← Spring valida que las tablas existan, no las crea
    sql:
      init:
        mode: never          ← Spring no ejecuta data.sql ni schema.sql propios

En application-local.yml (para desarrollo sin Docker):
  spring:
    jpa:
      hibernate:
        ddl-auto: update     ← En local Spring crea/actualiza tablas normalmente

### Contenido de sql/cliente/01_schema.sql
CREATE TABLE IF NOT EXISTS persona (
    persona_id     UUID         NOT NULL DEFAULT gen_random_uuid(),
    nombre         VARCHAR(100) NOT NULL,
    genero         VARCHAR(20),
    edad           INTEGER,
    identificacion VARCHAR(50)  NOT NULL,
    direccion      VARCHAR(150),
    telefono       VARCHAR(20),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_persona PRIMARY KEY (persona_id),
    CONSTRAINT uq_persona_identificacion UNIQUE (identificacion)
);

CREATE TABLE IF NOT EXISTS cliente (
    persona_id     UUID         NOT NULL,
    contrasena     VARCHAR(255) NOT NULL,
    estado         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cliente PRIMARY KEY (persona_id),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (persona_id)
        REFERENCES persona (persona_id)
);

### Contenido de sql/cliente/02_seed.sql
Nota: UUIDs fijos para poder referenciarlos desde seed de cuenta-service.

INSERT INTO persona (persona_id, nombre, identificacion, direccion, telefono)
VALUES
  ('a1b2c3d4-0001-0001-0001-000000000001', 'Jose Lema',         'ID-001', 'Otavalo sn y principal',  '098254785'),
  ('a1b2c3d4-0002-0002-0002-000000000002', 'Marianela Montalvo','ID-002', 'Amazonas y NNUU',         '097548965'),
  ('a1b2c3d4-0003-0003-0003-000000000003', 'Juan Osorio',       'ID-003', '13 junio y Equinoccial',  '098874587')
ON CONFLICT DO NOTHING;

INSERT INTO cliente (persona_id, contrasena, estado)
VALUES
  ('a1b2c3d4-0001-0001-0001-000000000001', '$2a$10$reemplazar_con_hash_de_1234', true),
  ('a1b2c3d4-0002-0002-0002-000000000002', '$2a$10$reemplazar_con_hash_de_5678', true),
  ('a1b2c3d4-0003-0003-0003-000000000003', '$2a$10$reemplazar_con_hash_de_1245', true)
ON CONFLICT DO NOTHING;

Nota: generar los hashes BCrypt reales ejecutando en un test o main temporal:
  System.out.println(new BCryptPasswordEncoder().encode("1234"));

### Contenido de sql/cuenta/01_schema.sql
CREATE TABLE IF NOT EXISTS cliente_snapshot (
    cliente_id   UUID         NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    last_updated TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cliente_snapshot PRIMARY KEY (cliente_id)
);

CREATE TABLE IF NOT EXISTS cuenta (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    numero_cuenta    VARCHAR(20)  NOT NULL,
    tipo_cuenta      VARCHAR(20)  NOT NULL,
    saldo_inicial    NUMERIC(15,2) NOT NULL DEFAULT 0,
    saldo_disponible NUMERIC(15,2) NOT NULL DEFAULT 0,
    estado           BOOLEAN      NOT NULL DEFAULT TRUE,
    cliente_id       UUID         NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cuenta PRIMARY KEY (id),
    CONSTRAINT uq_cuenta_numero UNIQUE (numero_cuenta)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id               UUID          NOT NULL DEFAULT gen_random_uuid(),
    fecha            TIMESTAMP     NOT NULL DEFAULT NOW(),
    tipo_movimiento  VARCHAR(20)   NOT NULL,
    valor            NUMERIC(15,2) NOT NULL,
    saldo            NUMERIC(15,2) NOT NULL,
    cuenta_id        UUID          NOT NULL,
    CONSTRAINT pk_movimiento PRIMARY KEY (id),
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (cuenta_id)
        REFERENCES cuenta (id)
);

### Contenido de sql/cuenta/02_seed.sql
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

---

## 3. LISTA DE TAREAS

### Infraestructura
- [ ] Crear carpeta sql/ en la raíz con subcarpetas cliente/ y cuenta/
- [ ] Crear sql/cliente/01_schema.sql con CREATE TABLE IF NOT EXISTS de persona y cliente
- [ ] Crear sql/cliente/02_seed.sql con INSERT de los 3 clientes del PDF (UUIDs fijos)
- [ ] Crear sql/cuenta/01_schema.sql con CREATE TABLE IF NOT EXISTS de cuenta,
      movimiento y cliente_snapshot
- [ ] Crear sql/cuenta/02_seed.sql con INSERT de los 4 snapshots y 4 cuentas del PDF
- [ ] Actualizar docker-compose.yml: cambiar volumes de postgres-cliente y postgres-cuenta
      para montar ./sql/cliente y ./sql/cuenta respectivamente en docker-entrypoint-initdb.d
- [ ] Generar hashes BCrypt reales para contraseñas 1234, 5678, 1245 y reemplazar
      los placeholders en sql/cliente/02_seed.sql

### Configuración Spring Boot
- [ ] Crear application-docker.yml en cliente-service con ddl-auto=validate y sql.init.mode=never
- [ ] Crear application-docker.yml en cuenta-service con ddl-auto=validate y sql.init.mode=never
- [ ] Verificar que el Dockerfile pase la variable SPRING_PROFILES_ACTIVE=docker
      como variable de entorno al contenedor

### Verificación
- [ ] Ejecutar docker-compose down -v && docker-compose up y confirmar que ambos
      servicios arrancan sin errores de schema
- [ ] Verificar vía psql o DBeaver que las 3 personas, 3 clientes y 4 cuentas existen
- [ ] Confirmar que un docker-compose restart no duplica registros