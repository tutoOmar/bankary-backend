---
id: SPEC-006
status: DRAFT
feature: variables-de-entorn
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: [SPEC-001, SPEC-002, SPEC-005]
---

# Spec: variables-de-entorno — Protección de configuración sensible

## 1. REQUERIMIENTOS

### Descripción
Externalizar toda configuración sensible de cliente-service y cuenta-service
(credenciales de base de datos, URLs, puertos, secretos) a variables de entorno,
eliminando valores hardcodeados en `application.properties` o `application.yml`.
El objetivo es que ningún secreto quede expuesto en el repositorio y que el
proyecto sea desplegable en cualquier entorno (local, CI, producción) sin modificar
código fuente.

### Requerimiento de Negocio
Un sistema financiero no puede tener credenciales de base de datos o configuración
sensible en texto plano dentro del repositorio. Esta práctica es requisito mínimo
de seguridad y es evaluada en revisiones técnicas y auditorías.

### Historias de Usuario

#### HU-01: Externalizar configuración de base de datos — ambos servicios
Como: Desarrollador o DevOps
Quiero: que las credenciales y URL de la base de datos vengan de variables de entorno
Para: no exponer secretos en el repositorio y poder cambiar entornos sin tocar código
Prioridad: Alta
Estimación: S
Dependencias: SPEC-001 HU-01, SPEC-002 HU-01
Capa: Configuración — cliente-service y cuenta-service

#### Criterios de Aceptación — HU-01

**Happy Path**
```
CRITERIO-1.1: Servicio arranca con variables de entorno definidas
  Dado que: las variables DB_URL, DB_USERNAME, DB_PASSWORD están definidas en el entorno
  Cuando: se levanta el servicio (docker-compose up o mvn spring-boot:run)
  Entonces: el servicio conecta a la base de datos y responde en /actuator/health con UP
```

**Error Path**
```
CRITERIO-1.2: Servicio falla explícitamente si falta variable obligatoria
  Dado que: la variable DB_URL NO está definida en el entorno
  Cuando: se intenta levantar el servicio
  Entonces: el servicio falla al arrancar con mensaje claro indicando la variable faltante
             (comportamiento por defecto de Spring con placeholders sin valor por defecto)
```

#### HU-02: Archivo `.env` para desarrollo local con `.env.example` en repo
Como: Desarrollador nuevo en el proyecto
Quiero: un archivo `.env.example` con todas las variables requeridas documentadas
Para: poder configurar mi entorno local rápidamente sin adivinar qué variables existen
Prioridad: Alta
Estimación: XS
Dependencias: HU-01
Capa: Configuración — raíz del repositorio

#### HU-03: Externalizar configuración del servidor y Eureka
Como: Desarrollador o DevOps
Quiero: que el puerto del servidor, el nombre de la aplicación y la URL de Eureka
        también vengan de variables de entorno
Para: poder desplegar múltiples instancias o cambiar puertos sin modificar archivos
Prioridad: Media
Estimación: XS
Dependencias: HU-01
Capa: Configuración — cliente-service y cuenta-service

---

## 2. DISEÑO

### Variables de entorno requeridas por servicio

#### cliente-service
| Variable              | Descripción                          | Ejemplo de valor                                      |
|-----------------------|--------------------------------------|-------------------------------------------------------|
| `DB_URL`              | JDBC URL de la base de datos         | `jdbc:postgresql://localhost:5432/clientedb`          |
| `DB_USERNAME`         | Usuario de base de datos             | `postgres`                                            |
| `DB_PASSWORD`         | Contraseña de base de datos          | `secret`                                              |
| `SERVER_PORT`         | Puerto HTTP del servicio             | `8081`                                                |
| `EUREKA_SERVER_URL`   | URL del servidor de descubrimiento   | `http://localhost:8761/eureka`                        |
| `APP_NAME`            | Nombre de la aplicación en Eureka    | `cliente-service`                                     |

#### cuenta-service
| Variable              | Descripción                          | Ejemplo de valor                                      |
|-----------------------|--------------------------------------|-------------------------------------------------------|
| `DB_URL`              | JDBC URL de la base de datos         | `jdbc:postgresql://localhost:5432/cuentadb`           |
| `DB_USERNAME`         | Usuario de base de datos             | `postgres`                                            |
| `DB_PASSWORD`         | Contraseña de base de datos          | `secret`                                              |
| `SERVER_PORT`         | Puerto HTTP del servicio             | `8082`                                                |
| `EUREKA_SERVER_URL`   | URL del servidor de descubrimiento   | `http://localhost:8761/eureka`                        |
| `APP_NAME`            | Nombre de la aplicación en Eureka    | `cuenta-service`                                      |
| `CLIENTE_SERVICE_URL` | URL base de cliente-service          | `http://localhost:8081`                               |

