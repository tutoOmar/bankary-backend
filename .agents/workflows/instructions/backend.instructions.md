---
applyTo: "backend/src/**/*.java"
---

> **Scope**: Se aplica a los microservicios `plataforma-danos-back` y `plataforma-core-ohs` (Spring Boot 3.2, Java 17).

# Instrucciones para Backend (Arquitectura Hexagonal + DDD)

## Estructura de Microservicios

Este repositorio contiene dos servicios independientes en carpetas raíz:
- **`plataforma-danos-back`**: Lógica de negocio de cotización, persistencia en PostgreSQL.
- **`plataforma-core-ohs`**: Mock de servicios core mediante fixtures JSON.

## Arquitectura Hexagonal (plataforma-danos-back)

Dentro de cada microservicio (especialmente `danos-back`), seguir estas capas:

```
src/main/java/com/asdd/danos/
├── domain/               ← Lógica de Negocio Pura (Cero dependencias de framework)
│   ├── model/            ← Agregados, Entidades de Dominio, Value Objects
│   ├── repository/       ← Interfaces de repositorio (Ports)
│   ├── service/          ← Domain Services (si aplican)
│   └── exception/        ← Excepciones de negocio
├── application/          ← Casos de Uso (Orquestación)
│   ├── usecase/          ← Implementaciones de la lógica de aplicación
│   ├── dto/              ← Request/Response de aplicación
│   └── mapper/           ← Mappers Dominio ↔ DTO
└── infrastructure/       ← Adaptadores (Detalles técnicos)
    ├── adapter/          ← Implementaciones de Ports (REST, JPA, Feign)
    │   ├── in/           ← REST Controllers
    │   └── out/          ← JPA Repositories, API Clients
    └── config/           ← Beans de Spring, Seguridad, etc.
```

## Reglas de Diseño (DDD)

- **Agregado Raíz**: `Cotizacion` es el Aggregate Root. Todas las operaciones se realizan a través de él.
- **Ubicaciones**: Son parte del agregado `Cotizacion`. No tienen repositorio propio.
- **Idempotencia**: `POST /v1/folios` debe verificar si el `numeroFolio` ya existe y retornar 200 con el objeto si es el caso.
- **Versionado Optimista Manual**: Validar el campo `version` en el caso de uso antes de persistir. Lanzar `ConflictException` (409) si hay desajuste. NO usar `@Version`.

## Inyección de Dependencias

- Usar **Constructor Injection** (con `@RequiredArgsConstructor` de Lombok).
- Definir Beans de Casos de Uso manualmente en la capa de `infrastructure.config` si el framework lo requiere para mantener `domain` y `application` limpios de anotaciones de Spring (`@Service`, `@Component`).

## Persistencia (PostgreSQL)

- Usar **Spring Data JPA** en la capa de infraestructura.
- Mapear Entidades de Dominio a Entidades JPA mediante mappers específicos en el adaptador de salida.
- Nombres de tablas/columnas en `snake_case`.

## Documentación y Pruebas

- **Swagger/OpenAPI**: Expuesto en `/swagger-ui.html`.
- **Tests**: JUnit 5 + Mockito. Seguir pirámide de testing: Unitarios (Dominio) > Integración (Infraestructura).

---

> Ver `ARCHITECTURE.md` para detalles de la fórmula de cálculo y contratos de API.
