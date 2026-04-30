---
id: SPEC-010
status: IMPLEMENTED
feature: pipeline-ci-cd
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-006, SPEC-007, SPEC-008, SPEC-009]
---

# Spec: pipeline-ci-cd — GitHub Actions: build, test y cobertura

## 1. REQUERIMIENTOS

### Descripción
Crear un workflow de GitHub Actions que corra automáticamente en cada push o
pull request hacia `main`, `develop` y ramas `feature/*`. El pipeline debe
compilar ambos servicios, ejecutar sus tests unitarios e de integración contra
una instancia real de PostgreSQL, validar que la cobertura de `domain/` no baje
del 80%, y fallar de forma clara si cualquiera de esos pasos no pasa.

### Requerimiento de Negocio
Un repositorio sin CI es un repositorio donde cualquier commit puede romper el
proyecto silenciosamente. El pipeline es la red de seguridad que garantiza que
`main` siempre compila y siempre pasa los tests — condición mínima para cualquier
equipo o evaluación técnica.

### Historias de Usuario

#### HU-01: Pipeline corre en push y pull request a ramas protegidas
Como: Desarrollador
Quiero: que GitHub Actions ejecute el pipeline automáticamente en cada push
        a `main`, `develop` o `feature/*` y en cada PR hacia `main` o `develop`
Para: detectar regresiones antes de que lleguen a la rama principal
Prioridad: Alta
Estimación: S
Dependencias: SPEC-008 HU-03
Capa: CI — .github/workflows/

#### Criterios de Aceptación — HU-01

```gherkin
CRITERIO-1.1: Push a feature/* dispara el pipeline
  Dado que: un desarrollador hace push a una rama feature/nueva-funcionalidad
  Cuando: GitHub recibe el push
  Entonces: el workflow ci.yml inicia automáticamente en menos de 60 segundos

CRITERIO-1.2: PR hacia main falla si los tests no pasan
  Dado que: existe un PR de develop hacia main con un test fallido
  Cuando: GitHub evalúa el PR
  Entonces: el check del pipeline aparece en rojo y bloquea el merge

CRITERIO-1.3: Pipeline verde en main significa build + tests + cobertura ok
  Dado que: todos los pasos del pipeline pasan en main
  Cuando: el workflow termina
  Entonces: el badge del README muestra passing y el reporte de cobertura
            está disponible como artefacto descargable en la ejecución
```

#### HU-02: PostgreSQL disponible como servicio en el pipeline
Como: Pipeline de CI
Quiero: una instancia de PostgreSQL disponible durante los tests
Para: que los tests de integración corran contra una base de datos real,
      igual que en desarrollo local
Prioridad: Alta
Estimación: S
Dependencias: SPEC-006 HU-01
Capa: CI — .github/workflows/

#### HU-03: Reporte de cobertura disponible como artefacto
Como: Desarrollador o Tech Lead
Quiero: que el reporte HTML de JaCoCo quede adjunto a cada ejecución del pipeline
Para: revisar qué líneas no están cubiertas sin necesidad de clonar y correr mvn verify
Prioridad: Media
Estimación: XS
Dependencias: HU-01, SPEC-008 HU-01
Capa: CI — .github/workflows/

---

## 2. DISEÑO

### Estructura de archivos a crear

```
.github/
└── workflows/
    └── ci.yml          ← workflow único para ambos servicios
```

Un solo workflow con dos jobs paralelos — uno por servicio — que comparten
el mismo servicio de PostgreSQL y corren simultáneamente para minimizar
el tiempo total del pipeline.

---

### Workflow completo — .github/workflows/ci.yml

```yaml
name: CI

on:
  push:
    branches:
      - main
      - develop
      - 'feature/**'
  pull_request:
    branches:
      - main
      - develop

jobs:

  # ─────────────────────────────────────────
  # JOB 1: cliente-service
  # ─────────────────────────────────────────
  cliente-service:
    name: Build & Test — cliente-service
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: clientedb
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout código
        uses: actions/checkout@v4

      - name: Configurar Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build, tests y cobertura — cliente-service
        working-directory: cliente-service
        env:
          DB_URL: jdbc:postgresql://localhost:5432/clientedb
          DB_USERNAME: postgres
          DB_PASSWORD: postgres
          SERVER_PORT: 8081
          EUREKA_SERVER_URL: http://localhost:8761/eureka
          APP_NAME: cliente-service
        run: mvn verify --no-transfer-progress

      - name: Publicar reporte de cobertura — cliente-service
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-cliente-service
          path: cliente-service/target/site/jacoco/
          retention-days: 7

  # ─────────────────────────────────────────
  # JOB 2: cuenta-service
  # ─────────────────────────────────────────
  cuenta-service:
    name: Build & Test — cuenta-service
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: cuentadb
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout código
        uses: actions/checkout@v4

      - name: Configurar Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build, tests y cobertura — cuenta-service
        working-directory: cuenta-service
        env:
          DB_URL: jdbc:postgresql://localhost:5432/cuentadb
          DB_USERNAME: postgres
          DB_PASSWORD: postgres
          SERVER_PORT: 8082
          EUREKA_SERVER_URL: http://localhost:8761/eureka
          APP_NAME: cuenta-service
          CLIENTE_SERVICE_URL: http://localhost:8081
        run: mvn verify --no-transfer-progress

      - name: Publicar reporte de cobertura — cuenta-service
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-cuenta-service
          path: cuenta-service/target/site/jacoco/
          retention-days: 7
```

