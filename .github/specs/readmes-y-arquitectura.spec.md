---
id: SPEC-009
status: DRAF
feature: readmes-y-arquitectura
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002, SPEC-005, SPEC-006, SPEC-007, SPEC-008]
---

# Spec: readmes-y-arquitectura — Documentación del proyecto

## 1. REQUERIMIENTOS

### Descripción
Escribir tres documentos de referencia para el monorepo: un `README.md` raíz que
sirva como punto de entrada al proyecto completo, un `ARCHITECTURE.md` que explique
las decisiones técnicas y la estructura hexagonal, y un `README.md` dentro de cada
servicio que describa cómo levantarlo y probarlo de forma independiente. El objetivo
es que un desarrollador nuevo pueda entender el proyecto y levantarlo localmente
leyendo solo estos archivos.

### Requerimiento de Negocio
La documentación es parte de la entrega técnica. En una revisión de código o
entrevista, un repositorio sin README comunica descuido. Con estos tres archivos
el proyecto se defiende solo antes de abrir cualquier clase Java.

### Historias de Usuario

#### HU-01: README raíz del monorepo
Como: Desarrollador nuevo o evaluador técnico
Quiero: un README en la raíz del repositorio que explique qué es el proyecto,
        cómo está organizado y cómo levantarlo completo
Para: entender el sistema en menos de 5 minutos sin leer código fuente
Prioridad: Alta
Estimación: S
Dependencias: SPEC-006 HU-02 (variables de entorno documentadas)
Capa: Documentación — raíz del repositorio

#### HU-02: ARCHITECTURE.md
Como: Desarrollador o Tech Lead que evalúa el proyecto
Quiero: un documento que explique la arquitectura hexagonal, las decisiones de diseño
        y cómo se comunican los servicios
Para: entender el por qué detrás de la estructura sin necesidad de preguntar al autor
Prioridad: Alta
Estimación: M
Dependencias: HU-01
Capa: Documentación — raíz del repositorio

#### HU-03: README por servicio
Como: Desarrollador que trabaja en un servicio específico
Quiero: un README dentro de `cliente-service/` y otro dentro de `cuenta-service/`
        que expliquen cómo levantar, probar y entender ese servicio de forma aislada
Para: poder trabajar en un servicio sin necesidad de leer el README raíz completo
Prioridad: Media
Estimación: S
Dependencias: HU-01
Capa: Documentación — cliente-service/ y cuenta-service/

---

## 2. DISEÑO

### Estructura de archivos a crear

```
/
├── README.md               ← HU-01: entrada al monorepo
├── ARCHITECTURE.md         ← HU-02: decisiones técnicas y diseño
├── cliente-service/
│   └── README.md           ← HU-03: guía del servicio
└── cuenta-service/
    └── README.md           ← HU-03: guía del servicio
```

---

### README.md raíz — Contenido requerido

**Secciones obligatorias (en este orden):**

1. **Título y descripción breve** — Qué es el sistema, qué problema resuelve,
   en una o dos oraciones. Sin párrafos de contexto innecesarios.

2. **Servicios del sistema** — Tabla con nombre del servicio, puerto por defecto
   y responsabilidad principal.
   ```
   | Servicio          | Puerto | Responsabilidad                        |
   |-------------------|--------|----------------------------------------|
   | cliente-service   | 8081   | Gestión de clientes y documentos       |
   | cuenta-service    | 8082   | Cuentas, saldos y movimientos          |
   ```

3. **Requisitos previos** — Lista mínima y concreta:
   Java 17+, Maven 3.8+, Docker (si aplica), PostgreSQL (versión).

4. **Configuración de entorno** — Referencia a `.env.example` y los pasos
   para crear el `.env` local. No repetir todas las variables, solo el comando:
   ```bash
   cp .env.example .env
   # Editar .env con tus valores locales
   ```

5. **Cómo levantar el proyecto** — Comandos exactos, sin ambigüedad:
   ```bash
   # Con Docker Compose
   docker-compose up --build

   # Sin Docker (cada servicio por separado)
   cd cliente-service && mvn spring-boot:run
   cd cuenta-service  && mvn spring-boot:run
   ```

6. **Cómo ejecutar los tests** — Un comando por servicio y uno global si aplica:
   ```bash
   cd cliente-service && mvn verify
   cd cuenta-service  && mvn verify
   # El reporte de cobertura queda en target/site/jacoco/index.html
   ```

7. **Estructura del repositorio** — Árbol de carpetas de un nivel de profundidad
   con una línea de descripción por carpeta.

8. **CI/CD** — Una línea indicando que el pipeline corre en GitHub Actions
   en cada push a `main`. Referencia al SPEC-010 cuando esté implementado.

9. **Documentación adicional** — Link a `ARCHITECTURE.md`.

---

### ARCHITECTURE.md — Contenido requerido

**Secciones obligatorias (en este orden):**

1. **Visión general** — Diagrama ASCII o descripción textual de cómo interactúan
   los servicios entre sí y con la base de datos.
   ```
   [Cliente HTTP]
        │
        ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ cliente-service │     │ cuenta-service  │
   │   :8081         │◄────│   :8082         │
   └────────┬────────┘     └────────┬────────┘
            │                       │
       [PostgreSQL]            [PostgreSQL]
        clientedb               cuentadb
   ```

