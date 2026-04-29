---
id: SPEC-007
status: IN_PROGRESS
feature: logs-y-manejo-de-errores
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002, SPEC-005, SPEC-006]
---

# Spec: logs-y-manejo-de-errores — Cobertura transversal de logging y excepciones

## 1. REQUERIMIENTOS

### Descripción
Agregar logging estructurado con SLF4J + Logback (incluido por defecto en Spring Boot)
en los puntos críticos de cliente-service y cuenta-service, y auditar los
`@RestControllerAdvice` existentes para garantizar que toda excepción quede registrada
y devuelva una respuesta coherente al cliente. El objetivo es que cualquier fallo
en producción sea rastreable sin necesidad de conectarse al servidor.

### Requerimiento de Negocio
En un sistema financiero, la ausencia de logs es un bloqueador en auditorías y
dificulta la resolución de incidentes. Todo error que experimente un usuario debe
poder reproducirse desde los logs sin información adicional.

### Historias de Usuario

#### HU-01: Auditoría de excepciones no manejadas — ambos servicios
Como: Desarrollador que revisa incidentes en producción
Quiero: que toda excepción que llegue al `@RestControllerAdvice` quede registrada
        en el log con nivel ERROR e información suficiente para reproducirla
Para: poder diagnosticar fallos sin acceso al servidor ni al cliente
Prioridad: Alta
Estimación: M
Dependencias: SPEC-001 HU-01, SPEC-002 HU-01, SPEC-005 HU-01
Capa: Backend transversal — cliente-service y cuenta-service

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Excepción de negocio genera log ERROR con contexto
  Dado que: se realiza una petición que viola una regla de negocio
            (ej: DocumentoInvalidoException, CuentaDuplicadaException)
  Cuando: el @RestControllerAdvice captura la excepción
  Entonces: el log registra un mensaje ERROR que incluye:
            - nombre de la excepción
            - mensaje descriptivo
            - método HTTP y URI de la petición
  Y: la respuesta HTTP devuelve el código y mensaje correcto (400 o 409)
```

**Happy Path**
```gherkin
CRITERIO-1.2: Excepción inesperada genera log ERROR con stack trace
  Dado que: ocurre una excepción no anticipada (NullPointerException, RuntimeException genérica)
  Cuando: el handler genérico del @RestControllerAdvice la captura
  Entonces: el log registra ERROR con stack trace completo
  Y: la respuesta HTTP devuelve 500 con mensaje genérico sin exponer detalles internos
```

**Error Path**
```gherkin
CRITERIO-1.3: Respuesta 500 no expone stack trace al cliente
  Dado que: ocurre una excepción inesperada en cualquier endpoint
  Cuando: el cliente recibe la respuesta
  Entonces: el body de respuesta contiene únicamente
            { "timestamp": "...", "status": 500, "error": "Internal Server Error",
              "message": "Ha ocurrido un error inesperado" }
  Y: el stack trace solo existe en el log del servidor, nunca en la respuesta HTTP
```

#### HU-02: Logging en capa de casos de uso — ambos servicios
Como: Desarrollador que depura flujos de negocio
Quiero: que los casos de uso registren INFO al iniciar operaciones relevantes
        y ERROR cuando fallen
Para: poder seguir el flujo de una petición a través de los logs sin modo DEBUG
Prioridad: Alta
Estimación: M
Dependencias: HU-01
Capa: Backend — domain/usecase en cliente-service y cuenta-service

#### Criterios de Aceptación — HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Creación exitosa genera log INFO
  Dado que: se crea un cliente o una cuenta exitosamente
  Cuando: el caso de uso completa la operación
  Entonces: el log registra INFO con el identificador del recurso creado
            Ejemplo: "Cliente creado exitosamente | id=<uuid> | tipoDocumento=CC"
```

**Happy Path**
```gherkin
CRITERIO-2.2: Operación fallida genera log ERROR antes de lanzar excepción
  Dado que: la validación de negocio rechaza la operación
  Cuando: el caso de uso detecta la violación
  Entonces: el log registra ERROR con motivo claro antes de lanzar la excepción
            Ejemplo: "Documento inválido | tipo=CC | numero=123 | motivo=menos de 8 dígitos"
```

