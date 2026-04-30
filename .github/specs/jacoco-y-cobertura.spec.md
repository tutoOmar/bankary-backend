---
id: SPEC-008
status: IMPLEMENTED
feature: jacoco-y-cobertura
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002, SPEC-005, SPEC-007]
---

# Spec: jacoco-y-cobertura — Reporte de cobertura y tests hasta 80%

## 1. REQUERIMIENTOS

### Descripción
Configurar JaCoCo en cliente-service y cuenta-service para generar reportes de
cobertura de código, identificar las clases críticas sin cobertura, y escribir los
tests unitarios necesarios para alcanzar un mínimo del 80% de cobertura de líneas
en la capa de dominio (use cases y validadores). La meta del 80% aplica sobre
`domain/` — no sobre infraestructura ni adaptadores.

### Requerimiento de Negocio
La cobertura de tests en la capa de negocio es el indicador más defendible en una
revisión técnica. Un 80% en domain/ demuestra que las reglas de negocio están
verificadas de forma automatizada y que el código es mantenible con confianza.

### Historias de Usuario

#### HU-01: Configurar JaCoCo y generar reporte base — ambos servicios
Como: Desarrollador
Quiero: que `mvn verify` genere un reporte HTML de cobertura en cada servicio
Para: saber exactamente qué clases y líneas están cubiertas antes de escribir tests
Prioridad: Alta
Estimación: S
Dependencias: ninguna
Capa: Build — pom.xml de cliente-service y cuenta-service

#### HU-02: Alcanzar 80% de cobertura en domain/ — ambos servicios
Como: Desarrollador o Tech Lead
Quiero: que la capa de dominio tenga al menos 80% de cobertura de líneas
Para: garantizar que las reglas de negocio críticas están verificadas automáticamente
Prioridad: Alta
Estimación: L
Dependencias: HU-01, SPEC-005 HU-01, SPEC-005 HU-02, SPEC-007 HU-02
Capa: Tests — src/test/java en cliente-service y cuenta-service

#### HU-03: Enforcement de cobertura mínima en el build
Como: Desarrollador
Quiero: que `mvn verify` falle automáticamente si la cobertura de domain/ baja del 80%
Para: evitar que futuros cambios degraden la cobertura sin saberlo
Prioridad: Media
Estimación: XS
Dependencias: HU-01, HU-02
Capa: Build — pom.xml de cliente-service y cuenta-service

---

## 2. DISEÑO

### Fase 1 — Configurar JaCoCo y leer el reporte base

#### Dependencia y plugin en pom.xml (cada servicio)

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.8.11</version>
      <executions>

        <!-- 1. Instrumentar el agente antes de los tests -->
        <execution>
          <id>prepare-agent</id>
          <goals><goal>prepare-agent</goal></goals>
        </execution>

        <!-- 2. Generar reporte HTML tras los tests -->
        <execution>
          <id>report</id>
          <phase>verify</phase>
          <goals><goal>report</goal></goals>
        </execution>

        <!-- 3. Enforcement: fallar si domain/ baja del 80% (activar en HU-03) -->
        <execution>
          <id>check</id>
          <phase>verify</phase>
          <goals><goal>check</goal></goals>
          <configuration>
            <rules>
              <rule>
                <element>PACKAGE</element>
                <includes>
                  <!-- Ajustar al package real del proyecto -->
                  <include>com.tuempresa.*.domain.*</include>
                </includes>
                <limits>
                  <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.80</minimum>
                  </limit>
                </limits>
              </rule>
            </rules>
          </configuration>
        </execution>

      </executions>
    </plugin>
  </plugins>
