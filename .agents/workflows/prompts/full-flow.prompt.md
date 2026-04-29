---
description: 'Orquesta el flujo completo ASDD: Spec → [Backend ∥ Frontend ∥ DB] → [Tests Backend ∥ Tests Frontend] → QA → DOC (opcional). Requiere un requerimiento de negocio como input.'
agent: Orchestrator
---

Inicia el flujo completo ASDD con paralelismo máximo.

**Feature**: ${input:featureName:nombre del feature en kebab-case}
**Requerimiento**: ${input:requirement:descripción funcional del feature}

## El @Orchestrator ejecuta automáticamente:

1. **[FASE 1 — Spec]** `Spec Generator` → Genera la spec en `.github/specs/`.
2. **[FASE 2 — Implementación Backend ∥ DB]** al aprobar la spec:
   - `Backend Developer` (Hexagonal: Domain → Application → Infrastructure).
   - `Database Agent` (PostgreSQL + Migraciones Flyway).
3. **[FASE 3 — Testing]** al completar implementación:
   - `Test Engineer Backend` (JUnit 5 + Mockito).
4. **[FASE 4 — QA]** `QA Agent` (Gherkin, Riesgos, k6).
5. **[FASE 5 — Doc]** `Documentation Agent` (OpenAPI, README).

**Todo el flujo se rige por las reglas de ARCHITECTURE.md: Idempotencia en folios y versionado optimista.**
