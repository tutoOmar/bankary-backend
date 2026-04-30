# Cliente Service — Bankary

Servicio encargado de la gestión de perfiles de clientes, validación de documentos de identidad y seguridad de acceso.

## Responsabilidades
- Registro y actualización de clientes (Personas).
- Validación de documentos (formato y unicidad).
- Hashing de contraseñas de acceso.
- Emisión de eventos de dominio cuando un cliente es creado o actualizado.

## Variables de Entorno Requeridas

| Variable | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `DB_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/cliente_db` |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | `secret` |
| `SERVER_PORT` | Puerto de escucha del servicio | `8080` |
| `RABBITMQ_HOST` | Host del broker de mensajería | `localhost` |

## Endpoints Principales

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/clientes` | Registrar un nuevo cliente |
| `GET` | `/api/v1/clientes` | Listar todos los clientes |
| `GET` | `/api/v1/clientes/{id}` | Obtener detalle de un cliente |
| `PUT` | `/api/v1/clientes/{id}` | Actualizar datos de un cliente |
| `DELETE` | `/api/v1/clientes/{id}` | Eliminar un cliente |
| `GET` | `/actuator/health` | Estado de salud del servicio |

## Cómo Levantar el Servicio

### Usando Maven
```bash
mvn spring-boot:run
```

### Ejecutando el JAR
```bash
mvn clean package -DskipTests
java -jar target/cliente-service-0.0.1-SNAPSHOT.jar
```

## Tests y Cobertura
El servicio cuenta con tests unitarios y de integración.

```bash
# Ejecutar suite de pruebas (excluyendo integración que requiere Docker)
mvn verify -Dtest=!*IntegrationTest

# Ver reporte de cobertura
# Abrir target/site/jacoco/index.html en el navegador
```

## Estructura de Paquetes
```text
src/main/java/com/bankary/cliente/
├── domain/            # Lógica pura: Modelos, Puertos y Validadores
├── application/       # Casos de Uso: Lógica de coordinación
└── infrastructure/    # Adaptadores: Controllers, Repositories, Messaging
```
