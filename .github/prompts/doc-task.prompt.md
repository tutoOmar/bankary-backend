---
description: 'Ejecuta el Documentation Agent para generar documentación técnica completa del feature implementado (README, API docs, ADRs, onboarding).'
agent: Documentation Agent
---

Ejecuta el Documentation Agent (MARCO DOC) para generar la documentación técnica del feature.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

**Instrucciones para @Documentation Agent:**

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName}.spec.md`.
2. **Revisa el código implementado** en `plataforma-danos-back/` y `plataforma-core-ohs/`.
3. **Genera los siguientes entregables**:
   - Actualiza `README.md` (si hay cambios en configuración/arranque).
   - Genera documentación de API: `docs/output/api/${input:featureName}-api.md` (OpenAPI).
   - Registra ADRs en `docs/output/adr/` (si hubo decisiones nuevas).
4. **Verifica contra ARCHITECTURE.md**: Asegurar que la documentación de cálculo sea coherente con las fórmulas oficiales.
