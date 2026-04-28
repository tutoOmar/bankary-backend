---
name: unit-testing
description: Genera tests unitarios e integración para el backend Java (JUnit 5 + Mockito). Lee la spec y el código implementado.
argument-hint: "<nombre-feature>"
---

# Unit Testing (Backend Java)

## Definition of Done — verificar al completar

- [ ] Cobertura ≥ 80% en lógica de negocio (domain/application)
- [ ] Tests aislados — Mocks para dependencias externas y bases de datos
- [ ] Escenario feliz + errores de negocio + validaciones de entrada cubiertos
- [ ] Uso estricto del patrón AAA (Given-When-Then)

## Prerequisito — Lee en paralelo

```
ARCHITECTURE.md                        (contexto y motor de cálculo)
.github/specs/<feature>.spec.md        (criterios de aceptación)
código en plataforma-danos-back/ o plataforma-core-ohs/
.github/instructions/tests.instructions.md  (JUnit 5 + Mockito)
```

## Estructura de Salida -> `backend/src/test/java/`

| Capa | Clase Test | Cubre |
|---------|-------|---|
| **Domain** | `[Entity]ServiceTest.java` | Lógica de negocio pura, cálculos |
| **Application** | `[UseCase]Test.java` | Orquestación, validación de versionado, idempotencia |
| **Infrastructure** | `[Adapter]ControllerTest.java` | Endpoints: 201/200, 400, 409 Conflict, 404 |
| **Infrastructure** | `[Adapter]RepositoryTest.java` | Mapeo JPA y queries (opcional, solo lógica compleja) |

## Patrones core (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class CreateFolioUseCaseTest {
    @Mock private CotizacionRepository repository;
    @InjectMocks private CreateFolioUseCase useCase;

    @Test
    void shouldReturnExistingFolioWhenAlreadyExists() {
        // GIVEN
        String folio = "DAN-123";
        Cotizacion existente = Cotizacion.builder().numeroFolio(folio).build();
        when(repository.findByNumeroFolio(folio)).thenReturn(Optional.of(existente));

        // WHEN
        Cotizacion result = useCase.execute(new CreateFolioCommand(folio));

        // THEN
        assertEquals(existente, result);
        verify(repository, never()).save(any()); // Idempotencia
    }
}
```

## Restricciones

- No modificar código fuente (solo `src/test/`).
- Nunca conectar a bases de datos de producción o servicios reales.
- Cobertura mínima ≥ 80% en lógica de negocio y casos de uso.
