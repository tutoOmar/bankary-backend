---
id: SPEC-005
status: IN_PROGRESS
feature: reglas-de-negocio
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002]
---

# Spec: reglas-de-negocio — Validaciones de documento y límites de cuentas

## 1. REQUERIMIENTOS

### Descripción
Definir y implementar reglas de negocio colombianas para validación de documentos
de identidad en cliente-service y límites de cuentas por cliente en cuenta-service.
Estas reglas hacen el sistema más robusto y defendible en entrevista técnica.

### Requerimiento de Negocio
El sistema debe rechazar documentos con formato inválido según el tipo colombiano,
cruzar tipo de documento con edad del cliente, y evitar que un cliente tenga más
de una cuenta activa del mismo tipo.

### Historias de Usuario

#### HU-01: Validación de tipo y formato de documento — cliente-service
Como: Operador del sistema
Quiero: que el sistema valide el tipo y formato del documento de identidad
Para: garantizar integridad de datos según normativa colombiana
Prioridad: Alta
Estimación: M
Dependencias: SPEC-001 HU-01
Capa: Backend — cliente-service

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Documento CC válido
  Dado que: el payload tiene tipoDocumento=CC y numeroDocumento=1023456789 (10 dígitos)
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 201 y el cliente se crea exitosamente
```

**Happy Path**
```gherkin
CRITERIO-1.2: Documento TI válido para menor de edad
  Dado que: el payload tiene tipoDocumento=TI, numeroDocumento=1023456789012 (11 dígitos)
             y edad=15
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 201 y el cliente se crea exitosamente
```

**Error Path**
```gherkin
CRITERIO-1.3: CC con formato inválido
  Dado que: el payload tiene tipoDocumento=CC y numeroDocumento=123 (menos de 8 dígitos)
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 400 con mensaje "CC debe tener entre 8 y 10 dígitos numéricos"
```

**Error Path**
```gherkin
CRITERIO-1.4: TI usado por mayor de edad
  Dado que: el payload tiene tipoDocumento=TI y edad=20
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 400 con mensaje
            "Tarjeta de Identidad solo aplica para personas entre 7 y 17 años"
```

**Error Path**
```gherkin
CRITERIO-1.5: CC usado por menor de edad
  Dado que: el payload tiene tipoDocumento=CC y edad=15
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 400 con mensaje
            "Cédula de Ciudadanía requiere edad mínima de 18 años"
```

**Error Path**
```gherkin
CRITERIO-1.6: NIT con formato inválido
  Dado que: el payload tiene tipoDocumento=NIT y numeroDocumento=12345 (menos de 9 dígitos)
  Cuando: se realiza POST /api/v1/clientes
  Entonces: devuelve 400 con mensaje "NIT debe tener 9 dígitos numéricos más dígito verificador"
```

#### HU-02: Límite de cuentas por cliente — cuenta-service
Como: Sistema financiero interno
Quiero: que un cliente no pueda tener más de una cuenta activa por tipo
Para: mantener una estructura de cuentas ordenada y coherente
Prioridad: Alta
Estimación: S
Dependencias: SPEC-002 HU-02
Capa: Backend — cuenta-service

#### Criterios de Aceptación — HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Crear cuenta de ahorro siendo la primera del cliente
  Dado que: el cliente no tiene ninguna cuenta activa
  Cuando: se realiza POST /api/v1/cuentas con tipoCuenta=AHORRO
  Entonces: devuelve 201 con la cuenta creada
```

**Happy Path**
```gherkin
CRITERIO-2.2: Crear cuenta corriente teniendo ya una de ahorro
  Dado que: el cliente tiene 1 cuenta de ahorro activa y 0 corrientes
  Cuando: se realiza POST /api/v1/cuentas con tipoCuenta=CORRIENTE
  Entonces: devuelve 201 con la cuenta creada
```

