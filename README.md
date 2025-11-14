# ecommerce-simulator

Monorepo de servicios de ecommerce construido con Spring Boot.

## Estructura del Proyecto

Este proyecto se organiza como un monorepo con los siguientes módulos:

- `libs/`: Librerías compartidas (DTOs, utilidades, etc.).
- `services/catalog-service/`: Servicio de catálogo de productos.
- `services/users-service/`: Servicio de gestión de usuarios y autenticación.
- `services/orders-service/`: Servicio de carritos, pedidos, pagos y configuración de órdenes.

> 📚 **Recursos**: [Guía de Monorepo con SpringBoot](https://chatgpt.com/share/69172111-cc3c-800f-adc7-1ba54bb5026a)

## Puertos y rutas base

| Servicio        | Puerto | Base URL                       |
| --------------- | ------ | ------------------------------ |
| Catalog Service | 8081   | `http://localhost:8081/api/v1` |
| Users Service   | 8082   | `http://localhost:8082/api/v1` |
| Orders Service  | 8083   | `http://localhost:8083/api/v1` |

> Todos los servicios exponen `GET /ping` en su respectiva base URL para verificaciones de salud.  
> Para automatizar los ejemplos, define en Postman (o en tus scripts) las variables `baseCatalogUrl`, `baseUsersUrl` y `baseOrdersUrl` con los valores de la tabla.

## Documentación de APIs

### Catalog Service (`{{baseCatalogUrl}}`)

#### Productos

- **Listar productos**  
  `GET /catalog/products` con parámetros opcionales `page` (default `0`), `size` (default `10`) y `search`.

  ```bash
  curl "{{baseCatalogUrl}}/catalog/products?page=0&size=5&search=camisa"
  ```

  ```json
  {
    "content": [
      {
        "id": 1,
        "name": "Camisa básica blanca",
        "description": "Camisa de algodón",
        "price": 19.99,
        "stock": 42,
        "createdAt": "2025-01-10T09:12:00",
        "updatedAt": "2025-01-12T14:33:00"
      }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
  ```

- **Obtener producto por id**  
  `GET /catalog/products/{id}`

  ```bash
  curl "{{baseCatalogUrl}}/catalog/products/1"
  ```

- **Crear producto**  
  `POST /catalog/products`

  ```bash
  curl -X POST "{{baseCatalogUrl}}/catalog/products" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "Zapatillas running",
      "description": "Modelo 2025 con amortiguación",
      "price": 89.90,
      "stock": 25
    }'
  ```

  ```json
  {
    "id": 12,
    "name": "Zapatillas running",
    "description": "Modelo 2025 con amortiguación",
    "price": 89.9,
    "stock": 25,
    "createdAt": "2025-01-12T11:45:30",
    "updatedAt": "2025-01-12T11:45:30"
  }
  ```

- **Actualizar producto**  
  `PUT /catalog/products/{id}` con el mismo cuerpo que la creación.

- **Eliminar producto**  
  `DELETE /catalog/products/{id}` devuelve `204 No Content`.

#### Configuración del catálogo

- **Obtener configuración**  
  `GET /catalog/settings`

  ```json
  {
    "id": 1,
    "minimumStock": 3
  }
  ```

- **Actualizar configuración**  
  `PUT /catalog/settings`
  ```bash
  curl -X PUT "{{baseCatalogUrl}}/catalog/settings" \
    -H "Content-Type: application/json" \
    -d '{ "minimumStock": 10 }'
  ```

### Users Service (`{{baseUsersUrl}}`)

#### Autenticación

- **Login**  
  `POST /auth/login`
  ```bash
  curl -X POST "{{baseUsersUrl}}/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
      "email": "ana@example.com",
      "password": "supersegura"
    }'
  ```
  ```json
  {
    "userId": 5,
    "email": "ana@example.com",
    "token": "jwt-generado"
  }
  ```

#### Usuarios

- **Listar usuarios**  
  `GET /users`

- **Obtener usuario por id**  
  `GET /users/{id}`

- **Crear usuario**  
  `POST /users`

  ```bash
  curl -X POST "{{baseUsersUrl}}/users" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "Ana Pérez",
      "email": "ana@example.com",
      "telefono": "+34123456789",
      "direccion": "Calle Mayor 123, Madrid",
      "password": "supersegura"
    }'
  ```

- **Actualizar usuario**  
  `PUT /users/{id}` con el mismo cuerpo que la creación.

- **Eliminar usuario**  
  `DELETE /users/{id}` devuelve `204 No Content`.

### Orders Service (`{{baseOrdersUrl}}`)

> Las operaciones de tarjetas (`/cards`) y pagos (`/payments`) requieren:
>
> - Cabecera `x-api-key` con la API key configurada en `security.api-key`.
> - Cabecera `Authorization: Bearer <jwt>` con un token válido cuyo `userId` se pueda resolver.

#### Carritos

- **Crear carrito**  
  `POST /carts`

  ```bash
  curl -X POST "{{baseOrdersUrl}}/carts" \
    -H "Content-Type: application/json" \
    -d '{ "userId": 5 }'
  ```

- **Agregar producto**  
  `POST /carts/{cartId}/items`

  ```bash
  curl -X POST "{{baseOrdersUrl}}/carts/10/items" \
    -H "Content-Type: application/json" \
    -d '{ "productId": 1, "quantity": 2 }'
  ```

- **Eliminar producto específico**  
  `DELETE /carts/{cartId}/items/{productId}`

- **Vaciar carrito**  
  `DELETE /carts/{cartId}/items`

- **Obtener carrito**  
  `GET /carts/{cartId}`
  ```json
  {
    "id": 10,
    "userId": 5,
    "items": [
      {
        "productId": 1,
        "productName": "Camisa básica blanca",
        "quantity": 2,
        "unitPrice": 19.99,
        "subtotal": 39.98
      }
    ],
    "total": 39.98,
    "createdAt": "2025-01-12T09:00:00",
    "updatedAt": "2025-01-12T09:05:00"
  }
  ```

#### Órdenes

- **Crear orden desde carrito**  
  `POST /orders`

  ```bash
  curl -X POST "{{baseOrdersUrl}}/orders" \
    -H "Content-Type: application/json" \
    -d '{
      "cartId": 10,
      "deliveryAddress": "Calle Mayor 123, Madrid"
    }'
  ```

- **Marcar orden como pagada**  
  `POST /orders/{orderId}/pay`

- **Cancelar orden**  
  `POST /orders/{orderId}/cancel`

- **Obtener orden**  
  `GET /orders/{orderId}`

#### Pagos

- **Registrar pago**  
  `POST /payments` con cabeceras de seguridad.

  ```bash
  curl -X POST "{{baseOrdersUrl}}/payments" \
    -H "Content-Type: application/json" \
    -H "x-api-key: change-me-orders-api-key" \
    -H "Authorization: Bearer <jwt>" \
    -d '{
      "orderId": 42,
      "amount": 39.98,
      "method": "CARD",
      "cardToken": "tok_1H2J3K"
    }'
  ```

- **Marcar pago como completado**  
  `POST /payments/{paymentId}/complete`

- **Marcar pago como fallido**  
  `POST /payments/{paymentId}/fail`

- **Obtener pago**  
  `GET /payments/{paymentId}`

#### Tarjetas de crédito

- **Registrar tarjeta**  
  `POST /cards` con cabeceras de seguridad.

  ```bash
  curl -X POST "{{baseOrdersUrl}}/cards" \
    -H "Content-Type: application/json" \
    -H "x-api-key: change-me-orders-api-key" \
    -H "Authorization: Bearer <jwt>" \
    -d '{
      "cardNumber": "4242424242424242",
      "cvv": "123",
      "expiryMonth": 12,
      "expiryYear": 2030
    }'
  ```

- **Listar tarjetas del usuario autenticado**  
  `GET /cards` con cabeceras de seguridad.

#### Configuración de órdenes

- **Obtener configuración**  
  `GET /orders/settings`

  ```json
  {
    "cardRejectionProbability": 0.2,
    "paymentRetryAttempts": 3
  }
  ```

- **Actualizar configuración**  
  `PUT /orders/settings`
  ```bash
  curl -X PUT "{{baseOrdersUrl}}/orders/settings" \
    -H "Content-Type: application/json" \
    -d '{
      "cardRejectionProbability": 0.1,
      "paymentRetryAttempts": 5
    }'
  ```

## Ejecutar el Proyecto

### Prerrequisitos

- **Java 17**: JDK 17 o superior
- **Gradle**: El proyecto incluye Gradle Wrapper (`./gradlew`), no es necesario instalar Gradle manualmente

Verifica que Java esté instalado:

```bash
java -version
```

### Ejecutar los Servicios Localmente

#### Opción 1: Desde la raíz del proyecto

```bash
# Ejecutar un servicio específico
./gradlew :services:catalog-service:bootRun
./gradlew :services:users-service:bootRun
./gradlew :services:orders-service:bootRun

# Ejecutar todos los servicios (en terminales separadas)
./gradlew :services:catalog-service:bootRun &
./gradlew :services:users-service:bootRun &
./gradlew :services:orders-service:bootRun &
```

#### Opción 2: Desde cada directorio de servicio

```bash
# Catalog Service
cd services/catalog-service
../../gradlew bootRun

# Users Service (en otra terminal)
cd services/users-service
../../gradlew bootRun

# Orders Service (en otra terminal)
cd services/orders-service
../../gradlew bootRun
```

#### Verificar que los servicios están funcionando

Una vez que los servicios estén en ejecución, verifica que respondan:

```bash
# Health check de cada servicio
curl http://localhost:8081/api/v1/ping  # Catalog Service
curl http://localhost:8082/api/v1/ping  # Users Service
curl http://localhost:8083/api/v1/ping  # Orders Service
```

Deberías recibir `pong` como respuesta de cada servicio.

## Ejecutar con Docker y Docker Compose

### Prerrequisitos

- **Docker**: Versión 20.10 o superior
- **Docker Compose**: Versión 1.29 o superior (o Docker Compose V2)

Verifica que Docker esté instalado y funcionando:

```bash
docker --version
docker compose version
```

### Construcción de los Servicios

Antes de ejecutar con Docker Compose, es necesario construir los JARs de cada servicio:

```bash
# Construir todos los servicios
./gradlew build

# O construir cada servicio individualmente
./gradlew :services:catalog-service:build
./gradlew :services:users-service:build
./gradlew :services:orders-service:build
```

> **Nota**: Los Dockerfiles esperan que los JARs estén en `build/libs/` de cada servicio. Asegúrate de ejecutar `build` antes de construir las imágenes Docker.

### Ejecutar con Docker Compose

Desde la raíz del proyecto, ejecuta:

```bash
# Construir y levantar todos los servicios
docker compose up --build

# Ejecutar en segundo plano (detached mode)
docker compose up --build -d

# Ver logs de todos los servicios
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f catalog
docker compose logs -f users
docker compose logs -f orders
```

### Comandos Útiles de Docker Compose

```bash
# Detener todos los servicios
docker compose down

# Detener y eliminar volúmenes
docker compose down -v

# Reconstruir un servicio específico
docker compose up --build catalog

# Ver el estado de los servicios
docker compose ps

# Ejecutar un comando en un contenedor
docker compose exec catalog sh
docker compose exec users sh
docker compose exec orders sh
```

### Construir Imágenes Docker Individualmente

Si prefieres construir y ejecutar cada servicio por separado:

```bash
# Catalog Service
cd services/catalog-service
docker build -t catalog-service:latest .
docker run -p 8081:8081 -e SPRING_PROFILES_ACTIVE=local catalog-service:latest

# Users Service
cd services/users-service
docker build -t users-service:latest .
docker run -p 8082:8082 -e SPRING_PROFILES_ACTIVE=local users-service:latest

# Orders Service
cd services/orders-service
docker build -t orders-service:latest .
docker run -p 8083:8083 -e SPRING_PROFILES_ACTIVE=local orders-service:latest
```


### Configuración de Docker Compose

El archivo `docker-compose.yml` configura tres servicios:

- **catalog**: Puerto 8081, contenedor `catalog-ms`
- **users**: Puerto 8082, contenedor `users-ms`
- **orders**: Puerto 8083, contenedor `orders-ms`

Cada servicio:
- Se construye desde su respectivo directorio (`./services/[service-name]`)
- Expone su puerto correspondiente
- Usa el perfil Spring `local` por defecto
- Utiliza H2 Database en memoria (configurado en `application.yaml`)

### Troubleshooting

#### Los servicios no responden después de iniciar

1. Verifica que los puertos no estén en uso:
   ```bash
   lsof -i :8081
   lsof -i :8082
   lsof -i :8083
   ```

2. Revisa los logs del contenedor:
   ```bash
   docker compose logs catalog
   docker compose logs users
   docker compose logs orders
   ```

3. Verifica que los JARs se hayan construido correctamente:
   ```bash
   ls -la services/catalog-service/build/libs/
   ls -la services/users-service/build/libs/
   ls -la services/orders-service/build/libs/
   ```

#### Error: "Cannot connect to Docker daemon"

Asegúrate de que Docker Desktop (o Docker daemon) esté ejecutándose.

#### Limpiar y Reconstruir Todo

Si necesitas empezar desde cero:

```bash
# Detener y eliminar contenedores, redes y volúmenes
docker compose down -v

# Limpiar builds de Gradle
./gradlew clean

# Reconstruir todo
./gradlew build
docker compose up --build
```

## Tecnologías

### Stack Principal

- **Spring Boot 3.5.7**: Framework principal para el desarrollo de microservicios
  - [Working with SpringBoot](https://chatgpt.com/share/6917208d-ffc0-800f-8a47-f576a731eb32)
- **Java 17**: Lenguaje de programación
- **Gradle (Kotlin DSL)**: Herramienta de construcción y gestión de dependencias
- **H2 Database**: Base de datos en memoria para desarrollo y pruebas
  - [Guía de uso de H2 como BD](https://chatgpt.com/share/6917212c-7fcc-800f-a2b5-4cd5114e9785)
- **Spring Data JPA**: Abstracción para acceso a datos
- **Spring Security**: Framework de seguridad (usado para hashing de contraseñas)
- **JWT (JSON Web Tokens)**: Autenticación y autorización mediante tokens
  - `jjwt-api:0.11.5`
  - `jjwt-impl:0.11.5`
  - `jjwt-jackson:0.11.5`

### Testing

- **JUnit 5**: Framework de testing unitario
- **Mockito**: Framework de mocking para tests aislados
- **AssertJ**: Librería de aserciones fluida y legible
- **JaCoCo**: Herramienta de análisis de cobertura de código
- **Spring Boot Test**: Utilidades de testing de Spring Boot

### Contenedores y Despliegue

- **Docker**: Contenedorización de servicios
- **Docker Compose**: Orquestación de múltiples contenedores
- **Google Cloud Build & Cloud Run**: Despliegue en la nube
  - [Guía de despliegue con Cloud Build y Cloud Run](https://chatgpt.com/share/6917204b-8188-800f-9214-633665f761cd)

## Arquitectura

Este proyecto implementa **Arquitectura Hexagonal (Ports and Adapters)**, también conocida como Clean Architecture, que separa la lógica de negocio de los detalles técnicos de implementación.

### Estructura de Capas

Cada microservicio sigue la siguiente estructura:

```
service-name/
├── domain/                          # Capa de Dominio (Núcleo)
│   ├── model/                       # Modelos de dominio puros (sin dependencias externas)
│   ├── port/
│   │   ├── in/                      # Puertos de entrada (casos de uso/interfaces)
│   │   └── out/                     # Puertos de salida (repositorios/interfaces)
│   └── exception/                  # Excepciones de dominio
│
├── application/                     # Capa de Aplicación
│   └── service/                     # Implementación de casos de uso
│
└── infrastructure/                  # Capa de Infraestructura
    ├── persistence/                 # Adaptadores de persistencia
    │   ├── entity/                  # Entidades JPA
    │   ├── repository/              # Repositorios JPA
    │   ├── mapper/                   # Mappers dominio ↔ entidad
    │   └── adapter/                  # Adaptadores que implementan puertos de salida
    │
    └── web/                         # Adaptadores de entrada (REST)
        ├── controller/              # Controladores REST
        ├── dto/                     # DTOs de entrada/salida
        ├── mapper/                  # Mappers DTO ↔ dominio
        └── filter/                  # Filtros HTTP (logging, seguridad, etc.)
```

### Principios Aplicados

1. **Separación de Responsabilidades**
   - **Dominio**: Contiene la lógica de negocio pura, sin dependencias de frameworks
   - **Aplicación**: Orquesta los casos de uso y coordina entre puertos
   - **Infraestructura**: Implementa los detalles técnicos (JPA, REST, etc.)

2. **Inversión de Dependencias**
   - El dominio define interfaces (puertos)
   - La infraestructura implementa esas interfaces (adaptadores)
   - La aplicación depende de interfaces, no de implementaciones concretas

3. **Independencia del Framework**
   - Los modelos de dominio no tienen anotaciones JPA
   - La lógica de negocio no depende directamente de Spring
   - Facilita el cambio de frameworks sin afectar el dominio

4. **Testabilidad**
   - Fácil crear mocks de los puertos
   - El dominio se puede probar sin infraestructura
   - Tests unitarios aislados de frameworks

### Flujo de Datos

```
HTTP Request
    ↓
Controller (Infrastructure/Web)
    ↓
DTO → WebMapper → Domain Model
    ↓
UseCase Interface (Domain/Port) → Service Implementation (Application)
    ↓
Repository Port (Domain/Port) → Repository Adapter (Infrastructure)
    ↓
JPA Repository → Entity (JPA)
    ↓
Database
```

### Ventajas de esta Arquitectura

- **Mantenibilidad**: Código organizado y fácil de entender
- **Escalabilidad**: Fácil agregar nuevos adaptadores (nuevas APIs, bases de datos, etc.)
- **Testabilidad**: Cada capa se puede probar independientemente
- **Flexibilidad**: Cambiar implementaciones sin afectar el dominio

## Testing

El proyecto implementa un enfoque completo de testing con pruebas unitarias y análisis de cobertura de código.

> 📚 **Recursos**: [Guía de Testing Unitario](https://chatgpt.com/share/69171fb4-d420-800f-80ae-93262acba3dc)

### Estrategia de Testing

#### 1. **Pruebas Unitarias**

Cada servicio incluye pruebas unitarias para:

- **Servicios de Aplicación**: Lógica de negocio con mocks de repositorios
- **Controladores**: Validación de endpoints y manejo de DTOs
- **Servicios de Seguridad**: Hashing de contraseñas, generación y validación de JWT
- **Mappers**: Conversión entre modelos de dominio, entidades y DTOs

#### 2. **Frameworks y Herramientas**

- **JUnit 5**: Framework principal de testing
- **Mockito**: Para crear mocks de dependencias
  - `@ExtendWith(MockitoExtension.class)` para integración con JUnit 5
  - `@Mock` para dependencias mockeadas
  - `@InjectMocks` para inyectar mocks en el objeto bajo prueba
- **AssertJ**: Para aserciones más legibles y expresivas
- **Spring Boot Test**: Para tests de integración cuando sea necesario

#### 3. **Cobertura de Código con JaCoCo**

Cada servicio está configurado con **JaCoCo** para medir la cobertura de código:

- **Configuración**: Plugin JaCoCo en `build.gradle.kts`
- **Reportes**: Generación automática de reportes HTML y XML después de ejecutar tests
- **Ubicación de reportes**: `build/reports/jacoco/test/html/index.html`

#### 4. **Ejecución de Tests**

##### Ejecutar tests de un servicio específico

```bash
# Desde la raíz del proyecto
./gradlew :services:catalog-service:test
./gradlew :services:users-service:test
./gradlew :services:orders-service:test

# O desde el directorio del servicio
cd services/catalog-service
../../gradlew test
```

##### Ejecutar todos los tests del proyecto

```bash
# Desde la raíz del proyecto
./gradlew test
```

##### Ejecutar un test específico

```bash
# Ejecutar una clase de test específica
./gradlew :services:users-service:test --tests UserServiceTest

# Ejecutar un método de test específico
./gradlew :services:users-service:test --tests UserServiceTest.shouldCreateUserWhenEmailAndPhoneAreUnique
```

##### Ver resultados de los tests

Los resultados de los tests se generan en:
- **Reportes HTML**: `services/[service-name]/build/reports/tests/test/index.html`
- **Reportes XML**: `services/[service-name]/build/test-results/test/`

```bash
# Abrir reporte HTML de tests (macOS)
open services/catalog-service/build/reports/tests/test/index.html

# Abrir reporte HTML de tests (Linux)
xdg-open services/catalog-service/build/reports/tests/test/index.html

# Abrir reporte HTML de tests (Windows)
start services\catalog-service\build\reports\tests\test\index.html
```

#### 5. **Generar Reportes de Cobertura**

Los reportes de cobertura se generan automáticamente después de ejecutar los tests (gracias a la configuración `finalizedBy(tasks.jacocoTestReport)` en `build.gradle.kts`).

##### Generar reporte de cobertura de un servicio

```bash
# Desde la raíz del proyecto (ejecuta tests y genera reporte)
./gradlew :services:catalog-service:test
./gradlew :services:users-service:test
./gradlew :services:orders-service:test

# O explícitamente generar el reporte
./gradlew :services:catalog-service:jacocoTestReport
./gradlew :services:users-service:jacocoTestReport
./gradlew :services:orders-service:jacocoTestReport
```

##### Generar reportes de cobertura de todos los servicios

```bash
# Desde la raíz del proyecto
./gradlew test jacocoTestReport
```

##### Ver reportes de cobertura

Los reportes se generan en:
- **Reporte HTML**: `services/[service-name]/build/reports/jacoco/test/html/index.html`
- **Reporte XML**: `services/[service-name]/build/reports/jacoco/test/jacocoTestReport.xml`

```bash
# Abrir reporte HTML de cobertura (macOS)
open services/catalog-service/build/reports/jacoco/test/html/index.html
open services/users-service/build/reports/jacoco/test/html/index.html
open services/orders-service/build/reports/jacoco/test/html/index.html

# Abrir reporte HTML de cobertura (Linux)
xdg-open services/catalog-service/build/reports/jacoco/test/html/index.html
xdg-open services/users-service/build/reports/jacoco/test/html/index.html
xdg-open services/orders-service/build/reports/jacoco/test/html/index.html

# Abrir reporte HTML de cobertura (Windows)
start services\catalog-service\build\reports\jacoco\test\html\index.html
start services\users-service\build\reports\jacoco\test\html\index.html
start services\orders-service\build\reports\jacoco\test\html\index.html
```

##### Métricas de Cobertura

JaCoCo reporta las siguientes métricas en los reportes:

- **INSTRUCTION**: Cobertura de instrucciones (líneas de código ejecutadas)
- **BRANCH**: Cobertura de ramas (if/else, switch, etc.)
- **LINE**: Cobertura de líneas
- **METHOD**: Cobertura de métodos
- **CLASS**: Cobertura de clases

El reporte HTML muestra:
- Vista general con porcentajes de cobertura por métrica
- Desglose por paquete
- Desglose por clase con indicadores de color (verde = cubierto, rojo = no cubierto)
- Líneas de código resaltadas mostrando qué líneas fueron ejecutadas durante los tests

#### 6. **Ejemplos de Tests**

Los tests siguen el patrón **AAA (Arrange-Act-Assert)**:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepositoryPort userRepositoryPort;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("Debería crear un usuario cuando el email y teléfono son únicos")
    void shouldCreateUserWhenEmailAndPhoneAreUnique() {
        // Arrange
        User user = new User("test@example.com", "Test User", "+1234567890", "Address");
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
        when(userRepositoryPort.existsByTelefono(anyString())).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(user);
        
        // Act
        User result = userService.create(user);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepositoryPort).save(user);
    }
}
```

#### 7. **Documentación de Cobertura**

Cada servicio incluye un archivo `COVERAGE.md` con:
- Comandos para ejecutar tests y generar reportes
- Ubicación de los reportes
- Interpretación de las métricas
- Troubleshooting común

### Servicios con Tests Implementados

- ✅ **users-service**: Tests completos para UserService, AuthService, JwtTokenService, PasswordHashService
- ✅ **catalog-service**: Tests para ProductService y CatalogSettingsService
- ✅ **orders-service**: Tests para OrderService, CartService y OrderSettingsService
