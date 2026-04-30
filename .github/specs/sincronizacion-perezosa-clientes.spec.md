---
id: SPEC-011
status: IN_PROGRESS
feature: sincronizacion-perezosa-clientes
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002, SPEC-005, SPEC-007]
---

# Spec: sincronizacion-perezosa-clientes — Lazy Loading de Snapshots

## 1. REQUERIMIENTOS

### Descripción
Implementar un mecanismo de "carga perezosa" (Lazy Loading) en `cuenta-service` para la información de clientes. Actualmente, si un cliente fue creado antes de que `cuenta-service` estuviera escuchando eventos, o si hubo una falla en la mensajería, el servicio de cuentas no puede crearle una cuenta porque no encuentra al cliente en su base de datos local (`cliente_snapshot`). 

Con este cambio, si el cliente no existe localmente, el servicio consultará síncronamente a `cliente-service` y, si existe, guardará la snapshot antes de proceder.

### Requerimiento de Negocio
Garantizar la continuidad operativa. Un cliente existente en el sistema siempre debe poder abrir una cuenta, independientemente de si la sincronización asíncrona (RabbitMQ) falló o no ha ocurrido aún para ese registro específico.

### Historias de Usuario

#### HU-01: Verificación y Sincronización en Creación de Cuenta
Como: Sistema de Cuentas
Quiero: consultar al servicio de clientes cuando un cliente no se encuentre en mi base de datos local
Para: registrar su información básica (snapshot) y permitir la creación de la cuenta sin errores manuales
Prioridad: Alta

#### Criterios de Aceptación — HU-01

```gherkin
CRITERIO-1.1: Cliente existe localmente
  Dado que: el cliente con UUID "D" ya existe en la tabla cliente_snapshot
  Cuando: se solicita crear una cuenta para el cliente "D"
  Entonces: el sistema usa la información local y crea la cuenta normalmente.

CRITERIO-1.2: Cliente NO existe localmente pero SÍ en Cliente-Service
  Dado que: el cliente "D" NO está en la tabla local
  Y: el cliente-service responde 200 OK con los datos del cliente
  Cuando: se solicita crear una cuenta para el cliente "D"
  Entonces: el sistema guarda al cliente "D" en cliente_snapshot
  Y: procede a crear la cuenta exitosamente.

CRITERIO-1.3: Cliente NO existe en ningún lado
  Dado que: el cliente "Z" no existe localmente
  Y: el cliente-service responde 404 Not Found
  Cuando: se solicita crear una cuenta para el cliente "Z"
  Entonces: el sistema devuelve un error 404 con el mensaje "Cliente no existe en el sistema".
```

---

## 2. DISEÑO

### Componentes Afectados

#### 1. Capa Domain (cuenta-service)
- **Puerto de Salida:** `ClienteServicePort` (ya existente, pero asegurar que maneja el mapeo a snapshot).

#### 2. Capa Application (cuenta-service)
- **Usecase:** `CuentaUseCaseImpl`.
- **Lógica:**
  ```java
  // Pseudocódigo
  if (!clienteLocalRepository.existsById(clienteId)) {
      Cliente clienteExt = clienteServicePort.buscarPorId(clienteId); // Puede lanzar 404
      clienteLocalRepository.save(new ClienteSnapshot(clienteExt));
  }
  // Continuar con creación de cuenta...
  ```

#### 3. Capa Infrastructure (cuenta-service)
- **Adaptador HTTP:** `ClienteServiceAdapter` (usando RestTemplate).
- **Manejo de Errores:** Debe capturar el `HttpClientErrorException.NotFound` de Spring y relanzarlo como una excepción de dominio (ej. `EntityNotFoundException`) para que el `GlobalExceptionHandler` devuelva el 404 correcto.

---

## 3. LISTA DE TAREAS

### Backend (cuenta-service)
- [ ] Modificar `ClienteServicePort` para incluir un método que devuelva el modelo de Dominio `Cliente` (o un DTO simplificado).
- [ ] Implementar/Actualizar `ClienteServiceAdapter` para realizar la llamada GET a `cliente-service`.
- [ ] Agregar lógica de "verificar y guardar" en `CuentaCommandUseCaseImpl` antes de persistir la nueva cuenta.
- [ ] Asegurar que la entidad `ClienteEntity` (la snapshot en cuenta-service) tenga los campos necesarios para recibir la respuesta de la API externa.

### Database
- [ ] Verificar que la tabla `cliente` (snapshot) en `cuentadb` coincida en estructura con los datos mínimos requeridos de `cliente-service`.

### QA / Tests
- [ ] Crear test unitario en `CuentaUseCase` simulando cliente no encontrado localmente y encontrado externamente (Mocking).
- [ ] Crear test unitario simulando cliente no encontrado en ninguno de los dos servicios (debe fallar con 404).
- [ ] Verificar que no se dupliquen registros en `cliente_snapshot` si se llama dos veces seguidas para el mismo ID.