**Error Path**
```gherkin
CRITERIO-2.3: Intentar crear segunda cuenta de ahorro
  Dado que: el cliente ya tiene una cuenta de tipo AHORRO activa
  Cuando: se realiza POST /api/v1/cuentas con tipoCuenta=AHORRO
  Entonces: devuelve 409 con mensaje
            "El cliente ya tiene una cuenta de tipo AHORRO activa"
```

**Error Path**
```gherkin
CRITERIO-2.4: Intentar crear segunda cuenta corriente
  Dado que: el cliente ya tiene una cuenta de tipo CORRIENTE activa
  Cuando: se realiza POST /api/v1/cuentas con tipoCuenta=CORRIENTE
  Entonces: devuelve 409 con mensaje
            "El cliente ya tiene una cuenta de tipo CORRIENTE activa"
```

### Reglas de Negocio

#### Documentos de identidad (cliente-service)
1. El campo `tipoDocumento` es obligatorio. Valores aceptados:
   CC | TI | CE | NIT | PASAPORTE

2. Formatos por tipo:
   - CC  (Cédula de Ciudadanía):  8–10 dígitos numéricos. Solo mayores de 18 años.
   - TI  (Tarjeta de Identidad):  10–11 dígitos numéricos. Solo entre 7 y 17 años.
   - CE  (Cédula de Extranjería): 6–12 caracteres alfanuméricos. Sin restricción de edad.
   - NIT (Número de Identificación Tributaria): 9 dígitos numéricos + 1 dígito verificador
         separado por guion. Formato: XXXXXXXXX-D. Sin restricción de edad.
   - PASAPORTE: 5–12 caracteres alfanuméricos. Sin restricción de edad.

3. Cruce documento–edad:
   - TI requiere edad entre 7 y 17 años inclusive.
   - CC requiere edad >= 18 años.
   - CE, NIT, PASAPORTE no tienen restricción de edad.

4. La unicidad se mantiene sobre la combinación (tipoDocumento, numeroDocumento).
   Dos clientes pueden tener el mismo número si el tipo es diferente.

5. Los mensajes de error deben ser descriptivos e indicar exactamente qué regla se violó.

#### Límites de cuentas (cuenta-service)
6. Un cliente puede tener máximo 1 cuenta AHORRO activa y 1 cuenta CORRIENTE activa.
7. El límite aplica solo sobre cuentas con estado=true. Una cuenta inactiva (estado=false)
   no cuenta para el límite, permitiendo crear una nueva del mismo tipo.
8. La validación se realiza en MovimientoUseCaseImpl antes de persistir la nueva cuenta.
9. HTTP 409 cuando se viola el límite, con mensaje indicando el tipo de cuenta duplicado.

---

## 2. DISEÑO

### Cambios en cliente-service

#### Modelo de dominio
- Agregar campo `tipoDocumento: TipoDocumento` (ENUM) a Persona y ClienteEntity.
- Renombrar campo `identificacion` a `numeroDocumento` para mayor claridad.
- Tabla persona: agregar columna `tipo_documento VARCHAR(20) NOT NULL`.
- Constraint UNIQUE pasa a ser sobre (tipo_documento, numero_documento).

#### Validación de documento
Crear clase de dominio: `DocumentoValidator`
  - Método: validate(tipoDocumento, numeroDocumento, edad): void
  - Lanza DocumentoInvalidoException (extends RuntimeException) con mensaje descriptivo.
  - Patrones regex por tipo:
    - CC:         ^\d{8,10}$
    - TI:         ^\d{10,11}$
    - CE:         ^[a-zA-Z0-9]{6,12}$
    - NIT:        ^\d{9}-\d{1}$
    - PASAPORTE:  ^[a-zA-Z0-9]{5,12}$
  - Lógica de cruce edad–documento separada en método privado validateEdadDocumento().

#### Ubicación en hexagonal
- DocumentoValidator vive en domain/ — es lógica de negocio pura, sin dependencias de infra.
- ClienteUseCaseImpl llama a DocumentoValidator.validate() antes de persistir.
- DocumentoInvalidoException mapeada en @RestControllerAdvice → HTTP 400.

