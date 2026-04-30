# Bankary Monorepo — Sistema de Gestión Bancaria

Sistema distribuido de gestión de clientes y cuentas bancarias basado en microservicios, diseñado bajo principios de Arquitectura Hexagonal y Domain-Driven Design (DDD).

## Servicios del Sistema

| Servicio          | Puerto | Responsabilidad                                  |
|-------------------|--------|--------------------------------------------------|
| cliente-service   | 8080   | Gestión de perfiles de clientes y autenticación  |
| cuenta-service    | 8081   | Gestión de cuentas, saldos y movimientos         |

## Requisitos Previos

- **Java 17+** (Amazon Corretto o OpenJDK)
- **Maven 3.8+**
- **Docker & Docker Compose** (opcional, para levantado rápido)
- **PostgreSQL 15** (si se levanta sin Docker)
- **RabbitMQ** (para la comunicación asíncrona de eventos)

## Configuración de Entorno

El proyecto utiliza variables de entorno para su configuración. Siga estos pasos para preparar su entorno local:

```bash
# Copiar el archivo de ejemplo
cp .env.example .env

# Editar .env con sus credenciales locales si es necesario
# Por defecto está configurado para funcionar con el docker-compose incluido.
```

## Cómo Levantar el Proyecto

### Opción A: Con Docker Compose (Recomendado)
Levanta los servicios y sus bases de datos PostgreSQL de forma aislada.

```bash
docker-compose up --build
```

### Opción B: Sin Docker (Manual)
Requiere tener instalados y corriendo PostgreSQL (puerto 5432) y RabbitMQ (puerto 5672).

```bash
# Terminal 1: Cliente Service
cd cliente-service
mvn spring-boot:run

# Terminal 2: Cuenta Service
cd cuenta-service
mvn spring-boot:run
```

## Cómo Ejecutar los Tests

El proyecto exige una cobertura mínima del 80% en la lógica de negocio.

```bash
# Ejecutar tests de todos los servicios
mvn clean verify -DskipTests=false

# El reporte de cobertura individual se genera en:
# [servicio]/target/site/jacoco/index.html
```

## Estructura del Repositorio

```text
/
├── .github/                # Workflows de CI/CD y especificaciones ASDD
├── cliente-service/        # Microservicio de gestión de clientes (Hexagonal)
├── cuenta-service/         # Microservicio de gestión de cuentas (Hexagonal)
├── sql/                    # Scripts de inicialización de base de datos
├── docker-compose.yml      # Orquestación de contenedores
├── .env.example            # Plantilla de variables de entorno
└── ARCHITECTURE.md         # Documentación técnica detallada
```

## CI/CD
El proyecto utiliza pipelines independientes para garantizar que cada microservicio sea validado de forma aislada ante cualquier cambio.

- **Cliente Service:** ![CI — cliente-service](https://github.com/tutoOmar/bankary-backend/actions/workflows/cliente-service.yml/badge.svg)
- **Cuenta Service:** ![CI — cuenta-service](https://github.com/tutoOmar/bankary-backend/actions/workflows/cuenta-service.yml/badge.svg)

## Documentación Adicional
- [ARCHITECTURE.md](ARCHITECTURE.md): Decisiones de diseño y flujo técnico.
- [API_EXAMPLES.md](API_EXAMPLES.md): Guía de Payloads y respuestas de los endpoints.
