# Cliente Service

El **Cliente Service** es un microservicio del proyecto **Bankary** encargado de la gestión de clientes. Sigue una Arquitectura Hexagonal e implementa el patrón CQRS (Command Query Responsibility Segregation) para separar las operaciones de lectura y escritura.

## 🚀 Tecnologías Principales
- Java 21
- Spring Boot
- Arquitectura Hexagonal
- CQRS

## 🌐 Endpoints

La ruta base para todos los endpoints es: `/api/v1/clientes`

### 1. Crear Cliente
- **Ruta:** `POST /api/v1/clientes`
- **Descripción:** Crea un nuevo cliente en el sistema.
- **Request Body (ClienteRequest):**
  ```json
  {
    "nombre": "Juan Perez",
    "genero": "Masculino",
    "edad": 30,
    "identificacion": "1234567890",
    "direccion": "Calle Falsa 123",
    "telefono": "555-1234",
    "contrasena": "Secreta123"
  }
  ```
- **Respuesta Exitosa (201 Created) - (ClienteResponse):**
  ```json
  {
    "clienteId": "550e8400-e29b-41d4-a716-446655440000",
    "nombre": "Juan Perez",
    "genero": "Masculino",
    "edad": 30,
    "identificacion": "1234567890",
    "direccion": "Calle Falsa 123",
    "telefono": "555-1234",
    "estado": true
  }
  ```

### 2. Listar Clientes
- **Ruta:** `GET /api/v1/clientes`
- **Descripción:** Obtiene una lista de todos los clientes registrados.
- **Respuesta Exitosa (200 OK):**
  ```json
  [
    {
      "clienteId": "550e8400-e29b-41d4-a716-446655440000",
      "nombre": "Juan Perez",
      "genero": "Masculino",
      "edad": 30,
      "identificacion": "1234567890",
      "direccion": "Calle Falsa 123",
      "telefono": "555-1234",
      "estado": true
    }
  ]
  ```

### 3. Obtener Cliente por ID
- **Ruta:** `GET /api/v1/clientes/{clienteId}`
- **Descripción:** Obtiene los detalles de un cliente específico utilizando su UUID.
- **Parámetro de Ruta:** `clienteId` (UUID)
- **Respuesta Exitosa (200 OK):**
  Devuelve el mismo objeto que en la creación (`ClienteResponse`).

### 4. Actualizar Cliente
- **Ruta:** `PUT /api/v1/clientes/{clienteId}`
- **Descripción:** Actualiza los datos de un cliente existente.
- **Parámetro de Ruta:** `clienteId` (UUID)
- **Request Body (ClienteRequest):** Mismo formato que en el método POST.
- **Respuesta Exitosa (200 OK):** Devuelve el objeto `ClienteResponse` actualizado.

### 5. Eliminar Cliente
- **Ruta:** `DELETE /api/v1/clientes/{clienteId}`
- **Descripción:** Elimina (o marca como inactivo) un cliente específico.
- **Parámetro de Ruta:** `clienteId` (UUID)
- **Respuesta Exitosa (200 OK):** Sin contenido.