#### HU-03: Configuración de niveles de log por entorno
Como: DevOps o desarrollador
Quiero: que el nivel de log sea configurable por variable de entorno
Para: usar INFO/ERROR en producción y DEBUG en local sin cambiar código
Prioridad: Media
Estimación: XS
Dependencias: SPEC-006 HU-01
Capa: Configuración — ambos servicios

---

## 2. DISEÑO

### Librería de logging
Spring Boot incluye SLF4J + Logback por defecto. No se requiere dependencia adicional.
Usar la anotación de Lombok `@Slf4j` en cada clase que necesite logger, o declarar
manualmente:

```java
private static final Logger log = LoggerFactory.getLogger(NombreClase.class);
```

Se prefiere `@Slf4j` de Lombok si ya está en el proyecto, para reducir boilerplate.

---

### Auditoría del @RestControllerAdvice existente

#### Gaps a corregir en ambos servicios

**Gap 1 — Handler genérico para Exception.class ausente o sin log**
Todo `@RestControllerAdvice` debe tener un handler de último recurso para
`Exception.class` que registre el stack trace y devuelva 500 sin exponer internos.

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Error inesperado | método={} uri={} | excepción={}",
              request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(500).body(
        ErrorResponse.of(500, "Internal Server Error", "Ha ocurrido un error inesperado")
    );
}
```

**Gap 2 — Handlers de negocio sin log ERROR**
Los handlers para `DocumentoInvalidoException`, `CuentaDuplicadaException`,
`ClienteNotFoundException`, etc., probablemente devuelven la respuesta correcta
pero no registran nada. Agregar log antes del return:

```java
@ExceptionHandler(DocumentoInvalidoException.class)
public ResponseEntity<ErrorResponse> handleDocumentoInvalido(
        DocumentoInvalidoException ex, HttpServletRequest request) {
    log.error("Documento inválido | método={} uri={} | motivo={}",
              request.getMethod(), request.getRequestURI(), ex.getMessage());
    return ResponseEntity.badRequest().body(
        ErrorResponse.of(400, "Bad Request", ex.getMessage())
    );
}
```

**Gap 3 — MethodArgumentNotValidException sin log**
Las validaciones de Bean Validation (`@NotNull`, `@Size`, etc.) lanzan
`MethodArgumentNotValidException`. Agregar log WARN (no es error de sistema,
es input inválido del cliente):

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest request) {
    String campos = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.warn("Validación fallida | uri={} | campos={}",
             request.getRequestURI(), campos);
    return ResponseEntity.badRequest().body(
        ErrorResponse.of(400, "Bad Request", campos)
    );
}
```

---

### Logging en casos de uso

#### cliente-service — ClienteUseCaseImpl

Puntos de log a agregar:

| Momento                              | Nivel | Mensaje ejemplo                                                         |
|--------------------------------------|-------|-------------------------------------------------------------------------|
| Inicio de creación de cliente        | INFO  | `"Creando cliente | tipoDocumento={} | numeroDocumento={}"`             |
| Validación de documento fallida      | ERROR | `"Documento inválido | tipo={} | numero={} | motivo={}"`                |
| Cliente duplicado detectado          | ERROR | `"Cliente duplicado | tipoDocumento={} | numeroDocumento={}"`           |
| Cliente creado exitosamente          | INFO  | `"Cliente creado | id={} | tipoDocumento={}"`                           |
| Consulta de cliente no encontrado    | WARN  | `"Cliente no encontrado | id={}"`                                       |

#### cuenta-service — CuentaUseCaseImpl

Puntos de log a agregar:

| Momento                              | Nivel | Mensaje ejemplo                                                         |
|--------------------------------------|-------|-------------------------------------------------------------------------|
| Inicio de creación de cuenta         | INFO  | `"Creando cuenta | clienteId={} | tipoCuenta={}"`                       |
| Límite de cuentas violado            | ERROR | `"Límite de cuentas excedido | clienteId={} | tipoCuenta={}"`           |
| Cuenta creada exitosamente           | INFO  | `"Cuenta creada | id={} | clienteId={} | tipoCuenta={}"`                |
| Consulta de cuenta no encontrada     | WARN  | `"Cuenta no encontrada | id={}"`                                        |
| Movimiento registrado                | INFO  | `"Movimiento registrado | cuentaId={} | tipo={} | monto={}"`            |