2. **Arquitectura hexagonal por servicio** — Explicar las tres capas y qué
   vive en cada una. No copiar código, describir la responsabilidad:

   - `domain/` — Entidades de negocio, casos de uso, puertos (interfaces),
     validadores. Sin dependencias de Spring ni de infraestructura.
   - `application/` (o `usecase/`) — Implementaciones de casos de uso.
     Orquesta domain y llama a los puertos out.
   - `infrastructure/` — Adaptadores: controladores REST, repositorios JPA,
     clientes HTTP. Todo lo que depende de frameworks externos.

3. **Decisiones de diseño** — Sección corta pero honesta. Ejemplos:

   - *¿Por qué arquitectura hexagonal?* — Separación de lógica de negocio
     de infraestructura; facilita tests unitarios sin levantar contexto Spring.
   - *¿Por qué PostgreSQL y no H2?* — Paridad con producción desde el inicio;
     evita sorpresas con tipos de datos y constraints.
   - *¿Por qué proyectos Maven independientes y no multi-módulo?* — Cada servicio
     es desplegable de forma autónoma; reduce acoplamiento entre builds.

4. **Modelo de datos** — Descripción de las tablas principales por servicio.
   No hace falta DDL completo, solo nombre de tabla, columnas clave y relaciones.

5. **Reglas de negocio implementadas** — Lista de las reglas más importantes
   con referencia al spec que las define:
   - Validación de documentos colombianos (SPEC-005)
   - Límite de 1 cuenta activa por tipo (SPEC-005)
   - Variables de entorno para configuración sensible (SPEC-006)
   - Cobertura mínima de 80% en domain/ (SPEC-008)

6. **Flujo de una petición** — Trazar una petición `POST /api/v1/clientes`
   desde el controller hasta la base de datos y de vuelta, nombrando cada clase
   que participa. Esto demuestra que el autor entiende su propio código.

---

### README por servicio — Contenido requerido

Mismo esquema para `cliente-service/README.md` y `cuenta-service/README.md`.
Más corto que el raíz — enfocado en ese servicio únicamente.

**Secciones obligatorias:**

1. **Descripción del servicio** — Una oración. Qué hace y qué no hace.

2. **Variables de entorno requeridas** — Tabla completa de las variables
   específicas de este servicio (copiar de SPEC-006, sección del servicio).

3. **Cómo levantar en local**
   ```bash
   # Desde la carpeta del servicio
   mvn spring-boot:run
   # O con el jar
   mvn package -DskipTests
   java -jar target/cliente-service-*.jar
   ```

4. **Endpoints principales** — Tabla con método, ruta y descripción de cada endpoint:
   ```
   | Método | Ruta                      | Descripción                  |
   |--------|---------------------------|------------------------------|
   | POST   | /api/v1/clientes          | Crear cliente                |
   | GET    | /api/v1/clientes/{id}     | Consultar cliente por ID     |
   | GET    | /actuator/health          | Estado del servicio          |
   ```

5. **Cómo ejecutar los tests y ver cobertura**
   ```bash
   mvn verify
   open target/site/jacoco/index.html
   ```

6. **Estructura de paquetes** — Árbol de `src/main/java` con una línea por paquete.

---

## 3. LISTA DE TAREAS

### README.md raíz
- [ ] Revisar el proyecto completo y anotar: servicios, puertos, tecnologías usadas
- [ ] Escribir sección de título y descripción (máximo 3 oraciones)
- [ ] Escribir tabla de servicios con puerto y responsabilidad
- [ ] Escribir sección de requisitos previos con versiones concretas
- [ ] Escribir sección de configuración de entorno referenciando `.env.example`
- [ ] Escribir sección "Cómo levantar" con comandos exactos probados
- [ ] Escribir sección "Cómo ejecutar tests" con ruta al reporte JaCoCo
- [ ] Escribir árbol de estructura del repositorio
- [ ] Agregar línea de CI/CD referenciando GitHub Actions

### ARCHITECTURE.md
- [ ] Dibujar diagrama ASCII de interacción entre servicios y bases de datos
- [ ] Describir las tres capas de la arquitectura hexagonal con ejemplos de clases reales
- [ ] Escribir sección de decisiones de diseño (mínimo 3 decisiones justificadas)
- [ ] Describir modelo de datos de cliente-service (tablas persona, cliente)
- [ ] Describir modelo de datos de cuenta-service (tablas cuenta, movimiento)
- [ ] Listar reglas de negocio implementadas con referencia a spec
- [ ] Trazar flujo completo de POST /api/v1/clientes nombrando cada clase

### cliente-service/README.md
- [ ] Revisar todos los endpoints expuestos y construir la tabla
- [ ] Escribir tabla de variables de entorno del servicio
- [ ] Escribir comandos de arranque y empaquetado probados localmente
- [ ] Escribir árbol de paquetes de src/main/java con descripción por paquete

### cuenta-service/README.md
- [ ] Revisar todos los endpoints expuestos y construir la tabla
- [ ] Escribir tabla de variables de entorno del servicio
- [ ] Escribir comandos de arranque y empaquetado probados localmente
- [ ] Escribir árbol de paquetes de src/main/java con descripción por paquete