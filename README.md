# ecommerce-simulator

Monorepo de servicios de ecommerce construido con Spring Boot.

## Estructura del Proyecto

Este proyecto se organiza como un monorepo con los siguientes módulos:

- `libs/`: Librerías compartidas (DTOs, utilidades, etc.).
- `services/catalog-service/`: Servicio de catálogo de productos.
- `services/users-service/`: Servicio de gestión de usuarios y autenticación.
- `services/orders-service/`: Servicio de carritos, pedidos, pagos y configuración de órdenes.

## Puertos y rutas base

| Servicio         | Puerto | Base URL                      |
|------------------|--------|-------------------------------|
| Catalog Service  | 8081   | `http://localhost:8081/api/v1` |
| Users Service    | 8082   | `http://localhost:8082/api/v1` |
| Orders Service   | 8083   | `http://localhost:8083/api/v1` |

> Todos los servicios exponen `GET /ping` en su respectiva base URL para verificaciones de salud.

## Documentación de APIs

### Catalog Service (`http://localhost:8081/api/v1`)

#### Productos

- **Listar productos**  
  `GET /catalog/products` con parámetros opcionales `page` (default `0`), `size` (default `10`) y `search`.  
  ```bash
  curl "http://localhost:8081/api/v1/catalog/products?page=0&size=5&search=camisa"
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
  curl "http://localhost:8081/api/v1/catalog/products/1"
  ```

- **Crear producto**  
  `POST /catalog/products`  
  ```bash
  curl -X POST "http://localhost:8081/api/v1/catalog/products" \
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
    "price": 89.90,
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
  curl -X PUT "http://localhost:8081/api/v1/catalog/settings" \
    -H "Content-Type: application/json" \
    -d '{ "minimumStock": 10 }'
  ```

### Users Service (`http://localhost:8082/api/v1`)

#### Autenticación

- **Login**  
  `POST /auth/login`  
  ```bash
  curl -X POST "http://localhost:8082/api/v1/auth/login" \
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
  curl -X POST "http://localhost:8082/api/v1/users" \
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

### Orders Service (`http://localhost:8083/api/v1`)

> Las operaciones de tarjetas (`/cards`) y pagos (`/payments`) requieren:  
> - Cabecera `x-api-key` con la API key configurada en `security.api-key`.  
> - Cabecera `Authorization: Bearer <jwt>` con un token válido cuyo `userId` se pueda resolver.

#### Carritos

- **Crear carrito**  
  `POST /carts`  
  ```bash
  curl -X POST "http://localhost:8083/api/v1/carts" \
    -H "Content-Type: application/json" \
    -d '{ "userId": 5 }'
  ```

- **Agregar producto**  
  `POST /carts/{cartId}/items`  
  ```bash
  curl -X POST "http://localhost:8083/api/v1/carts/10/items" \
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
  curl -X POST "http://localhost:8083/api/v1/orders" \
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
  curl -X POST "http://localhost:8083/api/v1/payments" \
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
  curl -X POST "http://localhost:8083/api/v1/cards" \
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
  curl -X PUT "http://localhost:8083/api/v1/orders/settings" \
    -H "Content-Type: application/json" \
    -d '{
      "cardRejectionProbability": 0.1,
      "paymentRetryAttempts": 5
    }'
  ```

## Ejecutar los Servicios

```bash
# Desde la raíz del proyecto
./gradlew :services:catalog-service:bootRun
./gradlew :services:users-service:bootRun
./gradlew :services:orders-service:bootRun
```

```bash
# Desde cada servicio
cd services/catalog-service && ../../gradlew bootRun
cd services/users-service && ../../gradlew bootRun
cd services/orders-service && ../../gradlew bootRun
```

## Tecnologías

- Spring Boot 3.5.7
- Java 17
- Gradle (Kotlin DSL)