</build>
```

> **Orden de trabajo:** Agregar primero solo `prepare-agent` y `report`.
> Ejecutar `mvn verify` para ver el reporte base. Agregar `check` (HU-03)
> solo después de alcanzar el 80%, para que el build no falle durante el desarrollo.

#### Ubicación del reporte generado
```
target/site/jacoco/index.html   ← abrir en navegador
```

Ejecutar y abrir antes de escribir un solo test:
```bash
mvn verify
open target/site/jacoco/index.html   # macOS
xdg-open target/site/jacoco/index.html  # Linux
```

---

### Fase 2 — Identificar gaps y escribir tests

#### Clases prioritarias a cubrir (ordenadas por impacto)

Las siguientes clases concentran las reglas de negocio más críticas y deben
alcanzar cobertura individualmente cercana al 100%:

**cliente-service**
| Clase                     | Por qué es prioritaria                              |
|---------------------------|-----------------------------------------------------|
| `DocumentoValidator`      | Toda la lógica de validación de documentos colombianos |
| `ClienteUseCaseImpl`      | Orquesta creación, consulta y validaciones          |

**cuenta-service**
| Clase                     | Por qué es prioritaria                              |
|---------------------------|-----------------------------------------------------|
| `CuentaLimiteValidator`   | Regla de máximo 1 cuenta activa por tipo            |
| `CuentaUseCaseImpl`       | Orquesta creación, movimientos y validaciones       |

#### Clases a excluir del reporte (no agregan valor cubrir con tests unitarios)
```xml
<!-- Dentro de <configuration> del plugin report -->
<excludes>
  <exclude>**/*Application.class</exclude>
  <exclude>**/*Config.class</exclude>
  <exclude>**/*Entity.class</exclude>
  <exclude>**/*Request.class</exclude>
  <exclude>**/*Response.class</exclude>
  <exclude>**/*Exception.class</exclude>
  <exclude>**/dto/**</exclude>
</excludes>
```

---

### Fase 3 — Catálogo de tests a escribir

#### cliente-service — DocumentoValidator

```
TEST-CV-01: CC con 10 dígitos válidos → no lanza excepción
TEST-CV-02: CC con 8 dígitos (mínimo) → no lanza excepción
TEST-CV-03: CC con 7 dígitos → lanza DocumentoInvalidoException
            mensaje: "CC debe tener entre 8 y 10 dígitos numéricos"
TEST-CV-04: CC con 11 dígitos → lanza DocumentoInvalidoException
TEST-CV-05: CC con letras → lanza DocumentoInvalidoException
TEST-CV-06: CC con edad=17 → lanza DocumentoInvalidoException
            mensaje: "Cédula de Ciudadanía requiere edad mínima de 18 años"
TEST-CV-07: CC con edad=18 → no lanza excepción
TEST-CV-08: TI con 11 dígitos y edad=15 → no lanza excepción
TEST-CV-09: TI con 10 dígitos (mínimo) y edad=7 → no lanza excepción
TEST-CV-10: TI con edad=18 → lanza DocumentoInvalidoException
            mensaje: "Tarjeta de Identidad solo aplica para personas entre 7 y 17 años"
TEST-CV-11: TI con edad=6 → lanza DocumentoInvalidoException
TEST-CV-12: NIT con formato XXXXXXXXX-D válido → no lanza excepción
TEST-CV-13: NIT sin dígito verificador → lanza DocumentoInvalidoException
            mensaje: "NIT debe tener 9 dígitos numéricos más dígito verificador"
TEST-CV-14: CE con 6 caracteres alfanuméricos → no lanza excepción
TEST-CV-15: CE con 5 caracteres → lanza DocumentoInvalidoException
TEST-CV-16: PASAPORTE con formato válido → no lanza excepción
```

#### cliente-service — ClienteUseCaseImpl

```
TEST-CU-01: crearCliente() con datos válidos → retorna ClienteResponse con id generado
            (mockear repositorio para retornar entidad persistida)
TEST-CU-02: crearCliente() con documento duplicado → lanza excepción de duplicado
            (mockear repositorio para simular existencia previa)
TEST-CU-03: buscarClientePorId() con id existente → retorna ClienteResponse
TEST-CU-04: buscarClientePorId() con id inexistente → lanza ClienteNotFoundException
TEST-CU-05: crearCliente() con documento inválido → lanza DocumentoInvalidoException
            (sin necesidad de mockear repositorio — falla antes)
```

#### cuenta-service — CuentaLimiteValidator

```
TEST-CLV-01: cliente sin cuentas activas, tipoCuenta=AHORRO → no lanza excepción
TEST-CLV-02: cliente con 1 AHORRO activa, tipoCuenta=AHORRO → lanza CuentaDuplicadaException
             mensaje: "El cliente ya tiene una cuenta de tipo AHORRO activa"
TEST-CLV-03: cliente con 1 CORRIENTE activa, tipoCuenta=CORRIENTE → lanza CuentaDuplicadaException
             mensaje: "El cliente ya tiene una cuenta de tipo CORRIENTE activa"
TEST-CLV-04: cliente con 1 AHORRO activa, tipoCuenta=CORRIENTE → no lanza excepción
TEST-CLV-05: cliente con cuenta AHORRO inactiva (estado=false), tipoCuenta=AHORRO
             → no lanza excepción (la inactiva no cuenta para el límite)
```

#### cuenta-service — CuentaUseCaseImpl

```
TEST-CU-01: crearCuenta() sin cuentas previas → retorna CuentaResponse con id generado
TEST-CU-02: crearCuenta() con cuenta del mismo tipo activa → lanza CuentaDuplicadaException
TEST-CU-03: registrarMovimiento() en cuenta existente → actualiza saldo y retorna respuesta
TEST-CU-04: registrarMovimiento() en cuenta inexistente → lanza CuentaNotFoundException
TEST-CU-05: buscarCuentaPorId() con id existente → retorna CuentaResponse
TEST-CU-06: buscarCuentaPorId() con id inexistente → lanza CuentaNotFoundException
```

---

### Estructura de tests recomendada

```java
// Ejemplo: DocumentoValidatorTest.java
@DisplayName("DocumentoValidator")
class DocumentoValidatorTest {

    private final DocumentoValidator validator = new DocumentoValidator();

    @Test
    @DisplayName("CC con 10 dígitos y edad 25 no lanza excepción")
    void cc_valido_no_lanza() {
        assertDoesNotThrow(() -> validator.validate(TipoDocumento.CC, "1023456789", 25));
    }

    @Test
    @DisplayName("CC con 7 dígitos lanza DocumentoInvalidoException")
    void cc_menos_de_8_digitos_lanza() {
        DocumentoInvalidoException ex = assertThrows(
            DocumentoInvalidoException.class,
            () -> validator.validate(TipoDocumento.CC, "1234567", 25)
        );
        assertThat(ex.getMessage()).contains("CC debe tener entre 8 y 10 dígitos");
    }
}
```

```java
// Ejemplo: ClienteUseCaseImplTest.java
@ExtendWith(MockitoExtension.class)
class ClienteUseCaseImplTest {

    @Mock  ClienteRepository clienteRepository;
    @Mock  DocumentoValidator documentoValidator;
    @InjectMocks ClienteUseCaseImpl useCase;

    @Test
    @DisplayName("crearCliente con datos válidos retorna response con id")
    void crear_cliente_exitoso() {
        // Arrange
        ClienteCommand command = buildValidCommand();
        ClienteEntity saved = buildEntityWithId();
        when(clienteRepository.save(any())).thenReturn(saved);

        // Act
        ClienteResponse response = useCase.crearCliente(command);

        // Assert
        assertThat(response.getId()).isNotNull();
        verify(clienteRepository).save(any());
    }
}
```

---

## 3. LISTA DE TAREAS

### Fase 1 — Configurar JaCoCo (hacer primero)
- [ ] Agregar plugin JaCoCo al `pom.xml` de cliente-service con goals `prepare-agent` y `report`
- [ ] Agregar plugin JaCoCo al `pom.xml` de cuenta-service con goals `prepare-agent` y `report`
- [ ] Agregar bloque `<excludes>` para clases sin valor (entidades, DTOs, excepciones, Application)
- [ ] Ejecutar `mvn verify` en cliente-service y abrir `target/site/jacoco/index.html`
- [ ] Ejecutar `mvn verify` en cuenta-service y abrir `target/site/jacoco/index.html`
- [ ] Anotar el porcentaje de cobertura actual de `domain/` en cada servicio como baseline

### Fase 2 — Escribir tests (después de ver el reporte)

#### cliente-service
- [ ] Crear `DocumentoValidatorTest` — cubrir TEST-CV-01 al TEST-CV-16
- [ ] Crear o completar `ClienteUseCaseImplTest` — cubrir TEST-CU-01 al TEST-CU-05
- [ ] Ejecutar `mvn verify` y verificar que domain/ supera el 80%

#### cuenta-service
- [ ] Crear `CuentaLimiteValidatorTest` — cubrir TEST-CLV-01 al TEST-CLV-05
- [ ] Crear o completar `CuentaUseCaseImplTest` — cubrir TEST-CU-01 al TEST-CU-06
- [ ] Ejecutar `mvn verify` y verificar que domain/ supera el 80%

### Fase 3 — Enforcement (solo cuando ambos servicios superen el 80%)
- [ ] Agregar goal `check` con `minimum=0.80` sobre el package `domain` en cliente-service
- [ ] Agregar goal `check` con `minimum=0.80` sobre el package `domain` en cuenta-service
- [ ] Ejecutar `mvn verify` en ambos y confirmar que el build pasa sin errores de cobertura
- [ ] Confirmar que al eliminar un test el build falla con mensaje de cobertura insuficiente