#### Cambios en ClienteRequest
- Agregar campo: tipoDocumento (String, obligatorio, valores: CC|TI|CE|NIT|PASAPORTE)
- Renombrar: identificacion → numeroDocumento
- Actualizar seed sql/cliente/02_seed.sql:
  - Jose Lema:          tipoDocumento=CC,  numeroDocumento=10232345678
  - Marianela Montalvo: tipoDocumento=CC,  numeroDocumento=10298765432
  - Juan Osorio:        tipoDocumento=CC,  numeroDocumento=10287654321

### Cambios en cuenta-service

#### Validación de límite de cuentas
Crear clase de dominio: `CuentaLimiteValidator`
  - Método: validarLimitePorTipo(clienteId, tipoCuenta, cuentasActivas): void
  - Lanza CuentaDuplicadaException (extends RuntimeException) con mensaje descriptivo.
  - Lógica: contar cuentas activas del cliente por tipo. Si count >= 1 → lanzar excepción.

#### Ubicación en hexagonal
- CuentaLimiteValidator vive en domain/ — lógica pura sin dependencias de infra.
- CuentaUseCaseImpl consulta cuentas activas del cliente antes de crear una nueva,
  luego llama a CuentaLimiteValidator.validarLimitePorTipo().
- CuentaDuplicadaException mapeada en @RestControllerAdvice → HTTP 409.

#### Nuevo método en CuentaRepository (port out)
- findByClienteIdAndEstadoTrue(clienteId: UUID): List<Cuenta>

### Manejo de excepciones nuevas
Agregar al @RestControllerAdvice existente en cada servicio:

cliente-service:
  - DocumentoInvalidoException → 400 con campo "error": mensaje descriptivo

cuenta-service:
  - CuentaDuplicadaException → 409 con campo "error": mensaje descriptivo

Formato de respuesta de error estándar (ya definido en SPEC-001):
  { "timestamp": "...", "status": 400, "error": "Bad Request", "message": "..." }

---

## 3. LISTA DE TAREAS

### cliente-service
- [x] Agregar ENUM TipoDocumento { CC, TI, CE, NIT, PASAPORTE } en domain/model
- [x] Agregar columna tipo_documento a PersonaEntity y actualizar constraint UNIQUE
      a (tipo_documento, numero_documento)
- [x] Renombrar campo identificacion → numero_documento en PersonaEntity,
      ClienteRequest, ClienteResponse y Commands
- [x] Crear DocumentoValidator en domain/ con regex por tipo y cruce edad–documento
- [x] Crear DocumentoInvalidoException en domain/
- [x] Actualizar ClienteUseCaseImpl para llamar DocumentoValidator.validate()
      antes de persistir
- [x] Mapear DocumentoInvalidoException → 400 en @RestControllerAdvice
- [x] Actualizar sql/cliente/01_schema.sql: agregar columna tipo_documento
      y actualizar constraint UNIQUE
- [x] Actualizar sql/cliente/02_seed.sql con tipoDocumento y numeroDocumento reales
- [x] Unit test: DocumentoValidator — CC con menos de 8 dígitos lanza excepción
- [x] Unit test: DocumentoValidator — TI con edad=20 lanza excepción

### cuenta-service
- [x] Crear ENUM TipoCuenta { AHORRO, CORRIENTE } en domain/model si no existe
- [x] Agregar método findByClienteIdAndEstadoTrue() en CuentaRepository (port out)
      y su implementación en CuentaJpaRepository
- [x] Crear CuentaLimiteValidator en domain/ con lógica de límite por tipo
- [x] Crear CuentaDuplicadaException en domain/
- [x] Actualizar CuentaUseCaseImpl para consultar cuentas activas y llamar
      CuentaLimiteValidator antes de persistir una nueva cuenta
- [x] Mapear CuentaDuplicadaException → 409 en @RestControllerAdvice
- [x] Unit test: CuentaLimiteValidator — cliente con AHORRO activo intenta crear
      otro AHORRO lanza CuentaDuplicadaException