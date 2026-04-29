---
applyTo: "backend/src/test/**/*.java"
---

> **Scope**: Se aplica a proyectos con JUnit 5 y Java 17.

# Instrucciones para Pruebas Backend (JUnit 5 + Mockito)

## Principios

- **Independencia**: cada test es 100% independiente — sin estado compartido entre tests.
- **Aislamiento**: mockear SIEMPRE dependencias externas (otras APIs, sistema de archivos).
- **Esquema AAA**: **Given** (preparar), **When** (ejecutar), **Then** (verificar).
- **Cobertura**: cubrir happy path, error path y edge cases. Meta: ≥ 80%.

## Estructura de archivos
```
backend/src/test/java/com/asdd/danos/
  application/usecase/CreateFolioUseCaseTest.java
  infrastructure/adapter/in/FolioControllerTest.java
  infrastructure/adapter/out/persistence/CotizacionRepositoryTest.java
```

## Convenciones de Nomenclatura
- Clase: `[ClaseAtester]Test.java`
- Método: `should[Accion]When[Escenario]` (ej: `shouldCreateUserWhenEmailIsValid`).

## Mocks y Fixtures
- Usar `@ExtendWith(MockitoExtension.class)`.
- Mockear con `@Mock` e inyectar con `@InjectMocks`.
- **Core OHS**: Mockear las respuestas de `plataforma-core-ohs` usando Mockito o archivos JSON de fixtures.

```java
@ExtendWith(MockitoExtension.class)
class CalculatePremiumUseCaseTest {
    @Mock private TariffPort tariffPort;
    @InjectMocks private CalculatePremiumUseCase useCase;

    @Test
    void shouldCalculateTotalCorrecly() {
        // GIVEN
        Cotizacion aggregate = CotizacionMother.valid();
        when(tariffPort.getTariff(any())).thenReturn(new BigDecimal("1.25"));

        // WHEN
        Cotizacion result = useCase.execute(aggregate);

        // THEN
        assertThat(result.getPrimaNeta()).isGreaterThan(BigDecimal.ZERO);
        verify(tariffPort).getTariff(any());
    }
}
```

## Nunca hacer

- Tests que dependan de una base de datos real en pruebas unitarias (usar H2 o Testcontainers para integración).
- Lógica condicional (if/else) dentro de los tests.
- Omitir aserciones (un test sin `assertThat` o `assertEquals` no es un test).
- Hardcodear datos sensibles o secretos.

---

> Para quality gates y pirámide de testing, ver `.github/docs/lineamientos/qa-guidelines.md`.
