# Copilot Instructions — Seguro de Daños BE

## ASDD Workflow (Agent Spec Software Development)

Este repositorio contiene el **Backend** del cotizador de seguros de daños, orquestado bajo el flujo ASDD.

```
[Orchestrator] → [Spec Generator] → [Backend ∥ DB] → [Tests BE] → [QA] → [Doc]
```

### Fases del flujo ASDD
1. **Spec**: El agente `spec-generator` genera la spec en `.github/specs/<feature>.spec.md`.
2. **Implementación**: `backend-developer` + `database-agent`.
3. **Tests**: `test-engineer-backend`.
4. **QA**: `qa-agent` genera estrategia, Gherkin y riesgos.

### Skills disponibles (slash commands):
- `/asdd-orchestrate` — orquesta el flujo completo ASDD
- `/generate-spec` — genera spec técnica en `.github/specs/`
- `/implement-backend` — implementa feature en el microservicio correspondiente
- `/unit-testing` — genera suite de tests unitarios/integración backend
- `/gherkin-case-generator` — casos Given-When-Then + datos de prueba
- `/risk-identifier` — clasificación de riesgos ASD

---

## Mapa de Archivos ASDD (Solo Backend)

### Agentes
| Agente | Fase | Ruta |
|---|---|---|
| Orchestrator | Entry point | `.github/agents/orchestrator.agent.md` |
| Spec Generator | Fase 1 | `.github/agents/spec-generator.agent.md` |
| Backend Developer | Fase 2 | `.github/agents/backend-developer.agent.md` |
| Database Agent | Fase 2 | `.github/agents/database.agent.md` |
| Test Engineer Backend | Fase 3 | `.github/agents/test-engineer-backend.agent.md` |
| QA Agent | Fase 4 | `.github/agents/qa.agent.md` |
| Documentation Agent | Fase 5 | `.github/agents/documentation.agent.md` |

### Instructions (path-scoped)
| Scope | Ruta | Se aplica a |
|---|---|---|
| Backend | `.github/instructions/backend.instructions.md` | `backend/src/**/*.java` |
| Tests | `.github/instructions/tests.instructions.md` | `backend/src/test/**` |

---

## Reglas de Oro (Seguro de Daños)

1. **Agregado Raíz**: `Cotizacion` es la única raíz. No crear repositorios para Ubicacion.
2. **Idempotencia**: `POST /v1/folios` debe ser idempotente por `numeroFolio`.
3. **Optimistic Locking**: Validación manual de `version` en Casos de Uso. No usar `@Version`. Desajuste lanza 409 Conflict.
4. **Arquitectura Hexagonal**: Mantener el `domain` libre de dependencias de Spring.
5. **Cálculo**: Seguir fórmulas en `ARCHITECTURE.md`.

---

## Diccionario de Dominio

| Término | Definición | Sinónimos rechazados |
|---------|-----------|---------------------|
| **Folio** (`numeroFolio`) | Identificador de negocio único de la cotización | ID, número técnico |
| **Cotización** | Agregado raíz que agrupa datos y ubicaciones | Póliza, solicitud |
| **Ubicación** | Bien inmueble asegurable dentro de una cotización | Propiedad, item |
| **Prima Neta** | Monto del seguro antes de factores comerciales | Costo base |
| **Giro** | Clasificación de actividad económica del inmueble | Actividad, uso |
| **Clave Incendio** | Identificador para tarificación de incendio | Código tasa |
| **Stub/Core OHS** | Servicio mock `plataforma-core-ohs` | Legacy, API real |

**Reglas:** `numeroFolio` es String. `Prima` es BigDecimal. `snake_case` en DB, `camelCase` en Java.
