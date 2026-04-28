---
id: SPEC-002
status: APPROVED
feature: cuenta-service
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs: [SPEC-001]
---

# Spec: cuenta-service — Gestión de Cuentas y Movimientos

## 1. REQUERIMIENTOS

### Descripción
Microservicio encargado de cuentas bancarias, movimientos y reportes. Consume eventos de cliente para mantener un ClienteSnapshot local y expone endpoints para cuentas, movimientos y reportes.

### Requerimiento de Negocio
Gestionar cuentas y movimientos; al registrar un movimiento se debe actualizar el saldo disponible; si se intenta retirar más del saldo disponible, lanzar excepción con mensaje "Saldo no disponible".

### Historias de Usuario

#### HU-01: Registrar Movimiento (depósito / retiro)
```
Como: Sistema financiero interno
Quiero: registrar movimientos en una cuenta
Para: mantener saldo actualizado y generar reportes
Prioridad: Alta
Estimación: M
Capa: Backend
```

**Criterios — HU-01**

**Happy Path (depósito)**
```gherkin
CRITERIO-1.1: Deposito exitoso
  Dado que: existe una cuenta con saldo 100
  Cuando: se registra un movimiento de tipo DEPOSITO valor 50
  Entonces: el nuevo saldo es 150 y se persiste el movimiento
```

**Happy Path (retiro)**
```gherkin
CRITERIO-1.2: Retiro exitoso
  Dado que: existe una cuenta con saldo 100
  Cuando: se registra un movimiento de tipo RETIRO valor 50
  Entonces: el nuevo saldo es 50 y se persiste el movimiento
```

**Error Path (saldo insuficiente)**
```gherkin
CRITERIO-1.3: Retiro con saldo insuficiente
  Dado que: la cuenta tiene saldo 30
  Cuando: se intenta retirar 50
  Entonces: lanza SaldoInsuficienteException con mensaje "Saldo no disponible" y no se persiste el movimiento
```

#### HU-02: CRUD de Cuentas y Movimientos
- Endpoints CRUD para /cuentas y /movimientos. (Las rutas usan `numeroCuenta` como identificador de negocio; internamente la BD usa `id: UUID` como PK técnica.)

**Criterios — HU-02**

**Happy Path**
```gherkin
CRITERIO-2.1: Crear cuenta exitosamente
  Dado que: el payload tiene numeroCuenta, tipoCuenta, saldoInicial >= 0 y clienteId válido
  Cuando: se realiza POST /api/v1/cuentas
  Entonces: devuelve 201 con la cuenta creada y saldoDisponible = saldoInicial
```

**Error Path**
```gherkin
CRITERIO-2.2: Cuenta no encontrada
  Dado que: el numeroCuenta no existe
  Cuando: se realiza GET /api/v1/cuentas/{numeroCuenta}
  Entonces: devuelve 404 con mensaje "Cuenta no encontrada"
```

**Error Path**
```gherkin
CRITERIO-2.3: Número de cuenta duplicado
  Dado que: ya existe una cuenta con el mismo numeroCuenta
  Cuando: se realiza POST /api/v1/cuentas
  Entonces: devuelve 409 con mensaje indicando conflicto
```

#### HU-03: Reportes por rango de fechas y cliente
- GET /api/v1/reportes?fechaInicio=yyyy-MM-dd&fechaFin=yyyy-MM-dd&clienteId={id}

Formato de respuesta por cada registro:
```json
{
  "fecha":"2026-04-28",
  "cliente":"Juan Perez",
  "numeroCuenta":"000123",
  "tipo":"AHORRO",
  "saldoInicial": 1000,
  "estado": true,
  "movimiento": 50,
  "saldoDisponible": 1050
}
```

### Reglas de Negocio
1. Movimiento modifica saldo disponible atomically (usar transacción).
2. Si retiro y saldo insuficiente → lanzar SaldoInsuficienteException con mensaje exacto "Saldo no disponible".
3. Movimientos: tipoMovimiento = ENUM {DEPOSITO, RETIRO}. valor positivo siempre.
4. Mantener ClienteSnapshot actualizado al consumir evento cliente.created.
5. Si una cuenta no tiene movimientos en el rango de fechas solicitado, no aparece en el resultado del reporte. El reporte solo incluye cuentas con al menos un movimiento en el rango.

