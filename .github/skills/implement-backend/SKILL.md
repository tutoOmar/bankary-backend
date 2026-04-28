---
name: implement-backend
description: Implementa un feature completo en el backend. Requiere spec con status APPROVED en .github/specs/.
argument-hint: "<nombre-feature>"
---

# Implement Backend (Hexagonal)

## Prerequisitos
1. Leer spec: `.github/specs/<feature>.spec.md`
2. Leer stack y arquitectura: `ARCHITECTURE.md` y `.github/instructions/backend.instructions.md`

## Orden de implementación
```
Domain (Models/Ports) → Application (UseCases) → Infrastructure (Adapters)
```

| Capa | Responsabilidad |
|------|-----------------|
| **Domain** | Lógica pura, Agregados (Cotizacion), Entidades de negocio |
| **Application** | Casos de Uso, Mappers Domain-DTO, Application Services |
| **Infrastructure** | Adaptadores (Controllers, Repositories JPA, Clients) |

## Inyección de Dependencias
- Usar inyección por constructor obligatoriamente.
- No instanciar clases de infraestructura dentro del Dominio.

## Reglas Críticas
- **Idempotencia**: Validar siempre en `POST /v1/folios`.
- **Versionado**: Validar manualmente el campo `version` antes de persistir (409 Conflict).
- **Aislamiento**: El Dominio no debe conocer anotaciones de Spring ni de JPA.
