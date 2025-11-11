# ecommerce-simulator

Monorepo de servicios de ecommerce usando Spring Boot.

## Estructura del Proyecto

Este proyecto está organizado como un monorepo con los siguientes módulos:

- **libs/**: Librerías compartidas (DTOs, utilidades, etc.)
- **services/catalog-service/**: Servicio de catálogo de productos
- **services/users-service/**: Servicio de gestión de usuarios

## Servicios

### Catalog Service
- Puerto: 8081
- Endpoint: `GET /products-catalog`

### Users Service
- Puerto: 8082
- Endpoint: `GET /users`

## Ejecutar los Servicios

### Desde la raíz del proyecto:

```bash
# Catalog Service
./gradlew :services:catalog-service:bootRun

# Users Service
./gradlew :services:users-service:bootRun
```

### Desde cada servicio:

```bash
# Catalog Service
cd services/catalog-service
../../gradlew bootRun

# Users Service
cd services/users-service
../../gradlew bootRun
```

## Tecnologías

- Spring Boot 3.5.7
- Java 17
- Gradle (Kotlin DSL)
