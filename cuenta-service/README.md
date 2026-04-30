# Cuenta Service — Bankary

Servicio encargado de la gestión de cuentas bancarias, procesamiento de movimientos (depósitos/retiros) y generación de reportes de estado de cuenta.

## Responsabilidades
- Creación y administración de cuentas de ahorro y corrientes.
- Validación de saldos disponibles según el tipo de cuenta.
- Registro histórico de movimientos contables.
- Sincronización de información de clientes vía eventos de RabbitMQ.
- Generación de reportes dinámicos por rango de fechas.

## Variables de Entorno Requeridas

| Variable | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `DB_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5433/cuenta_db` |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | `secret` |
| `SERVER_PORT` | Puerto de escucha del servicio | `8081` |
| `CLIENTE_SERVICE_URL` | URL base para llamadas al cliente-service | `http://localhost:8080` |
| `RABBITMQ_HOST` | Host del broker de mensajería | `localhost` |

## Endpoints Principales

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/cuentas` | Crear una nueva cuenta |
| `GET` | `/api/v1/cuentas` | Listar todas las cuentas |
| `POST` | `/api/v1/movimientos` | Registrar un depósito o retiro |
| `GET` | `/api/v1/movimientos` | Consultar historial de movimientos |
| `GET` | `/api/v1/reportes` | Generar estado de cuenta por fecha |
| `GET` | `/actuator/health` | Estado de salud del servicio |

## Cómo Levantar el Servicio

### Usando Maven
```bash
mvn spring-boot:run
```

### Ejecutando el JAR
```bash
mvn clean package -DskipTests
java -jar target/cuenta-service-0.0.1-SNAPSHOT.jar
```

## Tests y Cobertura
El servicio cuenta con tests unitarios, de integración y validaciones de patrones de diseño (Strategy).

```bash
# Ejecutar suite de pruebas
mvn verify -Dtest=!*IntegrationTest

# Ver reporte de cobertura
# Abrir target/site/jacoco/index.html en el navegador
```

## Estructura de Paquetes
```text
src/main/java/com/bankary/cuenta/
├── domain/            # Lógica: Entidades, Estrategias de Validación, Puertos
├── application/       # Casos de Uso y DTOs
└── infrastructure/    # Adaptadores: Controllers, Repositories, Client API
```
