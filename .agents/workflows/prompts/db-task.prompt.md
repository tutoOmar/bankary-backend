---
description: 'Ejecuta el Database Agent para diseñar esquemas de datos, generar scripts de migración, seeders y optimizar queries a partir de la spec aprobada.'
agent: Database Agent
---

Ejecuta el Database Agent (MARCO DB) para diseñar y gestionar el modelo de persistencia del feature.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

**Instrucciones para @Database Agent:**

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName}.spec.md` — sección "Diseño de Datos".
2. **Revisa ARCHITECTURE.md** para confirmar tipos de datos (BigDecimal para primas).
3. **Crea o actualiza Entidades JPA**:
   - `src/main/java/com/asdd/danos/infrastructure/adapter/out/persistence/entity/`
4. **Genera script de migración SQL** (Flyway):
   - `src/main/resources/db/migration/V[Version]__[Descripcion].sql`
5. **Alineación de nombres**: Usar `snake_case` para el esquema físico y `camelCase` para el modelo Java.

## Restricciones:
- Las columnas de Prima y Suma Asegurada deben ser `DECIMAL(19,4)` o similar.
- Toda tabla de negocio debe tener `created_at` (TIMESTAMPTZ) y `updated_at`.
- El agregado `Cotizacion` debe tener una columna `version` (INT) para el bloqueo optimista manual.
