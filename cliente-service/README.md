# cliente-service

Microservicio `cliente-service` - implementación inicial para SPEC-001.

Cómo ejecutar localmente:

- Ejecutar con Gradle (requiere Java 17 y Postgres local):

```
./gradlew bootRun
```

- Ejecutar tests (incluye Testcontainers para Postgres y RabbitMQ):

```
./gradlew test
```

- Perfil `docker` usa `application-docker.yml` (si aplica).

Decisiones / supuestos:

- Se usó `UUID` como identificador de `Cliente`.
- La contraseña se recibe en claro en `POST/PUT` y se guarda como `passwordHash` con `BCrypt`.
- Los eventos se publican en intercambio `cliente.exchange` y cola `cliente.event.queue` con routing `cliente.#`.
- En caso de fallo al publicar eventos, no se revierte la transacción (fire-and-forget): se registra el error.

TODOs:
- Revisar configuración del Dockerfile y gradle wrapper para producción.
- Revisar tamaños y políticas de retry para RabbitMQ si se requiere fiabilidad mayor.
