# Cuenta Service

El **Cuenta Service** es un microservicio del proyecto **Bankary** encargado de la gestión de cuentas bancarias, los movimientos (depósitos y retiros) asociados a estas y la generación de reportes de estado de cuenta. Está construido siguiendo los principios de la Arquitectura Hexagonal y se comunica asíncronamente con otros servicios mediante RabbitMQ.

## 🚀 Tecnologías Principales
- Java 21
- Spring Boot
- Arquitectura Hexagonal
- RabbitMQ (para consumo de eventos de clientes)

---

## 🌐 Endpoints

La base de las URLs dependerá del recurso al que quieras acceder. Existen tres controladores principales:

### 1. Gestión de Cuentas (`/api/v1/cuentas`)

#### Crear Cuenta
- **Ruta:** `POST /api/v1/cuentas`
- **Descripción:** Crea una nueva cuenta bancaria para un cliente existente.
- **Request Body (CuentaRequest):**
  ```json
  {
    "numeroCuenta": "478758",
    "tipoCuenta": "AHORRO",
    "saldoInicial": 2000.00,
    "clienteId": "UUID-DEL-CLIENTE"
  }
  ```
  *(Nota: `tipoCuenta` puede ser `AHORRO` o `CORRIENTE`)*

#### Listar Cuentas
- **Ruta:** `GET /api/v1/cuentas`
- **Descripción:** Obtiene una lista de todas las cuentas registradas.

#### Obtener Cuenta por Número
- **Ruta:** `GET /api/v1/cuentas/{numeroCuenta}`
- **Descripción:** Obtiene los detalles de una cuenta específica utilizando su número de cuenta.

#### Actualizar Cuenta
- **Ruta:** `PUT /api/v1/cuentas/{numeroCuenta}`
- **Descripción:** Actualiza los datos de una cuenta existente.

#### Eliminar Cuenta
- **Ruta:** `DELETE /api/v1/cuentas/{numeroCuenta}`
- **Descripción:** Elimina una cuenta específica.

---

### 2. Gestión de Movimientos (`/api/v1/movimientos`)

#### Registrar Movimiento
- **Ruta:** `POST /api/v1/movimientos`
- **Descripción:** Registra un nuevo movimiento (depósito o retiro) en una cuenta específica. Valida que haya saldo suficiente en caso de retiro.
- **Request Body (MovimientoRequest):**
  ```json
  {
    "numeroCuenta": "478758",
    "tipoMovimiento": "RETIRO",
    "valor": 150.00
  }
  ```
  *(Nota: `tipoMovimiento` puede ser `DEPOSITO` o `RETIRO`. El `valor` debe ser un número positivo).*

#### Listar Movimientos
- **Ruta:** `GET /api/v1/movimientos`
- **Descripción:** Obtiene una lista de todos los movimientos.

#### Obtener Movimiento por ID
- **Ruta:** `GET /api/v1/movimientos/{id}`
- **Descripción:** Obtiene los detalles de un movimiento utilizando su UUID.

---

### 3. Reportes (`/api/v1/reportes`)

#### Generar Reporte de Estado de Cuenta
- **Ruta:** `GET /api/v1/reportes`
- **Descripción:** Genera un estado de cuenta detallado para un cliente en un rango de fechas.
- **Parámetros de Consulta (Query Params):**
  - `fechaInicio` (formato `YYYY-MM-DD`)
  - `fechaFin` (formato `YYYY-MM-DD`)
  - `clienteId` (UUID)
- **Ejemplo:** `/api/v1/reportes?fechaInicio=2024-04-01&fechaFin=2024-04-30&clienteId=UUID-DEL-CLIENTE`