---

## 2. DISEÑO
### Modelos de Dominio
- Cuenta (Aggregate Root)
  - id: UUID (PK técnica)
  - numeroCuenta: String (business id, UNIQUE, NOT NULL)
  - tipoCuenta: ENUM {AHORRO, CORRIENTE}
  - saldoInicial: BigDecimal
  - saldoDisponible: BigDecimal
  - estado: boolean
  - clienteId: UUID
- Movimiento
  - id: UUID
  - fecha: Instant
  - tipoMovimiento: ENUM {DEPOSITO, RETIRO}
  - valor: BigDecimal
  - saldo: BigDecimal (saldo después del movimiento)
- ClienteSnapshot
  - clienteId: UUID
  - nombre: String
  - lastUpdated: Instant

### Ports (Hexagonal)
- domain.port.in.CuentaUseCase — CRUD cuentas
- domain.port.in.MovimientoUseCase — registrarMovimiento(cuentaNum, MovimientoCommand)
- domain.port.in.ReporteUseCase — generarReporte(fechaInicio, fechaFin, clienteId)
- domain.port.out.CuentaRepository, MovimientoRepository, ClienteSnapshotRepository

### Application
- MovimientoUseCaseImpl: validar existencia de cuenta, calcular nuevo saldo, validar saldo suficiente (retiros), persistir movimiento y actualizar cuenta en la misma transacción.

### Infra - Persistence
- CuentaEntity, MovimientoEntity, ClienteSnapshotEntity (JPA)
  - `CuentaEntity` debe usar `id: UUID` como PK técnica y `numeroCuenta` como campo de negocio único (UNIQUE, NOT NULL).
- Repositorios JPA: CuentaJpaRepository (con método `Optional<CuentaEntity> findByNumeroCuenta(String numeroCuenta)`), MovimientoJpaRepository, ClienteSnapshotJpaRepository
- Adapters que implementan los ports out.

### Infra - Messaging (Consumer)
- ClienteEventConsumer:
  - bind queue to cliente.exchange with routing keys `cliente.created` and `cliente.updated`
  - on message: upsert `ClienteSnapshot` (clienteId, nombre, lastUpdated)
  - Nota: si llega `cliente.updated` con `nombre` diferente, actualizar el snapshot para que futuros reportes muestren el nombre correcto.

### API Endpoints
- Cuentas:
  - POST /api/v1/cuentas
  - GET /api/v1/cuentas
  - GET /api/v1/cuentas/{numeroCuenta}
  - PUT /api/v1/cuentas/{numeroCuenta}
  - DELETE /api/v1/cuentas/{numeroCuenta}
  - Nota: las rutas usan `numeroCuenta` como identificador de negocio; internamente la aplicación debe resolverlo al `id: UUID` de la entidad para operaciones de persistencia.

- Movimientos: POST /api/v1/movimientos (body: numeroCuenta, tipoMovimiento, valor), GET /api/v1/movimientos, GET /api/v1/movimientos/{id}
- Reportes: GET /api/v1/reportes?fechaInicio=&fechaFin=&clienteId=

### Notas de Implementación
- Usar transacciones para update cuenta + insert movimiento.
- Validar concurrencia: version manual en entidad y 409 en conflicto si aplica.
- Exponer excepciones de dominio con códigos HTTP claros.

---

## 3. LISTA DE TAREAS

### Backend
- [ ] Modelar Cuenta, Movimiento, ClienteSnapshot como entidades JPA
- [ ] Implementar repositorios y adapters
- [ ] Implementar MovimientoUseCaseImpl con lógica de saldo y transacciones
- [ ] Implementar ClienteEventConsumer y RabbitMQConfig
- [ ] Implementar controllers: CuentaController, MovimientoController, ReporteController
- [ ] Añadir manejo global de excepciones (SaldoInsuficienteException, ClienteNotFoundException, CuentaNotFoundException)

### Tests
- [ ] Unit test JUnit + Mockito: MovimientoUseCaseImpl — retiro con saldo insuficiente lanza excepción "Saldo no disponible" (requerido)
- [ ] Integration test (@SpringBootTest + TestContainers o @DataJpaTest) sobre flujo completo de registro de movimiento (requerido)
