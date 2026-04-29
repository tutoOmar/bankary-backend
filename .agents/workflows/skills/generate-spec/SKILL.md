---
name: generate-spec
description: Genera una spec técnica ASDD en .github/specs/<feature>.spec.md. Obligatorio antes de cualquier implementación.
argument-hint: "<nombre-feature>: <descripción del requerimiento>"
---

# Generate Spec

## Definition of Ready — validar antes de generar

Una historia puede generar spec solo si cumple:

- [ ] Estructura **Como / Quiero / Para que** completa
- [ ] Términos canónicos del dominio (ver `CLAUDE.md` / `copilot-instructions.md` → Diccionario de Dominio)
- [ ] Criterios BDD: **Dado / Cuando / Entonces** (feliz + validaciones + errores)
- [ ] Contrato API explícito si aplica (método, ruta `/api/v1/...`, request, response, códigos HTTP)
- [ ] Alineada con arquitectura y stack (Java 17 + PostgreSQL + Hexagonal)
- [ ] Dependencias y riesgos identificados

Si el requerimiento no cumple el DoR → listar las preguntas pendientes antes de generar.

## Proceso

1. Busca requerimiento en `.github/requirements/<feature>.md`
2. Lee las instrucciones de stack: `ARCHITECTURE.md` y `.github/instructions/backend.instructions.md`
3. Explora código existente — no duplicar modelos ni endpoints existentes
4. Valida DoR (arriba)
5. Usa plantilla: `.github/skills/generate-spec/spec-template.md`
6. Guarda en `.github/specs/<nombre-en-kebab-case>.spec.md`

## Frontmatter obligatorio

```yaml
---
id: SPEC-###
status: DRAFT
feature: nombre-del-feature
created: YYYY-MM-DD
updated: YYYY-MM-DD
author: spec-generator
version: "1.0"
related-specs: []
---
```

## Secciones obligatorias

- `## 1. REQUERIMIENTOS` — HU (Como/Quiero/Para) + criterios Gherkin + reglas de negocio (Idempotencia/Versionado).
- `## 2. DISEÑO` — Agregados de Dominio, Entidades JPA, Endpoints API (OpenAPI 3.0), Flujos de cálculo.
- `## 3. LISTA DE TAREAS` — Checklists backend `[ ]` y QA `[ ]`.

## Restricciones

- Solo leer + crear. No modificar código existente.
- Status siempre `DRAFT`. El usuario aprueba antes de implementar.
