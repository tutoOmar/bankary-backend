---
id: SPEC-001
status: IMPLEMENTED
feature: cliente-service
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: cliente-service — Gestión de Clientes

## 1. REQUERIMIENTOS

### Descripción
Microservicio para gestionar clientes (Persona → Cliente) con CRUD y publicación de eventos hacia RabbitMQ al crear/actualizar.

### Requerimiento de Negocio
Permitir a sistemas internos crear, leer, actualizar y eliminar (borrado lógico) clientes. Al crear o actualizar un cliente el servicio publica un evento con { clienteId, nombre, timestamp, eventType } en el exchange `cliente.exchange` usando routing keys `cliente.created` y `cliente.updated`.

### Historias de Usuario

#### HU-01: Crear Cliente
```
Como: Operador del sistema
Quiero: crear un Cliente con datos personales
Para: que pueda asociarse a cuentas y otros servicios
Prioridad: Alta
Estimación: M
Dependencias: Ninguna
Capa: Backend
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Crear cliente exitosamente
  Dado que: el payload tiene nombres, identificacion y demás campos válidos
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 201 con el recurso creado y publica evento "cliente.created" con { clienteId, nombre }
```

**Error Path**
```gherkin
CRITERIO-1.2: Campos obligatorios faltantes
  Dado que: falta "nombre" o "identificacion"
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 400 con error de validación
```

**Error Path — duplicado**
```gherkin
CRITERIO-1.3: Identificación duplicada
  Dado que: ya existe un cliente con la misma identificacion
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 409 con mensaje indicando conflicto
```

#### HU-02: Actualizar Cliente
```
Como: Operador del sistema
Quiero: actualizar datos de un cliente existente
Para: mantener la información actualizada
Prioridad: Alta
Estimación: M
Dependencias: HU-01
Capa: Backend
```

**Happy Path**
```gherkin
CRITERIO-2.1: Actualizar cliente exitosamente
  Dado que: existe un cliente con clienteId válido y el payload tiene campos válidos
  Cuando: se realiza PUT /api/v1/clientes/{clienteId}
  Entonces: devuelve 200 con el recurso actualizado y publica evento "cliente.updated"
           con { clienteId, nombre }
```

**Error Path**
```gherkin
CRITERIO-2.2: Cliente no encontrado
  Dado que: el clienteId no existe en el sistema
  Cuando: se realiza PUT /api/v1/clientes/{clienteId}
  Entonces: devuelve 404 con mensaje "Cliente no encontrado"
```

**Error Path**
```gherkin
CRITERIO-2.3: Payload inválido en actualización
  Dado que: el payload tiene campos vacíos o con formato incorrecto
  Cuando: se realiza PUT /api/v1/clientes/{clienteId}
  Entonces: devuelve 400 con detalle de validación
```

#### HU-03: Obtener/Lista/Eliminar Cliente
- GET /api/v1/clientes, GET /api/v1/clientes/{clienteId}, DELETE /api/v1/clientes/{clienteId} (borrado lógico)

### Reglas de Negocio
1. `identificacion` debe ser única entre clientes activos.
2. `contrasena` se almacena hasheada (BCrypt) — nunca en texto plano.
3. Al crear o actualizar, publicar evento en `cliente.exchange` con routing keys `cliente.created` / `cliente.updated`. Payload obligatorio: { clienteId, nombre, timestamp, eventType }.
4. Validaciones: `nombre` obligatorio y no vacío; `edad` >= 0; `genero` es `String` simple.
5. Idempotencia: si el cliente se crea con `clienteId` existente, devolver 200 con recurso actual (evitar duplicados por reintentos).
6. Borrado lógico: DELETE marca `estado = false` y no elimina físicamente el registro.
7. Un cliente con `estado = false` no puede asociarse a nuevas cuentas.

---

## 2. DISEÑO

### Agregado / Modelos de Dominio
- Agregado raíz: `Cliente` (extiende `Persona`).
- Campos simples (sin Value Objects complejos):
  - `nombre: String`
  - `genero: String`
  - `edad: Integer`
  - `identificacion: String` (único)
  - `direccion: String`
  - `telefono: String`
- Entidades:
  - `Persona`: `nombre`, `genero`, `edad`, `identificacion`, `direccion`, `telefono`
  - `Cliente` (Aggregate): `clienteId: UUID`, `contrasena: String (hashed)`, `estado: boolean`, y los campos de `Persona`.

### Ports (Hexagonal)
- domain.port.in.ClienteUseCase
  - create(CreateClienteCommand): ClienteResponse
  - update(clienteId, UpdateClienteCommand): ClienteResponse
  - getById(clienteId): ClienteResponse
  - list(): List<ClienteResponse>
  - delete(clienteId): void
- domain.port.out.ClienteRepository
  - CRUD + findByIdentificacion(...)
