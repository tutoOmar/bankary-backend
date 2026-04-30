# Arquitectura del Proyecto — Bankary

Este documento detalla las decisiones técnicas, la estructura de componentes y los flujos de datos del sistema Bankary.

## 1. Visión General

El sistema está compuesto por dos microservicios que se comunican de forma síncrona (REST) y asíncrona (Eventos RabbitMQ).

```text
   [Cliente HTTP / Insomnia]
                │
                ▼
   ┌────────────────────────┐         ┌────────────────────────┐
   │    cliente-service     │         │     cuenta-service     │
   │      (Puerto 8080)     │◄────────│      (Puerto 8081)     │
   └───────────┬────────────┘  REST   └───────────┬────────────┘
               │                                  │
               │           [RabbitMQ]             │
               │        (cliente.exchange)        │
               └──────────────────────────────────┘
                                │
                                ▼
                         (Eventos de Sync)
```

## 2. Arquitectura Hexagonal

Cada microservicio sigue el patrón de Arquitectura Hexagonal para desacoplar la lógica de negocio de los detalles técnicos.

### Capas por Servicio
- **Domain**: Contiene el corazón del negocio. Incluye Entidades de Dominio (`Cliente`, `Cuenta`), Excepciones de Negocio y los Puertos (`Interfaces`). No tiene dependencias de Spring.
- **Application**: Implementa los Casos de Uso. Orquesta la lógica del dominio y coordina las llamadas a los puertos de salida (persistencia, mensajería).
- **Infrastructure**: Contiene los Adaptadores. Aquí residen los Controladores REST, los Repositorios JPA (Spring Data) y los Clientes de Mensajería.

## 3. Decisiones de Diseño

- **Arquitectura Hexagonal**: Se eligió para facilitar la testabilidad. La lógica de negocio se prueba con JUnit puro sin necesidad de levantar bases de datos o contextos pesados.
- **Microservicios Independientes**: Cada servicio es un proyecto Maven autónomo con su propio ciclo de vida, facilitando el escalamiento independiente.
- **Estrategia de Persistencia**: Uso de PostgreSQL para asegurar consistencia ACID. El esquema se gestiona mediante scripts SQL en la carpeta `/sql` del monorepo.
- **Patrón Strategy**: Utilizado en `cuenta-service` para validar los retiros según el tipo de cuenta (Ahorro vs Corriente), permitiendo extender nuevas reglas de negocio sin modificar el caso de uso principal (OCP).

## 4. Modelo de Datos

### cliente-service (DB: cliente_db)
- **persona**: Tabla base con datos demográficos (nombre, género, edad, documento).
- **cliente**: Tabla extendida (Secondary Table) que contiene la contraseña y el estado, vinculada por `persona_id`.

### cuenta-service (DB: cuenta_db)
- **cuenta**: Almacena el saldo disponible, número de cuenta y la referencia al `cliente_id`.
- **movimiento**: Registro histórico de cada transacción (Débito/Crédito) con su saldo resultante.
- **cliente_snapshot**: Cache local de información del cliente recibida vía eventos para evitar latencia en reportes.

## 5. Reglas de Negocio Críticas

- **Validación de Identidad**: Se validan formatos de documentos colombianos (SPEC-005).
- **Control de Retiros**: Las cuentas de ahorro no pueden tener saldo negativo; las corrientes permiten sobregiros (SPEC-005).
- **Integridad de Cuentas**: Un cliente no puede tener dos cuentas activas del mismo tipo (SPEC-005).
- **Cobertura**: Obligatoriedad del 80% de cobertura en capas de dominio y aplicación (SPEC-008).

## 6. Flujo de una Petición (Ejemplo: Crear Cliente)

Cuando se envía un `POST /api/v1/clientes`:
1. **Infrastructure**: `ClienteController` recibe el DTO y lo mapea a un `Command`.
2. **Application**: `ClienteCommandUseCaseImpl` recibe el comando.
3. **Domain**: Se instancia la entidad `Cliente`, que ejecuta sus validaciones internas.
4. **Application**: El caso de uso llama al puerto `ClienteRepository` (Interface).
5. **Infrastructure**: `ClienteRepositoryAdapter` implementa la interfaz, convierte el dominio a `ClienteEntity` y guarda en la DB vía `JpaRepository`.
6. **Application**: Se dispara un evento a través del puerto `ClienteEventPublisher`.
7. **Infrastructure**: `RabbitMQEventPublisherAdapter` envía el mensaje al broker.
