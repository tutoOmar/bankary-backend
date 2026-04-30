# Guía de Ejemplos de API — Bankary

Esta guía proporciona ejemplos concretos de las peticiones (Payloads) y respuestas esperadas para interactuar con los microservicios.

## 1. Cliente Service (Puerto 8080)

### Crear Cliente
**POST** `/api/v1/clientes`

**Request:**
```json
{
  "nombre": "Jose Lema",
  "genero": "Masculino",
  "edad": 30,
  "tipoDocumento": "CC",
  "numeroDocumento": "123456",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "contrasena": "1234",
  "estado": true
}
```

**Response (201 Created):**
```json
{
  "clienteId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "nombre": "Jose Lema",
  "estado": true
}
```

---

## 2. Cuenta Service (Puerto 8081)

### Crear Cuenta
**POST** `/api/v1/cuentas`

**Request:**
```json
{
  "numeroCuenta": "478758",
  "tipoCuenta": "AHORRO",
  "saldoInicial": 2000,
  "estado": true,
  "clienteId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
}
```

**Response (201 Created):**
```json
{
  "id": "f5e4d3c2-b1a0-4f9e-8d7c-6b5a4b3c2d1e",
  "numeroCuenta": "478758",
  "saldoDisponible": 2000,
  "estado": true
}
```

### Realizar Movimiento (Retiro)
**POST** `/api/v1/movimientos`

**Request:**
```json
{
  "numeroCuenta": "478758",
  "tipoMovimiento": "RETIRO",
  "valor": 575
}
```

**Response (201 Created):**
```json
{
  "id": "9a8b7c6d-5e4f-3a2b-1c0d-e9f8a7b6c5d4",
  "fecha": "2026-04-29T20:30:00Z",
  "tipoMovimiento": "RETIRO",
  "valor": 575,
  "saldo": 1425
}
```

---

## 3. Reportes y Consultas

### Generar Estado de Cuenta
**GET** `/api/v1/reportes?fechaInicio=2026-01-01&fechaFin=2026-12-31&clienteId=a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d`

**Response (200 OK):**
```json
[
  {
    "fecha": "2026-04-29",
    "cliente": "Jose Lema",
    "numeroCuenta": "478758",
    "tipo": "AHORRO",
    "saldoInicial": 2000,
    "estado": true,
    "movimiento": -575,
    "saldoDisponible": 1425
  }
]
```

---

## 4. Gestión de Errores (Estándar SPEC-007)

### Error: Saldo No Disponible
**POST** `/api/v1/movimientos` (Retiro mayor al saldo)

**Response (400 Bad Request):**
```json
{
  "timestamp": "2026-04-29T20:35:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Saldo no disponible",
  "path": "/api/v1/movimientos"
}
```

### Error: Recurso No Encontrado
**GET** `/api/v1/clientes/999`

**Response (404 Not Found):**
```json
{
  "timestamp": "2026-04-29T20:40:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente no encontrado con ID: 999",
  "path": "/api/v1/clientes/999"
}
```
