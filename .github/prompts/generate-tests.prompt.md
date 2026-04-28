name: generate-tests
description: Genera pruebas unitarias para el backend (JUnit 5 + Mockito) basadas en la spec ASDD y el código implementado.
argument-hint: "<nombre-feature>"
agent: Orchestrator
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName}.spec.md` — sección "Plan de Pruebas Unitarias".
2. **Lee ARCHITECTURE.md** para entender las capas de Dominio, Aplicación e Infraestructura.
3. **Genera los tests** delegando a `Test Engineer Backend` en:
   - `src/test/java/com/asdd/danos/domain/model/`
   - `src/test/java/com/asdd/danos/application/usecase/`
   - `src/test/java/com/asdd/danos/infrastructure/adapter/in/`
4. **Verifica** con:
   - Backend: `./mvnw test` o `./gradlew test` (según herramienta de building).

## Cobertura obligatoria por test:
- ✅ Happy path (flujo exitoso)
- ❌ Error path (ConflictException por versión, 400 Validation)
- 🔲 Edge cases (folios inexistentes, cálculos incalculables)

## Restricciones:
- Cada test debe ser independiente.
- Mockear dependencias externas (otras APIs, persistencia).
- Usar JUnit 5 + Mockito + AssertJ.
