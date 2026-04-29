---
description: 'Ejecuta el QA Agent con los 8 skills secuenciales para generar el plan de calidad completo basado en la spec aprobada.'
agent: QA Agent
---

Ejecuta el QA Agent completo con los 8 skills en secuencia.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

**Instrucciones para @QA Agent:**

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName}.spec.md`.
2. **Ejecuta los skills de QA** en secuencia:
   - `/gherkin-case-generator` → Escenarios de negocio (Idempotencia, Cálculos).
   - `/risk-identifier` → Matriz de riesgos ASD.
   - `/performance-analyzer` → SLAs de respuesta del motor de cálculo (k6).
   - `/automation-flow-proposer` → Plan de automatización de APIs.
3. **Verifica contra ARCHITECTURE.md**: Los casos Gherkin deben validar explícitamente las fórmulas de Remoción de Escombros y Extensión de Cobertura.
