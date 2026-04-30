---
name: backend-task
description: Implementa una funcionalidad en el backend Java Spring Boot basada en una spec ASDD aprobada.
argument-hint: "<nombre-feature> (debe existir .github/specs/<nombre-feature>.spec.md)"
agent: Backend Developer
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName}.spec.md`.
2. **Revisa la arquitectura** en `ARCHITECTURE.md`.
3. **Implementa en orden** (Arquitectura Hexagonal):
   - **Domain**: Entidades de negocio y Puertos (interfaces).
   - **Application**: Casos de Uso y Mappers.
   - **Infrastructure**: Adaptadores (Controllers, Repositories JPA).
4. **Respetar Idempotencia** en Folios y **Versionado Optimista** manual.
5. **Verifica sintaxis** con `./mvnw compile` o `./gradlew classes`.

## Restricciones:
- Usar inyección por constructor (Lombok `@RequiredArgsConstructor`).
- El Dominio no debe conocer detalles de infraestructura (DB/Web).
- Mapear DTOs a Objetos de Dominio en la capa de Aplicación.