### Cambios en application.properties / application.yml

Reemplazar valores hardcodeados por placeholders de Spring:

```properties
# Antes (❌ hardcodeado)
spring.datasource.url=jdbc:postgresql://localhost:5432/clientedb
spring.datasource.username=postgres
spring.datasource.password=secret

# Después (✅ externalizado)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.application.name=${APP_NAME:cliente-service}
server.port=${SERVER_PORT:8081}
eureka.client.service-url.defaultZone=${EUREKA_SERVER_URL:http://localhost:8761/eureka}
```

> Nota: Variables de infraestructura no-sensibles como `SERVER_PORT` y `APP_NAME`
> pueden tener valor por defecto (`${VAR:default}`) para facilitar el desarrollo local
> sin `.env`. Las credenciales (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) NO deben
> tener valor por defecto — deben fallar explícitamente si no están definidas.

### Archivo `.env.example`

Crear en la raíz del repositorio un archivo `.env.example` (nunca `.env` real):

```dotenv
# ============================
# cliente-service
# ============================
# DB_URL=jdbc:postgresql://localhost:5432/clientedb
# DB_USERNAME=postgres
# DB_PASSWORD=changeme
# SERVER_PORT=8081
# EUREKA_SERVER_URL=http://localhost:8761/eureka
# APP_NAME=cliente-service

# ============================
# cuenta-service
# ============================
# DB_URL=jdbc:postgresql://localhost:5432/cuentadb
# DB_USERNAME=postgres
# DB_PASSWORD=changeme
# SERVER_PORT=8082
# EUREKA_SERVER_URL=http://localhost:8761/eureka
# APP_NAME=cuenta-service
# CLIENTE_SERVICE_URL=http://localhost:8081
```

### Cambios en docker-compose.yml

Si el proyecto usa docker-compose, actualizar para leer variables desde `.env`:

```yaml
services:
  cliente-service:
    environment:
      DB_URL: ${CLIENTE_DB_URL}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      SERVER_PORT: ${CLIENTE_SERVER_PORT:-8081}
      EUREKA_SERVER_URL: ${EUREKA_SERVER_URL}
      APP_NAME: cliente-service

  cuenta-service:
    environment:
      DB_URL: ${CUENTA_DB_URL}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      SERVER_PORT: ${CUENTA_SERVER_PORT:-8082}
      EUREKA_SERVER_URL: ${EUREKA_SERVER_URL}
      APP_NAME: cuenta-service
      CLIENTE_SERVICE_URL: ${CLIENTE_SERVICE_URL}
```

### .gitignore

Verificar y asegurar que `.gitignore` incluya:

```gitignore
# Variables de entorno locales — NUNCA commitear
.env
*.env
!.env.example
```

---

## 3. LISTA DE TAREAS

### Ambos servicios
- [ ] Revisar `application.properties` / `application.yml` de cada servicio e identificar
      todos los valores hardcodeados (URL, credenciales, puertos, nombres)
- [ ] Reemplazar credenciales de base de datos por `${DB_URL}`, `${DB_USERNAME}`,
      `${DB_PASSWORD}` sin valor por defecto
- [ ] Reemplazar puerto, nombre de app y URL de Eureka por variables con default
      (`${SERVER_PORT:808X}`, `${APP_NAME:...}`, `${EUREKA_SERVER_URL:...}`)
- [ ] En cuenta-service: externalizar URL de cliente-service a `${CLIENTE_SERVICE_URL}`

### Repositorio
- [ ] Crear `.env.example` en la raíz con todas las variables documentadas y comentadas
- [ ] Verificar que `.gitignore` incluye `.env` y excluye `.env.example`
- [ ] Si existe docker-compose.yml: actualizar `environment` de cada servicio
      para leer desde variables de entorno del host / archivo `.env`

### Verificación
- [ ] Levantar ambos servicios localmente con variables definidas y confirmar
      que `/actuator/health` responde `UP`
- [ ] Confirmar que sin `DB_PASSWORD` el servicio falla al arrancar con error claro
- [ ] Hacer `git log --all -- '*.env'` y confirmar que ningún `.env` real
      fue commiteado en el historial