---

### Configuración de niveles por entorno

En `application.properties` de cada servicio:

```properties
# Nivel de log configurable por variable de entorno (default: INFO)
logging.level.root=${LOG_LEVEL:INFO}
logging.level.com.tuempresa=${LOG_LEVEL_APP:INFO}

# En producción: LOG_LEVEL=ERROR, LOG_LEVEL_APP=INFO
# En local/debug: LOG_LEVEL=INFO, LOG_LEVEL_APP=DEBUG
```

Agregar al `.env.example` (SPEC-006):
```dotenv
# LOG_LEVEL=INFO
# LOG_LEVEL_APP=INFO
```

### Formato de log recomendado

Logback default de Spring Boot es suficiente para esta etapa. No se requiere
configurar `logback-spring.xml` salvo que se necesite salida JSON para un
agregador de logs (fuera de alcance de este spec).

Formato resultante en consola:
```
2026-04-29 10:23:45.123  INFO 12345 --- [nio-8081-exec-1] c.e.cliente.usecase.ClienteUseCaseImpl   : Cliente creado | id=abc-123 | tipoDocumento=CC
2026-04-29 10:23:46.456 ERROR 12345 --- [nio-8081-exec-2] c.e.cliente.advice.GlobalExceptionHandler : Documento inválido | método=POST uri=/api/v1/clientes | motivo=CC debe tener entre 8 y 10 dígitos
```

---

## 3. LISTA DE TAREAS

### cliente-service

#### @RestControllerAdvice
- [x] Auditar handlers existentes e identificar cuáles no tienen `log.error()`
- [x] Agregar log ERROR en handler de `DocumentoInvalidoException` con método, URI y motivo
- [x] Agregar log ERROR en handler de `ClienteNotFoundException` (o equivalente) con id buscado
- [x] Agregar o corregir handler genérico para `Exception.class` con log ERROR + stack trace
      y respuesta 500 sin detalles internos
- [x] Agregar handler para `MethodArgumentNotValidException` con log WARN si no existe

#### ClienteUseCaseImpl
- [x] Agregar `@Slf4j` (o declaración manual del logger) a la clase
- [x] Log INFO al inicio de `crearCliente()` con tipoDocumento y numeroDocumento
- [x] Log ERROR antes de lanzar `DocumentoInvalidoException` con motivo
- [x] Log ERROR antes de lanzar excepción de duplicado
- [x] Log INFO tras persistir el cliente con el id generado
- [x] Log WARN en `buscarClientePorId()` cuando el cliente no existe

#### Configuración
- [x] Agregar `logging.level.root` y `logging.level.com.tuempresa` a `application.properties`
      usando variables de entorno con default INFO
- [x] Agregar `LOG_LEVEL` y `LOG_LEVEL_APP` a `.env.example`

### cuenta-service

#### @RestControllerAdvice
- [x] Auditar handlers existentes e identificar cuáles no tienen `log.error()`
- [x] Agregar log ERROR en handler de `CuentaDuplicadaException` con clienteId y tipoCuenta
- [x] Agregar log ERROR en handler de cuenta/cliente no encontrado con id buscado
- [x] Agregar o corregir handler genérico para `Exception.class` con log ERROR + stack trace
      y respuesta 500 sin detalles internos
- [x] Agregar handler para `MethodArgumentNotValidException` con log WARN si no existe

#### CuentaUseCaseImpl
- [x] Agregar `@Slf4j` (o declaración manual del logger) a la clase
- [x] Log INFO al inicio de `crearCuenta()` con clienteId y tipoCuenta
- [x] Log ERROR antes de lanzar `CuentaDuplicadaException` con clienteId y tipoCuenta
- [x] Log INFO tras persistir la cuenta con id generado
- [x] Log INFO al registrar un movimiento con cuentaId, tipo y monto
- [x] Log WARN cuando una cuenta consultada no existe

#### Configuración
- [x] Agregar `logging.level.root` y `logging.level.com.tuempresa` a `application.properties`
      usando variables de entorno con default INFO
- [x] Agregar `LOG_LEVEL` y `LOG_LEVEL_APP` a `.env.example`