# ASDD Framework — Cotizador de Seguros de Daños (Backend)

**ASDD** (Agent Spec Software Development) es un framework de desarrollo asistido por IA que organiza el trabajo de software en cinco fases orquestadas por agentes especializados.

Este repositorio contiene ÚNICAMENTE el backend del cotizador, compuesto por dos microservicios Spring Boot.

```
Requerimiento → Spec → [Backend ∥ DB] → [Tests BE] → QA → Doc
```

---

## Estructura del Proyecto

- **`plataforma-danos-back`**: Backend principal (Spring Boot 3.2, Java 17, PostgreSQL).
- **`plataforma-core-ohs`**: Stub de servicios core (códigos postales, tarifas).

Ver [ARCHITECTURE.md](./ARCHITECTURE.md) para detalles técnicos, fórmulas de cálculo y decisiones de diseño.

---

## Flujo de Trabajo

### Paso 1 — Spec (Fase 1)
Genera la especificación técnica en `.github/specs/`.
```
/generate-spec <nombre-feature>
```

### Paso 2 — Implementación (Fase 2)
Implementa el backend en el microservicio correspondiente.
```
/implement-backend <nombre-feature>
```

### Paso 3 — Tests (Fase 3)
Genera tests unitarios y de integración.
```
/unit-testing <nombre-feature>
```

### Paso 4 — QA (Fase 4)
Genera casos Gherkin y análisis de riesgos.
```
/gherkin-case-generator
```

---

## Reglas de Oro

1. **Idempotencia**: `POST /v1/folios` debe ser idempotente.
2. **Arquitectura Hexagonal**: Separar lógica de negocio de infraestructura.
3. **DDD**: `Cotizacion` es el Aggregate Root único.
4. **Versionado**: Gestión manual de versión para concurrencia optimista (409 Conflict).

---

## Instrucciones Automáticas

| Archivo activo | Instructions aplicadas |
|---|---|
| `backend/src/**/*.java` | `instructions/backend.instructions.md` |
| `backend/src/test/**` | `instructions/tests.instructions.md` |

---

> Para el arranque local, usa `docker-compose up --build` en la raíz.