---

### Decisiones de diseño del pipeline

#### Dos jobs paralelos, no uno secuencial
Cada servicio corre en su propio job con su propio runner y su propio
contenedor de PostgreSQL. Esto significa que si cliente-service falla,
cuenta-service igual corre y viceversa — se ven todos los fallos de una vez,
no en cascada. Además reduce el tiempo total del pipeline a la mitad.

#### `mvn verify` como comando único
`verify` ejecuta en orden: compilación → tests unitarios → tests de integración
→ JaCoCo report → JaCoCo check (enforcement del 80%). Si cualquier paso falla,
el job falla. No se necesitan pasos separados para compile, test y verify.

#### `--no-transfer-progress`
Elimina el spam de progreso de descarga de dependencias Maven en los logs,
haciendo los logs del pipeline legibles. No afecta el resultado.

#### `cache: maven` en setup-java
Cachea el repositorio local de Maven (`~/.m2`) entre ejecuciones. En la
primera ejecución descarga todas las dependencias (~2-3 min). En ejecuciones
posteriores las reutiliza (~20-30 seg de diferencia en builds medianos).

#### `if: always()` en upload-artifact
El reporte de cobertura se sube incluso si el job falla — por ejemplo si
el enforcement del 80% falla, el reporte muestra exactamente qué líneas
faltan. Sin `if: always()` el artefacto no se subiría cuando más se necesita.

#### Variables sensibles en `env` del step, no en el yaml raíz
Las credenciales de la base de datos de CI (`postgres/postgres`) no son
secretos reales — son valores de test. Por eso van directamente en el `env`
del step y no en GitHub Secrets. Si en el futuro se necesita conectar a un
entorno real, se reemplazan por `${{ secrets.DB_PASSWORD }}`.

---

### Badge de estado para el README

Agregar en el `README.md` raíz (sección CI/CD):

```markdown
![CI](https://github.com/<org>/<repo>/actions/workflows/ci.yml/badge.svg)
```

Reemplazar `<org>/<repo>` con el path real del repositorio en GitHub.
El badge muestra verde/rojo en tiempo real según el último run en `main`.

---

### Consideraciones sobre Eureka en CI

Los servicios intentan registrarse en Eureka al arrancar. Si Eureka no está
disponible en el pipeline, el servicio puede fallar al levantar antes de
que los tests corran.

**Opción A — Deshabilitar Eureka en el perfil de test (recomendada):**
```properties
# src/test/resources/application-test.properties
eureka.client.enabled=false
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```
Y en los tests de integración anotar con `@ActiveProfiles("test")`.

**Opción B — Configurar Eureka para fallar silenciosamente:**
```properties
eureka.client.enabled=false
```
Solo en el `application.properties` principal, controlado por variable de entorno:
```yaml
env:
  EUREKA_CLIENT_ENABLED: false
```

La Opción A es más limpia porque no requiere cambios en el workflow.

---

## 3. LISTA DE TAREAS

### Configuración del workflow
- [x] Crear carpeta `.github/workflows/` en la raíz del monorepo si no existe
- [x] Crear `.github/workflows/ci.yml` con el contenido definido en este spec
- [x] Reemplazar `<org>/<repo>` en el badge con el path real del repositorio

### Compatibilidad con el pipeline
- [x] Verificar que `mvn verify` corra sin errores localmente en cliente-service
      con las variables de entorno del workflow (DB_URL, DB_USERNAME, DB_PASSWORD)
- [x] Verificar que `mvn verify` corra sin errores localmente en cuenta-service
      con las variables de entorno del workflow
- [x] Crear `src/test/resources/application-test.properties` en cada servicio
      con Eureka deshabilitado para evitar fallos de conexión en CI
- [x] Confirmar que los tests de integración usan `@ActiveProfiles("test")`
      o que Eureka está deshabilitado por variable de entorno en el workflow

### Verificación del pipeline
- [ ] Hacer push del workflow a una rama `feature/ci-pipeline`
- [ ] Confirmar que el workflow aparece en la pestaña Actions de GitHub
- [ ] Confirmar que ambos jobs (cliente-service y cuenta-service) corren en paralelo
- [ ] Confirmar que los artefactos `jacoco-cliente-service` y `jacoco-cuenta-service`
      aparecen descargables en la ejecución
- [ ] Crear un PR de `feature/ci-pipeline` hacia `develop` y confirmar que el check
      aparece como requisito antes del merge
- [ ] Agregar badge de CI al `README.md` raíz con el path correcto del repositorio

### Verificación de fallo controlado
- [ ] Introducir un test fallido intencionalmente, hacer push y confirmar que
      el job correspondiente falla en rojo con mensaje claro
- [ ] Revertir el test fallido y confirmar que el pipeline vuelve a verde