- domain.port.out.ClienteEventPublisher
  - publishClienteCreado(ClienteEvent), publishClienteActualizado(ClienteEvent)

### Application
- application.usecase.ClienteUseCaseImpl — hashing de contraseña, validaciones y publicación de eventos.

### Infraestructura - Persistence
- PersonaEntity (JPA, @Inheritance(strategy = InheritanceType.JOINED))
  Tabla: persona
  Campos: persona_id (PK, UUID), nombre, genero, edad, identificacion (único),
          direccion, telefono, created_at, updated_at

- ClienteEntity extends PersonaEntity (@Entity, @PrimaryKeyJoinColumn(name="persona_id"))
  Tabla: cliente
  Campos: persona_id (PK + FK → persona.persona_id), contrasena_hash, estado,
          created_at, updated_at
  Nota: no existe un clienteId separado — persona_id cumple ese rol.

- ClienteJpaRepository extends JpaRepository<ClienteEntity, UUID>
  Métodos custom: findByIdentificacion(String), findAllByEstadoTrue()

- ClientePersistenceAdapter implements ClienteRepository
  Mapea entre ClienteEntity/PersonaEntity y el modelo de dominio Cliente.

Con JOINED, cada consulta de Cliente hace un JOIN automático
entre las tablas persona y cliente. Es el trade-off aceptado por normalización.

### Infraestructura - Messaging
- RabbitMQClienteEventPublisher:
  - Exchange: cliente.exchange (topic)
  - Routing keys: cliente.created, cliente.updated
  - Message: ClienteEvent { clienteId: UUID, nombre: String, timestamp: Instant, eventType: String }
  - Config en infrastructure.config.RabbitMQConfig

### API Endpoints

POST /api/v1/clientes
- Request: ClienteRequest (nombre, genero, edad, identificacion, direccion, telefono, contrasena — todos String excepto `edad` que es Integer)
- Responses:
  - 201 Created: ClienteResponse (incluye clienteId, estado: true)
  - 400 Bad Request: validaciones
  - 409 Conflict: identificacion duplicada

GET /api/v1/clientes
- 200 OK: lista de clientes activos (estado = true)

GET /api/v1/clientes/{clienteId}
- 200 OK: cliente si existe y está activo
- 404 Not Found: si no existe o está inactivo

PUT /api/v1/clientes/{clienteId}
- Request: ClienteRequest (campos a actualizar)
- 200 OK: cliente actualizado
- 404 Not Found: cliente no encontrado
- 400 Bad Request: validaciones

DELETE /api/v1/clientes/{clienteId}
- 200 OK: ClienteResponse con estado: false (borrado lógico realizado)
- 404 Not Found: cliente no encontrado

### Notas de Implementación
- Usar DTOs: ClienteRequest, ClienteResponse, CreateClienteCommand, UpdateClienteCommand.
- Password hashing en capa application/usecase.
- Publicación de eventos: Fire-and-forget. En caso de fallo de RabbitMQ, loguear el error pero no fallar la transacción principal (persistencia en BD debe completar).
- Todos los campos de entrada (nombre, genero, direccion, telefono, identificacion) son String simples, sin validaciones de formato complejas. Solo validar longitud máxima y que no estén vacíos.

### Stack Tecnológico
- **Build Tool**: Maven (pom.xml)
- **Getters/Setters**: Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Data`) — reduce boilerplate en DTOs y entidades.
- **Lombok Configuration**: agregar `<plugin>` en pom.xml y `@java.beans.ConstructorProperties` si es necesario para compatibilidad con frameworks.

---

## 3. LISTA DE TAREAS

### Backend
- [ ] Definir Create/Update/Response DTOs y Commands
- [ ] Implementar ClienteEntity + ClienteJpaRepository
- [ ] Modelar PersonaEntity con @Inheritance(JOINED) y ClienteEntity
        con @PrimaryKeyJoinColumn
- [ ] Implementar ClientePersistenceAdapter mapeando las dos tablas
        al modelo de dominio Cliente
- [ ] Implementar ClientePersistenceAdapter
- [ ] Implementar ClienteUseCaseImpl (hash contrasena, validaciones, publicar evento)
- [ ] Implementar ClienteController (REST)
- [ ] Implementar RabbitMQClienteEventPublisher y RabbitMQConfig
- [ ] Añadir @RestControllerAdvice para manejo global de errores

### Tests
- [ ] Unit test JUnit+Mockito: ClienteUseCaseImpl.create — crear cliente exitosamente y verificar llamadas a repository y event publisher (requerido)
- [ ] Integration test con @SpringBootTest: flujo completo POST /api/v1/clientes → verifica persistencia en BD y publicación del evento en RabbitMQ (usar Testcontainers para PostgreSQL y RabbitMQ) (requerido)
