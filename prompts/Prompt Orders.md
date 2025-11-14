# Crear microservicio orders service
_Exported on 11/14/2025 at 08:18:16 GMT-4 from Cursor (2.0.69)_

---

**User**

ahora necesito crear el microservicio orders service, siguiendo los mismos esquemas seguidos en los servicios anteriores con hexagonal, h2 y el endpoint /ping para el health con pong, este servira para gestionar carritos de usuarios, pedidos y pagos

---

**Cursor**

```java
package com.tuempresa.ecommerce.catalog.application.service;

import com.tuempresa.ecommerce.catalog.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.catalog.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.model.SearchQuery;
import com.tuempresa.ecommerce.catalog.domain.port.in.ProductUseCase;
import com.tuempresa.ecommerce.catalog.domain.port.out.ProductRepositoryPort;
import com.tuempresa.ecommerce.catalog.domain.port.out.CatalogSettingsRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final CatalogSettingsRepositoryPort catalogSettingsRepositoryPort;
    private final SearchQueryLoggingService searchQueryLoggingService;

    public ProductService(ProductRepositoryPort productRepositoryPort,
                          CatalogSettingsRepositoryPort catalogSettingsRepositoryPort,
                          SearchQueryLoggingService searchQueryLoggingService) {
        this.productRepositoryPort = productRepositoryPort;
        this.catalogSettingsRepositoryPort = catalogSettingsRepositoryPort;
        this.searchQueryLoggingService = searchQueryLoggingService;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Product> findAll(int page, int size, String search) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = size <= 0 ? 10 : size;
        String normalizedSearch = search != null && !search.trim().isEmpty() ? search.trim() : null;

        int minimumStock = catalogSettingsRepositoryPort.findSettings()
                .map(settings -> settings.getMinimumStock() != null ? settings.getMinimumStock() : 0)
                .orElse(0);

        PaginatedResult<Product> result = productRepositoryPort.findAll(sanitizedPage, sanitizedSize, normalizedSearch, minimumStock);

        SearchQuery searchQuery = new SearchQuery(normalizedSearch, sanitizedPage, sanitizedSize);
        searchQueryLoggingService.logSearch(searchQuery);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepositoryPort.findById(id);
    }

    @Override
    public Product create(Product product) {
        productRepositoryPort.findByName(product.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Ya existe un producto con el nombre: " + product.getName());
                });
        product.setDeleted(false);
        product.setCreatedAt(Optional.ofNullable(product.getCreatedAt()).orElse(LocalDateTime.now()));
        product.setUpdatedAt(Optional.ofNullable(product.getUpdatedAt()).orElse(LocalDateTime.now()));
        return productRepositoryPort.save(product);
    }

    @Override
    public Product update(Long id, Product product) {
        Product existingProduct = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (!existingProduct.getName().equalsIgnoreCase(product.getName())
                && productRepositoryPort.existsByNameExcludingId(product.getName(), id)) {
            throw new DuplicateResourceException("Ya existe un producto con el nombre: " + product.getName());
        }

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        return productRepositoryPort.save(existingProduct);
    }

    @Override
    public void delete(Long id) {
        productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        productRepositoryPort.softDelete(id);
    }
}



```

```plaintext
rootProject.name = "ecommerce-simulator"

include("libs")
include("services:catalog-service")
include("services:users-service")
include("services:orders-service")


```

```plaintext
plugins {
    `java`
    id("org.springframework.boot") version "3.5.7"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


```

```java
package com.tuempresa.ecommerce.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;

public class CartItem {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;

    public CartItem() {
    }

    public CartItem(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cart {

    private Long id;
    private Long userId;
    private List<CartItem> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cart() {
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    public Cart(Long id,
                Long userId,
                List<CartItem> items,
                BigDecimal total,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Cart(Long userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addItem(CartItem item) {
        this.items.add(item);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void clearItems() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        this.total = this.items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;

public class OrderItem {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;

    public OrderItem() {
    }

    public OrderItem(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    public enum Status {
        CREATED,
        PAID,
        SHIPPED,
        COMPLETED,
        CANCELLED
    }

    private Long id;
    private Long userId;
    private List<OrderItem> items;
    private BigDecimal total;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
        this.status = Status.CREATED;
    }

    public Order(Long id,
                 Long userId,
                 List<OrderItem> items,
                 BigDecimal total,
                 Status status,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Order(Long userId, List<OrderItem> items, BigDecimal total) {
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.status = Status.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void markAsPaid() {
        this.status = Status.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        this.total = this.items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    public enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
    }

    public Payment(Long id,
                   Long orderId,
                   BigDecimal amount,
                   String method,
                   Status status,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Payment(Long orderId, BigDecimal amount, String method) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void markCompleted() {
        this.status = Status.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.exception;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;

public interface CartUseCase {

    Cart createCart(Long userId);

    Cart addItem(Long cartId, CartItem item);

    Cart removeItem(Long cartId, Long productId);

    Cart clearCart(Long cartId);

    Cart getById(Long cartId);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Order;

public interface OrderUseCase {

    Order createFromCart(Long cartId);

    Order findById(Long orderId);

    Order markAsPaid(Long orderId);

    Order cancel(Long orderId);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

public interface PaymentUseCase {

    Payment registerPayment(Payment payment);

    Payment markAsCompleted(Long paymentId);

    Payment markAsFailed(Long paymentId);

    Payment findById(Long paymentId);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.Cart;

import java.util.Optional;

public interface CartRepositoryPort {

    Cart save(Cart cart);

    Optional<Cart> findById(Long id);

    void deleteById(Long id);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.Order;

import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CartService implements CartUseCase {

    private final CartRepositoryPort cartRepositoryPort;

    public CartService(CartRepositoryPort cartRepositoryPort) {
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Cart createCart(Long userId) {
        Cart cart = new Cart(userId);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart addItem(Long cartId, CartItem item) {
        Cart cart = getOrThrow(cartId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
        } else {
            cart.addItem(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart removeItem(Long cartId, Long productId) {
        Cart cart = getOrThrow(cartId);
        cart.removeItem(productId);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart clearCart(Long cartId) {
        Cart cart = getOrThrow(cartId);
        cart.clearItems();
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getById(Long cartId) {
        return getOrThrow(cartId);
    }

    private Cart getOrThrow(Long cartId) {
        return cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.InvalidOrderStateException;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort, CartRepositoryPort cartRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Order createFromCart(Long cartId) {
        Cart cart = cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));

        if (cart.getItems().isEmpty()) {
            throw new InvalidOrderStateException("No se puede crear una orden con un carrito vacío");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(cart.getUserId(), orderItems, total);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepositoryPort.save(order);

        cart.clearItems();
        cartRepositoryPort.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));
    }

    @Override
    public Order markAsPaid(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede marcar como pagada una orden cancelada");
        }
        order.markAsPaid();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    @Override
    public Order cancel(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.PAID.equals(order.getStatus()) || Order.Status.COMPLETED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede cancelar una orden pagada o completada");
        }
        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    private OrderItem toOrderItem(CartItem cartItem) {
        return new OrderItem(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort, OrderRepositoryPort orderRepositoryPort) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public Payment registerPayment(Payment payment) {
        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));

        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        if (Payment.Status.COMPLETED.equals(payment.getStatus())) {
            order.markAsPaid();
            orderRepositoryPort.save(order);
        }

        return savedPayment;
    }

    @Override
    public Payment markAsCompleted(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markCompleted();
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));
        order.markAsPaid();
        orderRepositoryPort.save(order);

        return savedPayment;
    }

    @Override
    public Payment markAsFailed(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markFailed();
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long paymentId) {
        return getOrThrow(paymentId);
    }

    private Payment getOrThrow(Long paymentId) {
        return paymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService implements CartUseCase {

    private final CartRepositoryPort cartRepositoryPort;

    public CartService(CartRepositoryPort cartRepositoryPort) {
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Cart createCart(Long userId) {
        Cart cart = new Cart(userId);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart addItem(Long cartId, CartItem item) {
        Cart cart = getOrThrow(cartId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        Optional<CartItem> existingItem = items.stream()
                .filter(cartItem -> cartItem.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
        } else {
            items.add(item);
        }

        cart.setItems(items);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart removeItem(Long cartId, Long productId) {
        Cart cart = getOrThrow(cartId);
        cart.removeItem(productId);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart clearCart(Long cartId) {
        Cart cart = getOrThrow(cartId);
        cart.clearItems();
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getById(Long cartId) {
        return getOrThrow(cartId);
    }

    private Cart getOrThrow(Long cartId) {
        return cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class CartItemEmbeddable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    public CartItemEmbeddable() {
    }

    public CartItemEmbeddable(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    private List<CartItemEmbeddable> items = new ArrayList<>();

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CartEntity() {
    }

    public CartEntity(Long id,
                      Long userId,
                      List<CartItemEmbeddable> items,
                      BigDecimal total,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemEmbeddable> getItems() {
        return items;
    }

    public void setItems(List<CartItemEmbeddable> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class OrderItemEmbeddable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    public OrderItemEmbeddable() {
    }

    public OrderItemEmbeddable(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItemEmbeddable> items = new ArrayList<>();

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Order.Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OrderEntity() {
    }

    public OrderEntity(Long id,
                       Long userId,
                       List<OrderItemEmbeddable> items,
                       BigDecimal total,
                       Order.Status status,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItemEmbeddable> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEmbeddable> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Order.Status getStatus() {
        return status;
    }

    public void setStatus(Order.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "method", nullable = false)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Payment.Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PaymentEntity() {
    }

    public PaymentEntity(Long id,
                         Long orderId,
                         BigDecimal amount,
                         String method,
                         Payment.Status status,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Payment.Status getStatus() {
        return status;
    }

    public void setStatus(Payment.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartItemEmbeddable;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    private CartMapper() {
    }

    public static CartEntity toEntity(Cart cart) {
        CartEntity entity = new CartEntity();
        entity.setId(cart.getId());
        entity.setUserId(cart.getUserId());
        entity.setItems(toEmbeddableList(cart.getItems()));
        entity.setTotal(cart.getTotal());
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());
        return entity;
    }

    public static Cart toDomain(CartEntity entity) {
        return new Cart(
                entity.getId(),
                entity.getUserId(),
                toDomainList(entity.getItems()),
                entity.getTotal(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static List<CartItemEmbeddable> toEmbeddableList(List<CartItem> items) {
        return items.stream()
                .map(item -> new CartItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }

    private static List<CartItem> toDomainList(List<CartItemEmbeddable> items) {
        return items.stream()
                .map(item -> new CartItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderItemEmbeddable;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setUserId(order.getUserId());
        entity.setItems(toEmbeddableList(order.getItems()));
        entity.setTotal(order.getTotal());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getUserId(),
                toDomainList(entity.getItems()),
                entity.getTotal(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static List<OrderItemEmbeddable> toEmbeddableList(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }

    private static List<OrderItem> toDomainList(List<OrderItemEmbeddable> items) {
        return items.stream()
                .map(item -> new OrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;

public class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setOrderId(payment.getOrderId());
        entity.setAmount(payment.getAmount());
        entity.setMethod(payment.getMethod());
        entity.setStatus(payment.getStatus());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getMethod(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.CartMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.CartJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CartRepositoryAdapter implements CartRepositoryPort {

    private final CartJpaRepository cartJpaRepository;

    public CartRepositoryAdapter(CartJpaRepository cartJpaRepository) {
        this.cartJpaRepository = cartJpaRepository;
    }

    @Override
    public Cart save(Cart cart) {
        CartEntity entity = CartMapper.toEntity(cart);
        CartEntity saved = cartJpaRepository.save(entity);
        return CartMapper.toDomain(saved);
    }

    @Override
    public Optional<Cart> findById(Long id) {
        return cartJpaRepository.findById(id)
                .map(CartMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        cartJpaRepository.deleteById(id);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.OrderMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderMapper.toEntity(order);
        OrderEntity saved = orderJpaRepository.save(entity);
        return OrderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id)
                .map(OrderMapper::toDomain);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.PaymentMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = PaymentMapper.toEntity(payment);
        PaymentEntity saved = paymentJpaRepository.save(entity);
        return PaymentMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id)
                .map(PaymentMapper::toDomain);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public class CreateCartRequest {

    @NotNull
    private Long userId;

    public CreateCartRequest() {
    }

    public CreateCartRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AddCartItemRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private BigDecimal price;

    public AddCartItemRequest() {
    }

    public AddCartItemRequest(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;

    public CartItemResponse() {
    }

    public CartItemResponse(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartResponse() {
    }

    public CartResponse(Long id,
                        Long userId,
                        List<CartItemResponse> items,
                        BigDecimal total,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull
    private Long cartId;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long cartId) {
        this.cartId = cartId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;

    public OrderItemResponse() {
    }

    public OrderItemResponse(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import com.tuempresa.ecommerce.orders.domain.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long userId;
    private List<OrderItemResponse> items;
    private BigDecimal total;
    private Order.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponse() {
    }

    public OrderResponse(Long id,
                         Long userId,
                         List<OrderItemResponse> items,
                         BigDecimal total,
                         Order.Status status,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Order.Status getStatus() {
        return status;
    }

    public void setStatus(Order.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull
    private Long orderId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @NotBlank
    private String method;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(Long orderId, BigDecimal amount, String method) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private Payment.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id,
                           Long orderId,
                           BigDecimal amount,
                           String method,
                           Payment.Status status,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Payment.Status getStatus() {
        return status;
    }

    public void setStatus(Payment.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.AddCartItemRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartItemResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;

import java.util.List;
import java.util.stream.Collectors;

public class CartWebMapper {

    private CartWebMapper() {
    }

    public static CartItem toDomain(AddCartItemRequest request) {
        return new CartItem(
                request.getProductId(),
                request.getProductName(),
                request.getQuantity(),
                request.getPrice()
        );
    }

    public static CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                toItemResponses(cart.getItems()),
                cart.getTotal(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private static List<CartItemResponse> toItemResponses(List<CartItem> items) {
        return items.stream()
                .map(item -> new CartItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderItemResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;

public class OrderWebMapper {

    private OrderWebMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                toItemResponses(order.getItems()),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;

public class PaymentWebMapper {

    private PaymentWebMapper() {
    }

    public static Payment toDomain(CreatePaymentRequest request) {
        return new Payment(
                request.getOrderId(),
                request.getAmount(),
                request.getMethod()
        );
    }

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.application.service.CartService;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.AddCartItemRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCartRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CartWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CreateCartRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartWebMapper.toResponse(cartService.createCart(request.getUserId())));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long cartId,
                                                @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(
                        cartService.addItem(cartId, CartWebMapper.toDomain(request))
                )
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartId,
                                                   @PathVariable Long productId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.removeItem(cartId, productId))
        );
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.clearCart(cartId))
        );
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.getById(cartId))
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.AddCartItemRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCartRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CartWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartUseCase cartService;

    public CartController(CartUseCase cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CreateCartRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartWebMapper.toResponse(cartService.createCart(request.getUserId())));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long cartId,
                                                @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(
                        cartService.addItem(cartId, CartWebMapper.toDomain(request))
                )
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartId,
                                                   @PathVariable Long productId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.removeItem(cartId, productId))
        );
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.clearCart(cartId))
        );
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.getById(cartId))
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateOrderRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.OrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderWebMapper.toResponse(orderUseCase.createFromCart(request.getCartId())));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> markAsPaid(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.markAsPaid(orderId))
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.cancel(orderId))
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.findById(orderId))
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> registerPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentWebMapper.toResponse(paymentUseCase.registerPayment(PaymentWebMapper.toDomain(request))));
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponse> markAsCompleted(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsCompleted(paymentId))
        );
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> markAsFailed(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsFailed(paymentId))
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.findById(paymentId))
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}


```

```yaml
server:
  port: 8083

spring:
  main:
    allow-bean-definition-overriding: true
  datasource:
    url: jdbc:h2:mem:ordersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true


```

## Resumen
- Nuevo módulo `services/orders-service` incluido en `settings.gradle.kts`, con configuración Gradle propia y estructura hexagonal completa (dominio, casos de uso, adaptadores JPA y mapeadores web).
- Modelos de dominio para carritos, órdenes y pagos, con servicios de aplicación que orquestan repositorios H2 mediante adaptadores (Colecciones embebidas para items).
- Controladores REST para carritos, órdenes y pagos, más endpoint de health `/ping` que responde `pong`, con DTOs y mappers dedicados para mantener la capa web desacoplada.

## Referencias clave
```1:13:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/controller/HealthController.java
@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
```

## Verificación
- `./gradlew :services:orders-service:build` *(falló: la sandbox no tiene un Java Runtime disponible; no fue posible compilar).*

```java
@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
```

---

**User**

necesito que el carrito de compras replique la data que reciba del producto al agregar un producto al carrito, y tambien que absorba la data del usario dueño del carrito, si es necesario comunicarlo con los otros microservicios de catalogo y usuarios hazlo

---

**Cursor**

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;

public class CartItem {

    private Long productId;
    private String productName;
    private String productDescription;
    private Integer quantity;
    private BigDecimal price;

    public CartItem() {
    }

    public CartItem(Long productId,
                    String productName,
                    String productDescription,
                    Integer quantity,
                    BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cart {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String userAddress;
    private List<CartItem> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cart() {
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    public Cart(Long id,
                Long userId,
                String userEmail,
                String userName,
                String userPhone,
                String userAddress,
                List<CartItem> items,
                BigDecimal total,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Cart(Long userId,
                String userEmail,
                String userName,
                String userPhone,
                String userAddress) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addItem(CartItem item) {
        this.items.add(item);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void clearItems() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        this.total = this.items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}



```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;

public class ProductSnapshot {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;

    public ProductSnapshot() {
    }

    public ProductSnapshot(Long id, String name, String description, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.model;

public class UserSnapshot {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String address;

    public UserSnapshot() {
    }

    public UserSnapshot(Long id, String email, String name, String phone, String address) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;

import java.util.Optional;

public interface ProductCatalogClientPort {

    Optional<ProductSnapshot> findById(Long productId);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;

import java.util.Optional;

public interface UserClientPort {

    Optional<UserSnapshot> findById(Long userId);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Cart;

public interface CartUseCase {

    Cart createCart(Long userId);

    Cart addItem(Long cartId, Long productId, Integer quantity);

    Cart removeItem(Long cartId, Long productId);

    Cart clearCart(Long cartId);

    Cart getById(Long cartId);
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;
import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.ProductCatalogClientPort;
import com.tuempresa.ecommerce.orders.domain.port.out.UserClientPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService implements CartUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final UserClientPort userClientPort;
    private final ProductCatalogClientPort productCatalogClientPort;

    public CartService(CartRepositoryPort cartRepositoryPort,
                       UserClientPort userClientPort,
                       ProductCatalogClientPort productCatalogClientPort) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.userClientPort = userClientPort;
        this.productCatalogClientPort = productCatalogClientPort;
    }

    @Override
    public Cart createCart(Long userId) {
        UserSnapshot user = userClientPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        Cart cart = new Cart(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getAddress()
        );
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart addItem(Long cartId, Long productId, Integer quantity) {
        Cart cart = getOrThrow(cartId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        ProductSnapshot product = productCatalogClientPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        CartItem newItem = new CartItem(
                product.getId(),
                product.getName(),
                product.getDescription(),
                quantity,
                product.getPrice()
        );

        Optional<CartItem> existingItem = items.stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setProductName(newItem.getProductName());
            cartItem.setProductDescription(newItem.getProductDescription());
            cartItem.setPrice(newItem.getPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            items.add(newItem);
        }

        cart.setItems(items);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart removeItem(Long cartId, Long productId) {
        Cart cart = getOrThrow(cartId);
        cart.removeItem(productId);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart clearCart(Long cartId) {
        Cart cart = getOrThrow(cartId);
        cart.clearItems();
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getById(Long cartId) {
        return getOrThrow(cartId);
    }

    private Cart getOrThrow(Long cartId) {
        return cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.AddCartItemRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCartRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CartWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartUseCase cartService;

    public CartController(CartUseCase cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CreateCartRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartWebMapper.toResponse(cartService.createCart(request.getUserId())));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long cartId,
                                                @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(
                        cartService.addItem(cartId, request.getProductId(), request.getQuantity())
                )
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartId,
                                                   @PathVariable Long productId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.removeItem(cartId, productId))
        );
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.clearCart(cartId))
        );
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.getById(cartId))
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public AddCartItemRequest() {
    }

    public AddCartItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long productId;
    private String productName;
    private String productDescription;
    private Integer quantity;
    private BigDecimal price;

    public CartItemResponse() {
    }

    public CartItemResponse(Long productId,
                            String productName,
                            String productDescription,
                            Integer quantity,
                            BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}



```

```java
*** Begin Patch
*** Update File: /Users/gebrayel/Documents/Personal-Projects/springboot-test/ecommerce-simulator/services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/dto/CartResponse.java
@@
-    private Long id;
-    private Long userId;
-    private List<CartItemResponse> items;
-    private BigDecimal total;
-    private LocalDateTime createdAt;
-    private LocalDateTime updatedAt;
+    private Long id;
+    private Long userId;
+    private String userEmail;
+    private String userName;
+    private String userPhone;
+    private String userAddress;
+    private List<CartItemResponse> items;
+    private BigDecimal total;
+    private LocalDateTime createdAt;
+    private LocalDateTime updatedAt;
@@
-    public CartResponse(Long id,
-                        Long userId,
-                        List<CartItemResponse> items,
-                        BigDecimal total,
-                        LocalDateTime createdAt,
-                        LocalDateTime updatedAt) {
+    public CartResponse(Long id,
+                        Long userId,
+                        String userEmail,
+                        String userName,
+                        String userPhone,
+                        String userAddress,
+                        List<CartItemResponse> items,
+                        BigDecimal total,
+                        LocalDateTime createdAt,
+                        LocalDateTime updatedAt) {
         this.id = id;
         this.userId = userId;
+        this.userEmail = userEmail;
+        this.userName = userName;
+        this.userPhone = userPhone;
+        this.userAddress = userAddress;
         this.items = items;
         this.total = total;
         this.createdAt = createdAt;
         this.updatedAt = updatedAt;
@@
     public void setUserId(Long userId) {
         this.userId = userId;
     }
+
+    public String getUserEmail() {
+        return userEmail;
+    }
+
+    public void setUserEmail(String userEmail) {
+        this.userEmail = userEmail;
+    }
+
+    public String getUserName() {
+        return userName;
+    }
+
 +    public void setUserName(String userName) {
 +        this.userName = userName;
 +    }
 +
 +    public String getUserPhone() {
 +        return userPhone;
 +    }
 +
 +    public void setUserPhone(String userPhone) {
 +        this.userPhone = userPhone;
 +    }
 +
 +    public String getUserAddress() {
 +        return userAddress;
 +    }
 +
 +    public void setUserAddress(String userAddress) {
 +        this.userAddress = userAddress;
 +    }
*** End Patch
```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String userAddress;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartResponse() {
    }

    public CartResponse(Long id,
                        Long userId,
                        String userEmail,
                        String userName,
                        String userPhone,
                        String userAddress,
                        List<CartItemResponse> items,
                        BigDecimal total,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = items;
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartItemResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;

import java.util.List;
import java.util.stream.Collectors;

public class CartWebMapper {

    private CartWebMapper() {
    }

    public static CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getUserEmail(),
                cart.getUserName(),
                cart.getUserPhone(),
                cart.getUserAddress(),
                toItemResponses(cart.getItems()),
                cart.getTotal(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private static List<CartItemResponse> toItemResponses(List<CartItem> items) {
        return items.stream()
                .map(item -> new CartItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class CartItemEmbeddable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_description", length = 1000)
    private String productDescription;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    public CartItemEmbeddable() {
    }

    public CartItemEmbeddable(Long productId,
                              String productName,
                              String productDescription,
                              Integer quantity,
                              BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "user_address")
    private String userAddress;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    private List<CartItemEmbeddable> items = new ArrayList<>();

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CartEntity() {
    }

    public CartEntity(Long id,
                      Long userId,
                      String userEmail,
                      String userName,
                      String userPhone,
                      String userAddress,
                      List<CartItemEmbeddable> items,
                      BigDecimal total,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = items;
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public List<CartItemEmbeddable> getItems() {
        return items;
    }

    public void setItems(List<CartItemEmbeddable> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartItemEmbeddable;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    private CartMapper() {
    }

    public static CartEntity toEntity(Cart cart) {
        CartEntity entity = new CartEntity();
        entity.setId(cart.getId());
        entity.setUserId(cart.getUserId());
        entity.setUserEmail(cart.getUserEmail());
        entity.setUserName(cart.getUserName());
        entity.setUserPhone(cart.getUserPhone());
        entity.setUserAddress(cart.getUserAddress());
        entity.setItems(toEmbeddableList(cart.getItems()));
        entity.setTotal(cart.getTotal());
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());
        return entity;
    }

    public static Cart toDomain(CartEntity entity) {
        return new Cart(
                entity.getId(),
                entity.getUserId(),
                entity.getUserEmail(),
                entity.getUserName(),
                entity.getUserPhone(),
                entity.getUserAddress(),
                toDomainList(entity.getItems()),
                entity.getTotal(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static List<CartItemEmbeddable> toEmbeddableList(List<CartItem> items) {
        return items.stream()
                .map(item -> new CartItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }

    private static List<CartItem> toDomainList(List<CartItemEmbeddable> items) {
        return items.stream()
                .map(item -> new CartItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.client.catalog;

import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.ProductCatalogClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CatalogClientAdapter implements ProductCatalogClientPort {

    private final RestTemplate restTemplate;

    public CatalogClientAdapter(RestTemplateBuilder restTemplateBuilder,
                                @Value("${clients.catalog.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    public Optional<ProductSnapshot> findById(Long productId) {
        try {
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                    "/catalog/products/{id}",
                    ProductResponse.class,
                    productId
            );

            ProductResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new ProductSnapshot(
                    body.id,
                    body.name,
                    body.description,
                    body.price
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }

    private static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.client.users;

import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.UserClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class UserClientAdapter implements UserClientPort {

    private final RestTemplate restTemplate;

    public UserClientAdapter(RestTemplateBuilder restTemplateBuilder,
                             @Value("${clients.users.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    public Optional<UserSnapshot> findById(Long userId) {
        try {
            ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                    "/users/{id}",
                    UserResponse.class,
                    userId
            );

            UserResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new UserSnapshot(
                    body.id,
                    body.email,
                    body.name,
                    body.telefono,
                    body.direccion
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }

    private static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String telefono;
        private String direccion;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }
    }
}


```

```yaml
server:
  port: 8083

spring:
  main:
    allow-bean-definition-overriding: true
  datasource:
    url: jdbc:h2:mem:ordersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

clients:
  catalog:
    base-url: http://localhost:8081
  users:
    base-url: http://localhost:8082



```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cart {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String userAddress;
    private List<CartItem> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cart() {
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    public Cart(Long id,
                Long userId,
                String userEmail,
                String userName,
                String userPhone,
                String userAddress,
                List<CartItem> items,
                BigDecimal total,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Cart(Long userId,
                String userEmail,
                String userName,
                String userPhone,
                String userAddress) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        if (items == null) {
            this.items = new ArrayList<>();
            this.total = BigDecimal.ZERO;
            return;
        }
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addItem(CartItem item) {
        this.items.add(item);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void clearItems() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        this.total = this.items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CartItemEmbeddable;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    private CartMapper() {
    }

    public static CartEntity toEntity(Cart cart) {
        CartEntity entity = new CartEntity();
        entity.setId(cart.getId());
        entity.setUserId(cart.getUserId());
        entity.setUserEmail(cart.getUserEmail());
        entity.setUserName(cart.getUserName());
        entity.setUserPhone(cart.getUserPhone());
        entity.setUserAddress(cart.getUserAddress());
        entity.setItems(toEmbeddableList(cart.getItems()));
        entity.setTotal(cart.getTotal());
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setUpdatedAt(cart.getUpdatedAt());
        return entity;
    }

    public static Cart toDomain(CartEntity entity) {
        return new Cart(
                entity.getId(),
                entity.getUserId(),
                entity.getUserEmail(),
                entity.getUserName(),
                entity.getUserPhone(),
                entity.getUserAddress(),
                toDomainList(entity.getItems()),
                entity.getTotal(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static List<CartItemEmbeddable> toEmbeddableList(List<CartItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new CartItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }

    private static List<CartItem> toDomainList(List<CartItemEmbeddable> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new CartItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductDescription(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}



```

## Cambios
- El carrito ahora clona la información del usuario propietario al crearse y sincroniza nombre, email, teléfono y dirección dentro del agregado `Cart`, extendiendo la persistencia y las respuestas REST para exponer esos datos.  
- Al agregar productos al carrito ya no se confía en datos enviados por el cliente: se consulta el catálogo para obtener nombre, descripción y precio vigentes, actualizando las líneas existentes si el mismo producto estaba presente.  
- Se añadieron puertos y adaptadores HTTP (`CatalogClientAdapter`, `UserClientAdapter`) que consumen los microservicios de catálogo y usuarios mediante `RestTemplate`, con URLs configurables en `application.yaml`.

## Referencias

```36:85:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/application/service/CartService.java
    public Cart createCart(Long userId) {
        UserSnapshot user = userClientPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        Cart cart = new Cart(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getAddress()
        );
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart addItem(Long cartId, Long productId, Integer quantity) {
        Cart cart = getOrThrow(cartId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        ProductSnapshot product = productCatalogClientPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        CartItem newItem = new CartItem(
                product.getId(),
                product.getName(),
                product.getDescription(),
                quantity,
                product.getPrice()
        );

        Optional<CartItem> existingItem = items.stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setProductName(newItem.getProductName());
            cartItem.setProductDescription(newItem.getProductDescription());
            cartItem.setPrice(newItem.getPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            items.add(newItem);
        }

        cart.setItems(items);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }
```

```15:47:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/client/catalog/CatalogClientAdapter.java
    @Override
    public Optional<ProductSnapshot> findById(Long productId) {
        try {
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                    "/catalog/products/{id}",
                    ProductResponse.class,
                    productId
            );

            ProductResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new ProductSnapshot(
                    body.id,
                    body.name,
                    body.description,
                    body.price
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }
```

```7:43:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/dto/CartResponse.java
public class CartResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String userAddress;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartResponse(Long id,
                        Long userId,
                        String userEmail,
                        String userName,
                        String userPhone,
                        String userAddress,
                        List<CartItemResponse> items,
                        BigDecimal total,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = items;
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
```

## Verificación
- `./gradlew :services:orders-service:build` *(falló: la sandbox no tiene un JRE instalado, por lo que no fue posible compilar).*

## Siguientes pasos
- Instalar/activar un JDK en el entorno y volver a ejecutar el build.
- Asegurar que los servicios de catálogo (8081) y usuarios (8082) estén levantados para que el orders-service pueda resolver los datos remotos configurados en `application.yaml`.

```java
    public Cart createCart(Long userId) {
        UserSnapshot user = userClientPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        Cart cart = new Cart(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getAddress()
        );
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }

    @Override
    public Cart addItem(Long cartId, Long productId, Integer quantity) {
        Cart cart = getOrThrow(cartId);
        List<CartItem> items = new ArrayList<>(cart.getItems());

        ProductSnapshot product = productCatalogClientPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productId));

        CartItem newItem = new CartItem(
                product.getId(),
                product.getName(),
                product.getDescription(),
                quantity,
                product.getPrice()
        );

        Optional<CartItem> existingItem = items.stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setProductName(newItem.getProductName());
            cartItem.setProductDescription(newItem.getProductDescription());
            cartItem.setPrice(newItem.getPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            items.add(newItem);
        }

        cart.setItems(items);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepositoryPort.save(cart);
    }
```

```java
    @Override
    public Optional<ProductSnapshot> findById(Long productId) {
        try {
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                    "/catalog/products/{id}",
                    ProductResponse.class,
                    productId
            );

            ProductResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new ProductSnapshot(
                    body.id,
                    body.name,
                    body.description,
                    body.price
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }
```

```java
public class CartResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String userAddress;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartResponse(Long id,
                        Long userId,
                        String userEmail,
                        String userName,
                        String userPhone,
                        String userAddress,
                        List<CartItemResponse> items,
                        BigDecimal total,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.items = items;
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
```

---

**User**

quiero que todos mis servicios vayan por api/v1

---

**Cursor**

```java
package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.port.in.ProductUseCase;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.PaginatedResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/catalog/products")
public class ProductController {

    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        PaginatedResult<Product> result = productUseCase.findAll(page, size, search);

        PaginatedResponse<ProductResponse> response = new PaginatedResponse<>(
                result.getContent().stream()
                        .map(ProductWebMapper::toResponse)
                        .collect(Collectors.toList()),
                result.getPage(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productUseCase.findById(id)
                .map(ProductWebMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = ProductWebMapper.toDomain(request);
        Product created = productUseCase.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductWebMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest request) {
        Product product = ProductWebMapper.toDomain(request);
        Product updated = productUseCase.update(id, product);
        return ResponseEntity.status(HttpStatus.OK).body(ProductWebMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productUseCase.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}



```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.port.in.CatalogSettingsUseCase;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.mapper.CatalogSettingsWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog/settings")
public class CatalogSettingsController {

    private final CatalogSettingsUseCase catalogSettingsUseCase;

    public CatalogSettingsController(CatalogSettingsUseCase catalogSettingsUseCase) {
        this.catalogSettingsUseCase = catalogSettingsUseCase;
    }

    @GetMapping
    public ResponseEntity<CatalogSettingsResponse> getSettings() {
        CatalogSettings settings = catalogSettingsUseCase.getSettings();
        return ResponseEntity.status(HttpStatus.OK).body(CatalogSettingsWebMapper.toResponse(settings));
    }

    @PutMapping
    public ResponseEntity<CatalogSettingsResponse> updateSettings(@Valid @RequestBody CatalogSettingsRequest request) {
        CatalogSettings currentSettings = catalogSettingsUseCase.getSettings();
        CatalogSettings toUpdate = CatalogSettingsWebMapper.toDomain(request, currentSettings.getId());
        CatalogSettings updated = catalogSettingsUseCase.updateSettings(toUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(CatalogSettingsWebMapper.toResponse(updated));
    }
}



```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}



```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.UserUseCase;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserRequest;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserResponse;
import com.tuempresa.ecommerce.users.infrastructure.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userUseCase.findAll().stream()
                .map(UserWebMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userUseCase.findById(id)
                .map(user -> ResponseEntity.status(HttpStatus.OK).body(UserWebMapper.toResponse(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = UserWebMapper.toDomain(request);
        User createdUser = userUseCase.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserWebMapper.toResponse(createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = UserWebMapper.toDomain(request);
        User updatedUser = userUseCase.update(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(UserWebMapper.toResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userUseCase.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}

```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.client.catalog;

import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.ProductCatalogClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CatalogClientAdapter implements ProductCatalogClientPort {

    private final RestTemplate restTemplate;

    public CatalogClientAdapter(RestTemplateBuilder restTemplateBuilder,
                                @Value("${clients.catalog.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    public Optional<ProductSnapshot> findById(Long productId) {
        try {
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                    "/api/v1/catalog/products/{id}",
                    ProductResponse.class,
                    productId
            );

            ProductResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new ProductSnapshot(
                    body.id,
                    body.name,
                    body.description,
                    body.price
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }

    private static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.client.users;

import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.UserClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class UserClientAdapter implements UserClientPort {

    private final RestTemplate restTemplate;

    public UserClientAdapter(RestTemplateBuilder restTemplateBuilder,
                             @Value("${clients.users.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    public Optional<UserSnapshot> findById(Long userId) {
        try {
            ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                    "/api/v1/users/{id}",
                    UserResponse.class,
                    userId
            );

            UserResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new UserSnapshot(
                    body.id,
                    body.email,
                    body.name,
                    body.telefono,
                    body.direccion
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }

    private static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String telefono;
        private String direccion;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }
    }
}



```

## Cambios
- Todos los controladores de catálogo, usuarios y órdenes fueron actualizados para exponer endpoints bajo el prefijo común `/api/v1`, incluidos los health checks (`/api/v1/ping`).
- Los clientes HTTP del servicio de órdenes ajustan ahora sus llamadas a catálogo y usuarios usando los nuevos paths versionados.
- Las respuestas y rutas internas quedaron alineadas; no se modificó la lógica de negocio.

## Referencias

```17:25:services/catalog-service/src/main/java/com/tuempresa/ecommerce/catalog/infrastructure/web/controller/ProductController.java
@RestController
@RequestMapping("/api/v1/catalog/products")
public class ProductController {
```

```20:34:services/users-service/src/main/java/com/tuempresa/ecommerce/users/infrastructure/web/controller/UserController.java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
```

```26:36:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/client/catalog/CatalogClientAdapter.java
ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
        "/api/v1/catalog/products/{id}",
        ProductResponse.class,
        productId
);
```

## Verificación
- No se pudo ejecutar `./gradlew …:build`; el entorno sandbox no dispone de un JRE instalado. Cuando tengas Java disponible, recompila para validar.

```java
@RestController
@RequestMapping("/api/v1/catalog/products")
public class ProductController {
```

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
```

```java
ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
        "/api/v1/catalog/products/{id}",
        ProductResponse.class,
        productId
);
```

---

**User**

no hay forma de sintetizar en los endpoints el api/v1 sin agregarlo en cada route?

---

**Cursor**

Sí, puedes centralizar el prefijo para no repetir `api/v1` en cada `@RequestMapping`. En Spring Boot 3 basta con declarar en cada servicio:

```yaml
spring:
  mvc:
    servlet:
      path: /api/v1
```

(En versiones más antiguas se usaba `server.servlet.context-path`, pero ambos mueven todo el árbol de rutas).

Con ese ajuste:

- Los controladores pueden volver a usar rutas “cortas”, por ejemplo `@RequestMapping("/catalog/products")`.
- Todos los handlers quedarán colgando de `/api/v1`, incluido el `/ping` del health check.
- Cualquier cliente interno (p. ej. el `RestTemplate` del orders-service) debe apuntar a la nueva URL base (`http://localhost:8081/api/v1`), para que no tengas un doble `/api/v1`.

Así evitas repetir el prefijo en cada endpoint y mantienes el versionado centralizado en la configuración.

```yaml
spring:
  mvc:
    servlet:
      path: /api/v1
```

---

**User**

hazlo

---

**Cursor**

```yaml
server:
  port: 8081

spring:
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:catalogdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

```

```yaml
server:
  port: 8082

spring:
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:usersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

```

```yaml
server:
  port: 8083

spring:
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:ordersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

clients:
  catalog:
    base-url: http://localhost:8081/api/v1
  users:
    base-url: http://localhost:8082/api/v1



```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import com.tuempresa.ecommerce.catalog.domain.model.PaginatedResult;
import com.tuempresa.ecommerce.catalog.domain.model.Product;
import com.tuempresa.ecommerce.catalog.domain.port.in.ProductUseCase;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.PaginatedResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.ProductResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog/products")
public class ProductController {

    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        PaginatedResult<Product> result = productUseCase.findAll(page, size, search);

        PaginatedResponse<ProductResponse> response = new PaginatedResponse<>(
                result.getContent().stream()
                        .map(ProductWebMapper::toResponse)
                        .collect(Collectors.toList()),
                result.getPage(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productUseCase.findById(id)
                .map(ProductWebMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = ProductWebMapper.toDomain(request);
        Product created = productUseCase.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductWebMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest request) {
        Product product = ProductWebMapper.toDomain(request);
        Product updated = productUseCase.update(id, product);
        return ResponseEntity.status(HttpStatus.OK).body(ProductWebMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productUseCase.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}



```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import com.tuempresa.ecommerce.catalog.domain.model.CatalogSettings;
import com.tuempresa.ecommerce.catalog.domain.port.in.CatalogSettingsUseCase;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsRequest;
import com.tuempresa.ecommerce.catalog.infrastructure.web.dto.CatalogSettingsResponse;
import com.tuempresa.ecommerce.catalog.infrastructure.web.mapper.CatalogSettingsWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalog/settings")
public class CatalogSettingsController {

    private final CatalogSettingsUseCase catalogSettingsUseCase;

    public CatalogSettingsController(CatalogSettingsUseCase catalogSettingsUseCase) {
        this.catalogSettingsUseCase = catalogSettingsUseCase;
    }

    @GetMapping
    public ResponseEntity<CatalogSettingsResponse> getSettings() {
        CatalogSettings settings = catalogSettingsUseCase.getSettings();
        return ResponseEntity.status(HttpStatus.OK).body(CatalogSettingsWebMapper.toResponse(settings));
    }

    @PutMapping
    public ResponseEntity<CatalogSettingsResponse> updateSettings(@Valid @RequestBody CatalogSettingsRequest request) {
        CatalogSettings currentSettings = catalogSettingsUseCase.getSettings();
        CatalogSettings toUpdate = CatalogSettingsWebMapper.toDomain(request, currentSettings.getId());
        CatalogSettings updated = catalogSettingsUseCase.updateSettings(toUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(CatalogSettingsWebMapper.toResponse(updated));
    }
}



```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}



```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.UserUseCase;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserRequest;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserResponse;
import com.tuempresa.ecommerce.users.infrastructure.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userUseCase.findAll().stream()
                .map(UserWebMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userUseCase.findById(id)
                .map(user -> ResponseEntity.status(HttpStatus.OK).body(UserWebMapper.toResponse(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = UserWebMapper.toDomain(request);
        User createdUser = userUseCase.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserWebMapper.toResponse(createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = UserWebMapper.toDomain(request);
        User updatedUser = userUseCase.update(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(UserWebMapper.toResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userUseCase.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}

```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.CartUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.AddCartItemRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CartResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCartRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CartWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartUseCase cartService;

    public CartController(CartUseCase cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(@Valid @RequestBody CreateCartRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartWebMapper.toResponse(cartService.createCart(request.getUserId())));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long cartId,
                                                @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(
                        cartService.addItem(cartId, request.getProductId(), request.getQuantity())
                )
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartId,
                                                   @PathVariable Long productId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.removeItem(cartId, productId))
        );
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.clearCart(cartId))
        );
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(
                CartWebMapper.toResponse(cartService.getById(cartId))
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateOrderRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.OrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderWebMapper.toResponse(orderUseCase.createFromCart(request.getCartId())));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> markAsPaid(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.markAsPaid(orderId))
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.cancel(orderId))
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.findById(orderId))
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> registerPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentWebMapper.toResponse(paymentUseCase.registerPayment(PaymentWebMapper.toDomain(request))));
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponse> markAsCompleted(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsCompleted(paymentId))
        );
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> markAsFailed(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsFailed(paymentId))
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.findById(paymentId))
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.client.catalog;

import com.tuempresa.ecommerce.orders.domain.model.ProductSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.ProductCatalogClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CatalogClientAdapter implements ProductCatalogClientPort {

    private final RestTemplate restTemplate;

    public CatalogClientAdapter(RestTemplateBuilder restTemplateBuilder,
                                @Value("${clients.catalog.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    public Optional<ProductSnapshot> findById(Long productId) {
        try {
            ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                    "/catalog/products/{id}",
                    ProductResponse.class,
                    productId
            );

            ProductResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new ProductSnapshot(
                    body.id,
                    body.name,
                    body.description,
                    body.price
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }

    private static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.client.users;

import com.tuempresa.ecommerce.orders.domain.model.UserSnapshot;
import com.tuempresa.ecommerce.orders.domain.port.out.UserClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class UserClientAdapter implements UserClientPort {

    private final RestTemplate restTemplate;

    public UserClientAdapter(RestTemplateBuilder restTemplateBuilder,
                             @Value("${clients.users.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    public Optional<UserSnapshot> findById(Long userId) {
        try {
            ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                    "/users/{id}",
                    UserResponse.class,
                    userId
            );

            UserResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new UserSnapshot(
                    body.id,
                    body.email,
                    body.name,
                    body.telefono,
                    body.direccion
            ));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        }
    }

    private static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String telefono;
        private String direccion;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }
    }
}



```

Ya quedó centralizado el prefijo `api/v1`:

- Añadí `spring.mvc.servlet.path: /api/v1` en los `application.yaml` de catálogo, usuarios y órdenes para que todos los endpoints cuelguen automáticamente de ese prefijo.  
- Restablecí los `@RequestMapping` de los controladores a rutas “cortas” (`/catalog/...`, `/users`, `/carts`, etc.) y dejé los health checks sin path específico; gracias al servlet path los endpoints finales siguen siendo `/api/v1/...`.  
- Actualicé los clientes HTTP de `orders-service` para apuntar a las nuevas URLs base (`http://localhost:8081/api/v1`, `http://localhost:8082/api/v1`) y ajusté las rutas internas que consumen (`/catalog/products/{id}`, `/users/{id}`).

No ejecuté el build porque el entorno carece de JRE; cuando tengas Java disponible puedes correr `./gradlew :services:orders-service:build` para validar.

---

**User**

 hagamos unas pruebas de crear un carrito agregar a un carrito productos etc etc, dame un sample

---

**Cursor**

## Flujo de Pruebas (curl)

> Supongamos que los servicios corren en:
> - catálogo: `http://localhost:8081`
> - usuarios: `http://localhost:8082`
> - órdenes: `http://localhost:8083`

### 1. Preparación

```bash
# Crear un producto
curl -X POST http://localhost:8081/api/v1/catalog/products \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Nintendo Switch",
        "description": "Consola híbrida",
        "price": 299.99,
        "stock": 15
      }'

# Crear un usuario
curl -X POST http://localhost:8082/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
        "email": "ana@example.com",
        "name": "Ana Tester",
        "telefono": "+34 600 111 222",
        "direccion": "Calle Falsa 123, Madrid"
      }'
```

### 2. Carrito

```bash
# Crear carrito para el usuario (usa userId devuelto arriba)
curl -X POST http://localhost:8083/api/v1/carts \
  -H "Content-Type: application/json" \
  -d '{ "userId": 1 }'

# Agregar producto al carrito (productId del catálogo)
curl -X POST http://localhost:8083/api/v1/carts/1/items \
  -H "Content-Type: application/json" \
  -d '{ "productId": 1, "quantity": 2 }'

# Consultar el carrito
curl http://localhost:8083/api/v1/carts/1
```

### 3. Orden y Pago

```bash
# Crear una orden desde el carrito
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{ "cartId": 1 }'

# Marcar orden como pagada
curl -X POST http://localhost:8083/api/v1/orders/1/pay

# Registrar pago (opcional)
curl -X POST http://localhost:8083/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
        "orderId": 1,
        "amount": 599.98,
        "method": "CREDIT_CARD"
      }'
```

### 4. Salud y Diagnóstico

```bash
curl http://localhost:8081/api/v1/ping
curl http://localhost:8082/api/v1/ping
curl http://localhost:8083/api/v1/ping
```

### Notas
- Si todavía no tienes IDs, mira la respuesta de cada POST (`id` se devuelve en el JSON).
- El servicio de órdenes valida al usuario y obtiene los datos del producto automáticamente gracias a los clientes configurados.
- Para limpiar carritos: `DELETE /api/v1/carts/{cartId}/items` o eliminar un ítem concreto: `DELETE /api/v1/carts/{cartId}/items/{productId}`.

Cuando tengas Java disponible, puedes ejecutar los tests con `./gradlew :services:orders-service:build` para asegurarte de que todo compila.

```shellscript
# Crear un producto
curl -X POST http://localhost:8081/api/v1/catalog/products \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Nintendo Switch",
        "description": "Consola híbrida",
        "price": 299.99,
        "stock": 15
      }'

# Crear un usuario
curl -X POST http://localhost:8082/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
        "email": "ana@example.com",
        "name": "Ana Tester",
        "telefono": "+34 600 111 222",
        "direccion": "Calle Falsa 123, Madrid"
      }'
```

```shellscript
# Crear carrito para el usuario (usa userId devuelto arriba)
curl -X POST http://localhost:8083/api/v1/carts \
  -H "Content-Type: application/json" \
  -d '{ "userId": 1 }'

# Agregar producto al carrito (productId del catálogo)
curl -X POST http://localhost:8083/api/v1/carts/1/items \
  -H "Content-Type: application/json" \
  -d '{ "productId": 1, "quantity": 2 }'

# Consultar el carrito
curl http://localhost:8083/api/v1/carts/1
```

```shellscript
# Crear una orden desde el carrito
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{ "cartId": 1 }'

# Marcar orden como pagada
curl -X POST http://localhost:8083/api/v1/orders/1/pay

# Registrar pago (opcional)
curl -X POST http://localhost:8083/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
        "orderId": 1,
        "amount": 599.98,
        "method": "CREDIT_CARD"
      }'
```

```shellscript
curl http://localhost:8081/api/v1/ping
curl http://localhost:8082/api/v1/ping
curl http://localhost:8083/api/v1/ping
```

```plaintext
plugins {
    `java`
    id("org.springframework.boot") version "3.5.7"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}



```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.time.LocalDateTime;

public class CreditCard {

    private Long id;
    private Long userId;
    private String cardNumberHash;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String tokenId;
    private String tokenSignature;
    private LocalDateTime createdAt;

    public CreditCard() {
    }

    public CreditCard(Long id,
                      Long userId,
                      String cardNumberHash,
                      String lastFourDigits,
                      Integer expiryMonth,
                      Integer expiryYear,
                      String tokenId,
                      String tokenSignature,
                      LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.cardNumberHash = cardNumberHash;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.tokenId = tokenId;
        this.tokenSignature = tokenSignature;
        this.createdAt = createdAt;
    }

    public CreditCard(Long userId,
                      String cardNumberHash,
                      String lastFourDigits,
                      Integer expiryMonth,
                      Integer expiryYear) {
        this.userId = userId;
        this.cardNumberHash = cardNumberHash;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumberHash() {
        return cardNumberHash;
    }

    public void setCardNumberHash(String cardNumberHash) {
        this.cardNumberHash = cardNumberHash;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenSignature() {
        return tokenSignature;
    }

    public void setTokenSignature(String tokenSignature) {
        this.tokenSignature = tokenSignature;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;

import java.util.List;
import java.util.Optional;

public interface CreditCardUseCase {

    CreditCard registerCard(Long userId, String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear);

    List<CreditCard> findByUser(Long userId);

    Optional<CreditCard> findByToken(String token);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepositoryPort {

    CreditCard save(CreditCard creditCard);

    Optional<CreditCard> findById(Long id);

    List<CreditCard> findByUserId(Long userId);

    Optional<CreditCard> findByTokenComponents(Long cardId, String tokenId);
}


```

```java
package com.tuempresa.ecommerce.orders.application.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;

@Component
public class CardTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public CardTokenService(@Value("${security.card-token.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public GeneratedToken generate(Long cardId, Long userId) {
        String tokenId = UUID.randomUUID().toString();
        String payload = payload(cardId, userId, tokenId);
        String signature = sign(payload);
        String token = cardId + "." + tokenId + "." + signature;
        return new GeneratedToken(token, tokenId, signature);
    }

    public boolean isValid(Long cardId, Long userId, String tokenId, String providedSignature) {
        String payload = payload(cardId, userId, tokenId);
        String expectedSignature = sign(payload);
        return constantTimeEquals(expectedSignature, providedSignature);
    }

    public TokenComponents parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato de token de tarjeta inválido");
        }
        Long cardId = Long.parseLong(parts[0]);
        String tokenId = parts[1];
        String signature = parts[2];
        return new TokenComponents(cardId, tokenId, signature);
    }

    private String payload(Long cardId, Long userId, String tokenId) {
        return cardId + ":" + userId + ":" + tokenId;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible firmar el token de tarjeta", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public record GeneratedToken(String token, String tokenId, String signature) {
    }

    public record TokenComponents(Long cardId, String tokenId, String signature) {
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class SensitiveDataHasher {

    private final MessageDigest messageDigest;

    public SensitiveDataHasher() {
        try {
            this.messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo inicializar el hash de datos sensibles", e);
        }
    }

    public synchronized String hash(String value) {
        messageDigest.reset();
        byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service.security;

import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final String expectedIssuer;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.issuer:}") String expectedIssuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expectedIssuer = expectedIssuer;
    }

    public Long extractUserId(String authorizationHeader) {
        String jwt = extractBearerToken(authorizationHeader);
        Claims claims = parse(jwt);

        Object userIdClaim = claims.get("userId");
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        if (userIdClaim instanceof String s && !s.isBlank()) {
            return Long.parseLong(s);
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new UnauthorizedAccessException("El token JWT no contiene información de usuario");
        }
        return Long.parseLong(subject);
    }

    private Claims parse(String jwt) {
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .requireIssuer(hasIssuer() ? expectedIssuer : null)
                    .build()
                    .parseClaimsJws(jwt);
            return parsed.getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Token JWT inválido");
        }
    }

    private boolean hasIssuer() {
        return expectedIssuer != null && !expectedIssuer.isBlank();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedAccessException("Token JWT ausente");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException("Formato de token JWT inválido");
        }
        return authorizationHeader.substring("Bearer ".length());
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service.security;

import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final String expectedIssuer;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.issuer:}") String expectedIssuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expectedIssuer = expectedIssuer;
    }

    public Long extractUserId(String authorizationHeader) {
        String jwt = extractBearerToken(authorizationHeader);
        Claims claims = parse(jwt);

        Object userIdClaim = claims.get("userId");
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        if (userIdClaim instanceof String s && !s.isBlank()) {
            return Long.parseLong(s);
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new UnauthorizedAccessException("El token JWT no contiene información de usuario");
        }
        return Long.parseLong(subject);
    }

    private Claims parse(String jwt) {
        try {
            var builder = Jwts.parserBuilder()
                    .setSigningKey(secretKey);
            if (hasIssuer()) {
                builder.requireIssuer(expectedIssuer);
            }
            Jws<Claims> parsed = builder.build().parseClaimsJws(jwt);
            return parsed.getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Token JWT inválido");
        }
    }

    private boolean hasIssuer() {
        return expectedIssuer != null && !expectedIssuer.isBlank();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedAccessException("Token JWT ausente");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException("Formato de token JWT inválido");
        }
        return authorizationHeader.substring("Bearer ".length());
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_cards")
public class CreditCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_hash", nullable = false, length = 128)
    private String cardNumberHash;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFourDigits;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "token_id", nullable = false, unique = true, length = 64)
    private String tokenId;

    @Column(name = "token_signature", nullable = false, length = 128)
    private String tokenSignature;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumberHash() {
        return cardNumberHash;
    }

    public void setCardNumberHash(String cardNumberHash) {
        this.cardNumberHash = cardNumberHash;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenSignature() {
        return tokenSignature;
    }

    public void setTokenSignature(String tokenSignature) {
        this.tokenSignature = tokenSignature;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CreditCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditCardJpaRepository extends JpaRepository<CreditCardEntity, Long> {

    List<CreditCardEntity> findByUserId(Long userId);

    Optional<CreditCardEntity> findByIdAndTokenId(Long id, String tokenId);
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CreditCardEntity;

public class CreditCardMapper {

    private CreditCardMapper() {
    }

    public static CreditCardEntity toEntity(CreditCard creditCard) {
        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(creditCard.getId());
        entity.setUserId(creditCard.getUserId());
        entity.setCardNumberHash(creditCard.getCardNumberHash());
        entity.setLastFourDigits(creditCard.getLastFourDigits());
        entity.setExpiryMonth(creditCard.getExpiryMonth());
        entity.setExpiryYear(creditCard.getExpiryYear());
        entity.setTokenId(creditCard.getTokenId());
        entity.setTokenSignature(creditCard.getTokenSignature());
        entity.setCreatedAt(creditCard.getCreatedAt());
        return entity;
    }

    public static CreditCard toDomain(CreditCardEntity entity) {
        return new CreditCard(
                entity.getId(),
                entity.getUserId(),
                entity.getCardNumberHash(),
                entity.getLastFourDigits(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.getTokenId(),
                entity.getTokenSignature(),
                entity.getCreatedAt()
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.CreditCardEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.CreditCardMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.CreditCardJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CreditCardRepositoryAdapter implements CreditCardRepositoryPort {

    private final CreditCardJpaRepository repository;

    public CreditCardRepositoryAdapter(CreditCardJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CreditCard save(CreditCard creditCard) {
        CreditCardEntity entity = CreditCardMapper.toEntity(creditCard);
        CreditCardEntity saved = repository.save(entity);
        return CreditCardMapper.toDomain(saved);
    }

    @Override
    public Optional<CreditCard> findById(Long id) {
        return repository.findById(id).map(CreditCardMapper::toDomain);
    }

    @Override
    public List<CreditCard> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(CreditCardMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CreditCard> findByTokenComponents(Long cardId, String tokenId) {
        return repository.findByIdAndTokenId(cardId, tokenId)
                .map(CreditCardMapper::toDomain);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.time.LocalDateTime;

public class CreditCard {

    private Long id;
    private Long userId;
    private String cardNumberHash;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String tokenId;
    private String tokenSignature;
    private LocalDateTime createdAt;
    private transient String plainToken;

    public CreditCard() {
    }

    public CreditCard(Long id,
                      Long userId,
                      String cardNumberHash,
                      String lastFourDigits,
                      Integer expiryMonth,
                      Integer expiryYear,
                      String tokenId,
                      String tokenSignature,
                      LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.cardNumberHash = cardNumberHash;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.tokenId = tokenId;
        this.tokenSignature = tokenSignature;
        this.createdAt = createdAt;
    }

    public CreditCard(Long userId,
                      String cardNumberHash,
                      String lastFourDigits,
                      Integer expiryMonth,
                      Integer expiryYear) {
        this.userId = userId;
        this.cardNumberHash = cardNumberHash;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumberHash() {
        return cardNumberHash;
    }

    public void setCardNumberHash(String cardNumberHash) {
        this.cardNumberHash = cardNumberHash;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenSignature() {
        return tokenSignature;
    }

    public void setTokenSignature(String tokenSignature) {
        this.tokenSignature = tokenSignature;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPlainToken() {
        return plainToken;
    }

    public void setPlainToken(String plainToken) {
        this.plainToken = plainToken;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.GeneratedToken;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.TokenComponents;
import com.tuempresa.ecommerce.orders.application.service.security.SensitiveDataHasher;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CreditCardService implements CreditCardUseCase {

    private final CreditCardRepositoryPort repositoryPort;
    private final SensitiveDataHasher hasher;
    private final CardTokenService cardTokenService;

    public CreditCardService(CreditCardRepositoryPort repositoryPort,
                             SensitiveDataHasher hasher,
                             CardTokenService cardTokenService) {
        this.repositoryPort = repositoryPort;
        this.hasher = hasher;
        this.cardTokenService = cardTokenService;
    }

    @Override
    public CreditCard registerCard(Long userId, String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        validateCardData(cardNumber, cvv, expiryMonth, expiryYear);
        String cardHash = hasher.hash(cardNumber + ":" + cvv);
        String lastFour = extractLastFour(cardNumber);

        CreditCard card = new CreditCard(userId, cardHash, lastFour, expiryMonth, expiryYear);
        CreditCard saved = repositoryPort.save(card);

        GeneratedToken generatedToken = cardTokenService.generate(saved.getId(), saved.getUserId());
        saved.setTokenId(generatedToken.tokenId());
        saved.setTokenSignature(generatedToken.signature());
        saved.setPlainToken(generatedToken.token());

        CreditCard updated = repositoryPort.save(saved);
        updated.setPlainToken(generatedToken.token());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCard> findByUser(Long userId) {
        return repositoryPort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditCard> findByToken(String token) {
        TokenComponents components = cardTokenService.parse(token);
        CreditCard card = repositoryPort.findByTokenComponents(components.cardId(), components.tokenId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada para el token proporcionado"));

        boolean validSignature = cardTokenService.isValid(
                card.getId(),
                card.getUserId(),
                card.getTokenId(),
                components.signature()
        );

        if (!validSignature) {
            return Optional.empty();
        }

        return Optional.of(card);
    }

    private void validateCardData(String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        if (cardNumber == null || cardNumber.length() < 12 || cardNumber.length() > 19) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        if (!cardNumber.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("El número de tarjeta debe ser numérico");
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4 || !cvv.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("CVV inválido");
        }
        if (expiryMonth == null || expiryMonth < 1 || expiryMonth > 12) {
            throw new IllegalArgumentException("Mes de expiración inválido");
        }
        if (expiryYear == null || expiryYear < YearMonth.now().getYear()) {
            throw new IllegalArgumentException("Año de expiración inválido");
        }
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        if (expiry.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La tarjeta se encuentra vencida");
        }
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.GeneratedToken;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.TokenComponents;
import com.tuempresa.ecommerce.orders.application.service.security.SensitiveDataHasher;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CreditCardService implements CreditCardUseCase {

    private final CreditCardRepositoryPort repositoryPort;
    private final SensitiveDataHasher hasher;
    private final CardTokenService cardTokenService;

    public CreditCardService(CreditCardRepositoryPort repositoryPort,
                             SensitiveDataHasher hasher,
                             CardTokenService cardTokenService) {
        this.repositoryPort = repositoryPort;
        this.hasher = hasher;
        this.cardTokenService = cardTokenService;
    }

    @Override
    public CreditCard registerCard(Long userId, String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        validateCardData(cardNumber, cvv, expiryMonth, expiryYear);
        String cardHash = hasher.hash(cardNumber + ":" + cvv);
        String lastFour = extractLastFour(cardNumber);

        CreditCard card = new CreditCard(userId, cardHash, lastFour, expiryMonth, expiryYear);
        CreditCard saved = repositoryPort.save(card);

        GeneratedToken generatedToken = cardTokenService.generate(saved.getId(), saved.getUserId());
        saved.setTokenId(generatedToken.tokenId());
        saved.setTokenSignature(generatedToken.signature());
        saved.setPlainToken(generatedToken.token());

        CreditCard updated = repositoryPort.save(saved);
        updated.setPlainToken(generatedToken.token());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCard> findByUser(Long userId) {
        return repositoryPort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditCard> findByToken(String token) {
        TokenComponents components = cardTokenService.parse(token);
        return repositoryPort.findByTokenComponents(components.cardId(), components.tokenId())
                .filter(card -> cardTokenService.isValid(
                        card.getId(),
                        card.getUserId(),
                        card.getTokenId(),
                        components.signature()
                ));
    }

    private void validateCardData(String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        if (cardNumber == null || cardNumber.length() < 12 || cardNumber.length() > 19) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        if (!cardNumber.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("El número de tarjeta debe ser numérico");
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4 || !cvv.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("CVV inválido");
        }
        if (expiryMonth == null || expiryMonth < 1 || expiryMonth > 12) {
            throw new IllegalArgumentException("Mes de expiración inválido");
        }
        if (expiryYear == null || expiryYear < YearMonth.now().getYear()) {
            throw new IllegalArgumentException("Año de expiración inválido");
        }
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        if (expiry.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La tarjeta se encuentra vencida");
        }
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.GeneratedToken;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.TokenComponents;
import com.tuempresa.ecommerce.orders.application.service.security.SensitiveDataHasher;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CreditCardService implements CreditCardUseCase {

    private final CreditCardRepositoryPort repositoryPort;
    private final SensitiveDataHasher hasher;
    private final CardTokenService cardTokenService;

    public CreditCardService(CreditCardRepositoryPort repositoryPort,
                             SensitiveDataHasher hasher,
                             CardTokenService cardTokenService) {
        this.repositoryPort = repositoryPort;
        this.hasher = hasher;
        this.cardTokenService = cardTokenService;
    }

    @Override
    public CreditCard registerCard(Long userId, String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        validateCardData(cardNumber, cvv, expiryMonth, expiryYear);
        String cardHash = hasher.hash(cardNumber + ":" + cvv);
        String lastFour = extractLastFour(cardNumber);

        CreditCard card = new CreditCard(userId, cardHash, lastFour, expiryMonth, expiryYear);
        CreditCard saved = repositoryPort.save(card);

        GeneratedToken generatedToken = cardTokenService.generate(saved.getId(), saved.getUserId());
        saved.setTokenId(generatedToken.tokenId());
        saved.setTokenSignature(generatedToken.signature());
        saved.setPlainToken(generatedToken.token());

        CreditCard updated = repositoryPort.save(saved);
        updated.setPlainToken(generatedToken.token());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCard> findByUser(Long userId) {
        return repositoryPort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditCard> findByToken(String token) {
        TokenComponents components = cardTokenService.parse(token);
        return repositoryPort.findByTokenComponents(components.cardId(), components.tokenId())
                .filter(card -> cardTokenService.isValid(
                        card.getId(),
                        card.getUserId(),
                        card.getTokenId(),
                        components.signature()
                ));
    }

    private void validateCardData(String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        if (cardNumber == null || cardNumber.length() < 12 || cardNumber.length() > 19) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        if (!cardNumber.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("El número de tarjeta debe ser numérico");
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4 || !cvv.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("CVV inválido");
        }
        if (expiryMonth == null || expiryMonth < 1 || expiryMonth > 12) {
            throw new IllegalArgumentException("Mes de expiración inválido");
        }
        if (expiryYear == null || expiryYear < YearMonth.now().getYear()) {
            throw new IllegalArgumentException("Año de expiración inválido");
        }
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        if (expiry.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La tarjeta se encuentra vencida");
        }
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}



```

```yaml
server:
  port: 8083

spring:
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:ordersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

clients:
  catalog:
    base-url: http://localhost:8081/api/v1
  users:
    base-url: http://localhost:8082/api/v1

security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
  card-token:
    secret: change-me-card-token-secret

```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    public enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private Status status;
    private Long creditCardId;
    private String cardTokenId;
    private String cardLastFour;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
    }

    public Payment(Long id,
                   Long orderId,
                   BigDecimal amount,
                   String method,
                   Status status,
                   Long creditCardId,
                   String cardTokenId,
                   String cardLastFour,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Payment(Long orderId, BigDecimal amount, String method, Long creditCardId, String cardTokenId, String cardLastFour) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getCardTokenId() {
        return cardTokenId;
    }

    public void setCardTokenId(String cardTokenId) {
        this.cardTokenId = cardTokenId;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void markCompleted() {
        this.status = Status.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "method", nullable = false)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Payment.Status status;

    @Column(name = "credit_card_id")
    private Long creditCardId;

    @Column(name = "card_token_id", length = 64)
    private String cardTokenId;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PaymentEntity() {
    }

    public PaymentEntity(Long id,
                         Long orderId,
                         BigDecimal amount,
                         String method,
                         Payment.Status status,
                         Long creditCardId,
                         String cardTokenId,
                         String cardLastFour,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Payment.Status getStatus() {
        return status;
    }

    public void setStatus(Payment.Status status) {
        this.status = status;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getCardTokenId() {
        return cardTokenId;
    }

    public void setCardTokenId(String cardTokenId) {
        this.cardTokenId = cardTokenId;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;

public class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setOrderId(payment.getOrderId());
        entity.setAmount(payment.getAmount());
        entity.setMethod(payment.getMethod());
        entity.setStatus(payment.getStatus());
        entity.setCreditCardId(payment.getCreditCardId());
        entity.setCardTokenId(payment.getCardTokenId());
        entity.setCardLastFour(payment.getCardLastFour());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getMethod(),
                entity.getStatus(),
                entity.getCreditCardId(),
                entity.getCardTokenId(),
                entity.getCardLastFour(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateCreditCardRequest {

    @NotBlank
    @Pattern(regexp = "\\d{12,19}", message = "El número de tarjeta debe contener entre 12 y 19 dígitos")
    private String cardNumber;

    @NotBlank
    @Pattern(regexp = "\\d{3,4}", message = "El CVV debe contener 3 o 4 dígitos")
    private String cvv;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer expiryMonth;

    @NotNull
    private Integer expiryYear;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import java.time.LocalDateTime;

public class CreditCardResponse {

    private Long id;
    private String token;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
    private LocalDateTime createdAt;

    public CreditCardResponse() {
    }

    public CreditCardResponse(Long id,
                              String token,
                              String lastFourDigits,
                              Integer expiryMonth,
                              Integer expiryYear,
                              LocalDateTime createdAt) {
        this.id = id;
        this.token = token;
        this.lastFourDigits = lastFourDigits;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreditCardResponse;

public class CreditCardWebMapper {

    private CreditCardWebMapper() {
    }

    public static CreditCardResponse toResponse(CreditCard card) {
        return new CreditCardResponse(
                card.getId(),
                resolveToken(card),
                card.getLastFourDigits(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getCreatedAt()
        );
    }

    private static String resolveToken(CreditCard card) {
        if (card.getPlainToken() != null) {
            return card.getPlainToken();
        }
        if (card.getId() == null || card.getTokenId() == null || card.getTokenSignature() == null) {
            return null;
        }
        return card.getId() + "." + card.getTokenId() + "." + card.getTokenSignature();
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.application.service.security.JwtService;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCreditCardRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreditCardResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CreditCardWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CreditCardController {

    private final CreditCardUseCase creditCardUseCase;
    private final JwtService jwtService;

    public CreditCardController(CreditCardUseCase creditCardUseCase, JwtService jwtService) {
        this.creditCardUseCase = creditCardUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<CreditCardResponse> registerCard(@RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreateCreditCardRequest request) {
        Long userId = jwtService.extractUserId(authorization);
        CreditCard card = creditCardUseCase.registerCard(
                userId,
                request.getCardNumber(),
                request.getCvv(),
                request.getExpiryMonth(),
                request.getExpiryYear()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CreditCardWebMapper.toResponse(card));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> listUserCards(@RequestHeader("Authorization") String authorization) {
        Long userId = jwtService.extractUserId(authorization);
        List<CreditCardResponse> responses = creditCardUseCase.findByUser(userId).stream()
                .map(CreditCardWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull
    private Long orderId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @NotBlank
    private String method;

    @NotBlank
    private String cardToken;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(Long orderId, BigDecimal amount, String method, String cardToken) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.cardToken = cardToken;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String cardLastFour;
    private Payment.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id,
                           Long orderId,
                           BigDecimal amount,
                           String method,
                           String cardLastFour,
                           Payment.Status status,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.cardLastFour = cardLastFour;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public Payment.Status getStatus() {
        return status;
    }

    public void setStatus(Payment.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;

public class PaymentWebMapper {

    private PaymentWebMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getCardLastFour(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

import java.math.BigDecimal;

public interface PaymentUseCase {

    Payment registerPayment(Long userId, Long orderId, BigDecimal amount, String method, String cardToken);

    Payment markAsCompleted(Long paymentId);

    Payment markAsFailed(Long paymentId);

    Payment findById(Long paymentId);
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final CreditCardUseCase creditCardUseCase;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort,
                          OrderRepositoryPort orderRepositoryPort,
                          CreditCardUseCase creditCardUseCase) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.creditCardUseCase = creditCardUseCase;
    }

    @Override
    public Payment registerPayment(Long userId,
                                   Long orderId,
                                   BigDecimal amount,
                                   String method,
                                   String cardToken) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("El usuario autenticado no coincide con la orden");
        }

        CreditCard creditCard = creditCardUseCase.findByToken(cardToken)
                .orElseThrow(() -> new UnauthorizedAccessException("Token de tarjeta inválido"));

        if (!creditCard.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("La tarjeta no pertenece al usuario autenticado");
        }

        ensureCardNotExpired(creditCard);

        Payment payment = new Payment(
                orderId,
                amount,
                method,
                creditCard.getId(),
                creditCard.getTokenId(),
                creditCard.getLastFourDigits()
        );

        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    public Payment markAsCompleted(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markCompleted();
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));
        order.markAsPaid();
        orderRepositoryPort.save(order);

        return savedPayment;
    }

    @Override
    public Payment markAsFailed(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markFailed();
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long paymentId) {
        return getOrThrow(paymentId);
    }

    private Payment getOrThrow(Long paymentId) {
        return paymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
    }

    private void ensureCardNotExpired(CreditCard creditCard) {
        YearMonth expiry = YearMonth.of(creditCard.getExpiryYear(), creditCard.getExpiryMonth());
        if (expiry.isBefore(YearMonth.now())) {
            throw new UnauthorizedAccessException("La tarjeta se encuentra vencida");
        }
    }
}



```

```java
*** Begin Patch
*** Update File: /Users/gebrayel/Documents/Personal-Projects/springboot-test/ecommerce-simulator/services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/controller/PaymentController.java
@@
-package com.tuempresa.ecommerce.orders.infrastructure.web.controller;
-
-import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
-import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
-import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;
-import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.PaymentWebMapper;
-import jakarta.validation.Valid;
-import org.springframework.http.HttpStatus;
-import org.springframework.http.ResponseEntity;
-import org.springframework.web.bind.annotation.GetMapping;
-import org.springframework.web.bind.annotation.PathVariable;
-import org.springframework.web.bind.annotation.PostMapping;
-import org.springframework.web.bind.annotation.RequestBody;
-import org.springframework.web.bind.annotation.RequestMapping;
-import org.springframework.web.bind.annotation.RestController;
-
-@RestController
-@RequestMapping("/payments")
-public class PaymentController {
-
-    private final PaymentUseCase paymentUseCase;
-
-    public PaymentController(PaymentUseCase paymentUseCase) {
-        this.paymentUseCase = paymentUseCase;
-    }
-
-    @PostMapping
-    public ResponseEntity<PaymentResponse> registerPayment(@Valid @RequestBody CreatePaymentRequest request) {
-        return ResponseEntity
-                .status(HttpStatus.CREATED)
-                .body(PaymentWebMapper.toResponse(paymentUseCase.registerPayment(PaymentWebMapper.toDomain(request))));
-    }
-
-    @PostMapping("/{paymentId}/complete")
-    public ResponseEntity<PaymentResponse> markAsCompleted(@PathVariable Long paymentId) {
-        return ResponseEntity.ok(
-                PaymentWebMapper.toResponse(paymentUseCase.markAsCompleted(paymentId))
-        );
-    }
-
-    @PostMapping("/{paymentId}/fail")
-    public ResponseEntity<PaymentResponse> markAsFailed(@PathVariable Long paymentId) {
-        return ResponseEntity.ok(
-                PaymentWebMapper.toResponse(paymentUseCase.markAsFailed(paymentId))
-        );
-    }
-
-    @GetMapping("/{paymentId}")
-    public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
-        return ResponseEntity.ok(
-                PaymentWebMapper.toResponse(paymentUseCase.findById(paymentId))
-        );
-    }
-}
-
-
-
*** End Patch
```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.application.service.security.JwtService;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;
    private final JwtService jwtService;

    public PaymentController(PaymentUseCase paymentUseCase, JwtService jwtService) {
        this.paymentUseCase = paymentUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> registerPayment(@RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreatePaymentRequest request) {
        Long userId = jwtService.extractUserId(authorization);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentWebMapper.toResponse(
                        paymentUseCase.registerPayment(
                                userId,
                                request.getOrderId(),
                                request.getAmount(),
                                request.getMethod(),
                                request.getCardToken()
                        )
                ));
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponse> markAsCompleted(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsCompleted(paymentId))
        );
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> markAsFailed(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsFailed(paymentId))
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.findById(paymentId))
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.GeneratedToken;
import com.tuempresa.ecommerce.orders.application.service.security.CardTokenService.TokenComponents;
import com.tuempresa.ecommerce.orders.application.service.security.SensitiveDataHasher;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CreditCardRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CreditCardService implements CreditCardUseCase {

    private final CreditCardRepositoryPort repositoryPort;
    private final SensitiveDataHasher hasher;
    private final CardTokenService cardTokenService;

    public CreditCardService(CreditCardRepositoryPort repositoryPort,
                             SensitiveDataHasher hasher,
                             CardTokenService cardTokenService) {
        this.repositoryPort = repositoryPort;
        this.hasher = hasher;
        this.cardTokenService = cardTokenService;
    }

    @Override
    public CreditCard registerCard(Long userId, String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        validateCardData(cardNumber, cvv, expiryMonth, expiryYear);
        String cardHash = hasher.hash(cardNumber + ":" + cvv);
        String lastFour = extractLastFour(cardNumber);

        CreditCard card = new CreditCard(userId, cardHash, lastFour, expiryMonth, expiryYear);
        CreditCard saved = repositoryPort.save(card);

        GeneratedToken generatedToken = cardTokenService.generate(saved.getId(), saved.getUserId());
        saved.setTokenId(generatedToken.tokenId());
        saved.setTokenSignature(generatedToken.signature());
        saved.setPlainToken(generatedToken.token());

        CreditCard updated = repositoryPort.save(saved);
        updated.setPlainToken(generatedToken.token());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditCard> findByUser(Long userId) {
        return repositoryPort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreditCard> findByToken(String token) {
        TokenComponents components;
        try {
            components = cardTokenService.parse(token);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        return repositoryPort.findByTokenComponents(components.cardId(), components.tokenId())
                .filter(card -> cardTokenService.isValid(
                        card.getId(),
                        card.getUserId(),
                        card.getTokenId(),
                        components.signature()
                ));
    }

    private void validateCardData(String cardNumber, String cvv, Integer expiryMonth, Integer expiryYear) {
        if (cardNumber == null || cardNumber.length() < 12 || cardNumber.length() > 19) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }
        if (!cardNumber.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("El número de tarjeta debe ser numérico");
        }
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4 || !cvv.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("CVV inválido");
        }
        if (expiryMonth == null || expiryMonth < 1 || expiryMonth > 12) {
            throw new IllegalArgumentException("Mes de expiración inválido");
        }
        if (expiryYear == null || expiryYear < YearMonth.now().getYear()) {
            throw new IllegalArgumentException("Año de expiración inválido");
        }
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        if (expiry.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La tarjeta se encuentra vencida");
        }
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedAccessException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}


```

## Resumen
- Añadí la gestión completa de tarjetas en `orders-service`: registro/listado autenticado por JWT y tokens firmados vía HMAC para detectar manipulaciones (`CardTokenService`) y persistencia propia (`CreditCard` + repositorio JPA).  
```19:54:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/controller/CreditCardController.java
@RestController
@RequestMapping("/cards")
public class CreditCardController {
    // ...
    public ResponseEntity<CreditCardResponse> registerCard(@RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreateCreditCardRequest request) {
        Long userId = jwtService.extractUserId(authorization);
        CreditCard card = creditCardUseCase.registerCard(
                userId,
                request.getCardNumber(),
                request.getCvv(),
                request.getExpiryMonth(),
                request.getExpiryYear()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CreditCardWebMapper.toResponse(card));
    }
}
```
- Los tokens de tarjeta ahora se generan y validan con firma HMAC (`cardId.tokenId.signature`), evitando suplantaciones y almacenando únicamente hashes de PAN/CVV.  
```13:79:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/application/service/security/CardTokenService.java
@Component
public class CardTokenService {
    // ...
    public GeneratedToken generate(Long cardId, Long userId) {
        String tokenId = UUID.randomUUID().toString();
        String payload = payload(cardId, userId, tokenId);
        String signature = sign(payload);
        String token = cardId + "." + tokenId + "." + signature;
        return new GeneratedToken(token, tokenId, signature);
    }
    public boolean isValid(Long cardId, Long userId, String tokenId, String providedSignature) {
        String payload = payload(cardId, userId, tokenId);
        String expectedSignature = sign(payload);
        return constantTimeEquals(expectedSignature, providedSignature);
    }
}
```
- El flujo de pagos exige JWT + token de tarjeta; valida que la orden y la tarjeta pertenezcan al mismo usuario y que la tarjeta no haya expirado antes de registrar el pago.  
```35:109:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/application/service/PaymentService.java
public Payment registerPayment(Long userId,
                               Long orderId,
                               BigDecimal amount,
                               String method,
                               String cardToken) {
    Order order = orderRepositoryPort.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

    if (!order.getUserId().equals(userId)) {
        throw new UnauthorizedAccessException("El usuario autenticado no coincide con la orden");
    }

    CreditCard creditCard = creditCardUseCase.findByToken(cardToken)
            .orElseThrow(() -> new UnauthorizedAccessException("Token de tarjeta inválido"));

    if (!creditCard.getUserId().equals(userId)) {
        throw new UnauthorizedAccessException("La tarjeta no pertenece al usuario autenticado");
    }

    ensureCardNotExpired(creditCard);

    Payment payment = new Payment(
            orderId,
            amount,
            method,
            creditCard.getId(),
            creditCard.getTokenId(),
            creditCard.getLastFourDigits()
    );

    payment.setCreatedAt(LocalDateTime.now());
    payment.setUpdatedAt(LocalDateTime.now());
    return paymentRepositoryPort.save(payment);
}
```
- Configuré `application.yaml` para centralizar `/api/v1`, URLs de catálogo/usuarios y nuevos secretos para JWT + tokens de tarjeta.  
```1:39:services/orders-service/src/main/resources/application.yaml
spring:
  mvc:
    servlet:
      path: /api/v1
# ...
clients:
  catalog:
    base-url: http://localhost:8081/api/v1
  users:
    base-url: http://localhost:8082/api/v1

security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
  card-token:
    secret: change-me-card-token-secret
```

## Pruebas
- No se ejecutó `./gradlew :services:orders-service:build` porque la sandbox no tiene un JRE disponible. Ejecuta el build localmente una vez configurado Java.

## Pasos sugeridos de verificación manual
1. (Con JWT válido para el usuario) registrar una tarjeta:
   ```bash
   curl -X POST http://localhost:8083/api/v1/cards \
     -H "Authorization: Bearer <JWT>" \
     -H "Content-Type: application/json" \
     -d '{ "cardNumber": "4111111111111111", "cvv": "123", "expiryMonth": 12, "expiryYear": 2030 }'
   ```
   Guarda el `token` devuelto.
2. Listar tarjetas del usuario:
   ```bash
   curl -H "Authorization: Bearer <JWT>" http://localhost:8083/api/v1/cards
   ```
3. Registrar un pago usando el token de tarjeta:
   ```bash
   curl -X POST http://localhost:8083/api/v1/payments \
     -H "Authorization: Bearer <JWT>" \
     -H "Content-Type: application/json" \
     -d '{ "orderId": 1, "amount": 100.00, "method": "CREDIT_CARD", "cardToken": "<token>" }'
   ```

Reemplaza los secretos en `application.yaml` por valores seguros antes de desplegar.

```java
@RestController
@RequestMapping("/cards")
public class CreditCardController {
    // ...
    public ResponseEntity<CreditCardResponse> registerCard(@RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreateCreditCardRequest request) {
        Long userId = jwtService.extractUserId(authorization);
        CreditCard card = creditCardUseCase.registerCard(
                userId,
                request.getCardNumber(),
                request.getCvv(),
                request.getExpiryMonth(),
                request.getExpiryYear()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CreditCardWebMapper.toResponse(card));
    }
}
```

```java
@Component
public class CardTokenService {
    // ...
    public GeneratedToken generate(Long cardId, Long userId) {
        String tokenId = UUID.randomUUID().toString();
        String payload = payload(cardId, userId, tokenId);
        String signature = sign(payload);
        String token = cardId + "." + tokenId + "." + signature;
        return new GeneratedToken(token, tokenId, signature);
    }
    public boolean isValid(Long cardId, Long userId, String tokenId, String providedSignature) {
        String payload = payload(cardId, userId, tokenId);
        String expectedSignature = sign(payload);
        return constantTimeEquals(expectedSignature, providedSignature);
    }
}
```

```java
public Payment registerPayment(Long userId,
                               Long orderId,
                               BigDecimal amount,
                               String method,
                               String cardToken) {
    Order order = orderRepositoryPort.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

    if (!order.getUserId().equals(userId)) {
        throw new UnauthorizedAccessException("El usuario autenticado no coincide con la orden");
    }

    CreditCard creditCard = creditCardUseCase.findByToken(cardToken)
            .orElseThrow(() -> new UnauthorizedAccessException("Token de tarjeta inválido"));

    if (!creditCard.getUserId().equals(userId)) {
        throw new UnauthorizedAccessException("La tarjeta no pertenece al usuario autenticado");
    }

    ensureCardNotExpired(creditCard);

    Payment payment = new Payment(
            orderId,
            amount,
            method,
            creditCard.getId(),
            creditCard.getTokenId(),
            creditCard.getLastFourDigits()
    );

    payment.setCreatedAt(LocalDateTime.now());
    payment.setUpdatedAt(LocalDateTime.now());
    return paymentRepositoryPort.save(payment);
}
```

```yaml
spring:
  mvc:
    servlet:
      path: /api/v1
# ...
clients:
  catalog:
    base-url: http://localhost:8081/api/v1
  users:
    base-url: http://localhost:8082/api/v1

security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
  card-token:
    secret: change-me-card-token-secret
```

```shellscript
   curl -X POST http://localhost:8083/api/v1/cards \
     -H "Authorization: Bearer <JWT>" \
     -H "Content-Type: application/json" \
     -d '{ "cardNumber": "4111111111111111", "cvv": "123", "expiryMonth": 12, "expiryYear": 2030 }'
```

```shellscript
   curl -H "Authorization: Bearer <JWT>" http://localhost:8083/api/v1/cards
```

```shellscript
   curl -X POST http://localhost:8083/api/v1/payments \
     -H "Authorization: Bearer <JWT>" \
     -H "Content-Type: application/json" \
     -d '{ "orderId": 1, "amount": 100.00, "method": "CREDIT_CARD", "cardToken": "<token>" }'
```

---

**User**

puedes hacer en el endpoint de user un endpoint login para iniciar sesion, que al poner usuario y contraseña retorne el token que necesitaría para las tarjetas, y agregar en el registro la password y guardarla cifrada en la DB

---

**Cursor**

```plaintext
plugins {
    `java`
    id("org.springframework.boot") version "3.5.7"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


```

```java
package com.tuempresa.ecommerce.users.domain.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String email;
    private String name;
    private String telefono;
    private String direccion;
    private String passwordHash;
    private transient String rawPassword;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id,
                String email,
                String name,
                String telefono,
                String direccion,
                String passwordHash,
                LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public User(String email,
                String name,
                String telefono,
                String direccion) {
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String telefono;

    @Column(nullable = false)
    private String direccion;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UserEntity() {
    }

    public UserEntity(Long id,
                      String email,
                      String name,
                      String telefono,
                      String direccion,
                      String passwordHash,
                      LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;

public class UserEntityMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getName(),
            entity.getTelefono(),
            entity.getDireccion(),
            entity.getPasswordHash(),
            entity.getCreatedAt()
        );
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity(
            domain.getId(),
            domain.getEmail(),
            domain.getName(),
            domain.getTelefono(),
            domain.getDireccion(),
            domain.getPasswordHash(),
            domain.getCreatedAt()
        );
        return entity;
    }
}


```

```java
package com.tuempresa.ecommerce.users.domain.port.out;

import com.tuempresa.ecommerce.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    User save(User user);
    void deleteById(Long id);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;
import com.tuempresa.ecommerce.users.infrastructure.persistence.mapper.UserEntityMapper;
import com.tuempresa.ecommerce.users.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntityMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return UserEntityMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        return userJpaRepository.existsByTelefono(telefono);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userJpaRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsByTelefonoAndIdNot(String telefono, Long id) {
        return userJpaRepository.existsByTelefonoAndIdNot(telefono, id);
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
    Optional<UserEntity> findByEmail(String email);
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.mapper;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserRequest;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserResponse;

public class UserWebMapper {

    public static User toDomain(UserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User(
            request.getEmail(),
            request.getName(),
            request.getTelefono(),
            request.getDireccion()
        );
        user.setRawPassword(request.getPassword());
        return user;
    }

    public static UserResponse toResponse(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserResponse(
            domain.getId(),
            domain.getName(),
            domain.getEmail(),
            domain.getTelefono(),
            domain.getDireccion(),
            domain.getCreatedAt()
        );
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos y puede iniciar con +")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String telefono, String direccion, String password) {
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos y puede iniciar con +")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @jakarta.validation.constraints.Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String telefono, String direccion, String password) {
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


```

```java
package com.tuempresa.ecommerce.users.application.service.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashService {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}


```

```java
package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.application.service.security.PasswordHashService;
import com.tuempresa.ecommerce.users.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.users.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.UserUseCase;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHashService passwordHashService;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordHashService passwordHashService) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHashService = passwordHashService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public User create(User user) {
        // Validar que email y telefono sean únicos
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("El email ya está registrado: " + user.getEmail());
        }
        if (userRepositoryPort.existsByTelefono(user.getTelefono())) {
            throw new DuplicateResourceException("El teléfono ya está registrado: " + user.getTelefono());
        }
        if (user.getRawPassword() == null || user.getRawPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        user.setPasswordHash(passwordHashService.hash(user.getRawPassword()));
        user.setRawPassword(null);
        return userRepositoryPort.save(user);
    }

    @Override
    public User update(Long id, User user) {
        Optional<User> existingUserOpt = userRepositoryPort.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        User existingUser = existingUserOpt.get();

        // Validar que email sea único (excepto el usuario actual)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepositoryPort.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new DuplicateResourceException("El email ya está registrado: " + user.getEmail());
        }

        // Validar que telefono sea único (excepto el usuario actual)
        if (!existingUser.getTelefono().equals(user.getTelefono()) && 
            userRepositoryPort.existsByTelefonoAndIdNot(user.getTelefono(), id)) {
            throw new DuplicateResourceException("El teléfono ya está registrado: " + user.getTelefono());
        }

        // Actualizar campos
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setTelefono(user.getTelefono());
        existingUser.setDireccion(user.getDireccion());
        if (user.getRawPassword() != null && !user.getRawPassword().isBlank()) {
            existingUser.setPasswordHash(passwordHashService.hash(user.getRawPassword()));
        }

        return userRepositoryPort.save(existingUser);
    }

    @Override
    public void delete(Long id) {
        if (userRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepositoryPort.deleteById(id);
    }
}


```

```java
package com.tuempresa.ecommerce.users.application.service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long expirationMinutes;

    public JwtTokenService(@Value("${security.jwt.secret}") String secret,
                           @Value("${security.jwt.issuer:ecommerce-simulator}") String issuer,
                           @Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(Long userId, String email, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .addClaims(extraClaims)
                .claim("userId", userId)
                .claim("email", email)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}


```

```java
package com.tuempresa.ecommerce.users.domain.port.in;

import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginResult;

public interface AuthUseCase {

    LoginResult login(LoginCommand command);
}


```

```java
package com.tuempresa.ecommerce.users.domain.port.in.dto;

public class LoginCommand {

    private final String email;
    private final String password;

    public LoginCommand(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}


```

```java
package com.tuempresa.ecommerce.users.domain.port.in.dto;

public class LoginResult {

    private final Long userId;
    private final String email;
    private final String token;

    public LoginResult(Long userId, String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}


```

```java
package com.tuempresa.ecommerce.users.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}


```

```java
package com.tuempresa.ecommerce.users.application.service;

import com.tuempresa.ecommerce.users.application.service.security.JwtTokenService;
import com.tuempresa.ecommerce.users.application.service.security.PasswordHashService;
import com.tuempresa.ecommerce.users.domain.exception.InvalidCredentialsException;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.AuthUseCase;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginResult;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional(readOnly = true)
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHashService passwordHashService;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepositoryPort userRepositoryPort,
                       PasswordHashService passwordHashService,
                       JwtTokenService jwtTokenService) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHashService = passwordHashService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (user.getPasswordHash() == null ||
                !passwordHashService.matches(command.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = jwtTokenService.generateToken(
                user.getId(),
                user.getEmail(),
                Collections.emptyMap()
        );

        return new LoginResult(user.getId(), user.getEmail(), token);
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.tuempresa.ecommerce.users.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.users.domain.exception.InvalidCredentialsException;
import com.tuempresa.ecommerce.users.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateResourceException(DuplicateResourceException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

public class LoginResponse {

    private Long userId;
    private String email;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(Long userId, String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.tuempresa.ecommerce.users.domain.port.in.AuthUseCase;
import com.tuempresa.ecommerce.users.domain.port.in.dto.LoginCommand;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.LoginRequest;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authUseCase.login(new LoginCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.ok(new LoginResponse(result.getUserId(), result.getEmail(), result.getToken()));
    }
}


```

```yaml
server:
  port: 8082

spring:
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:usersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
    expiration-minutes: 120

```

```java
*** Begin Patch
*** Update File: /Users/gebrayel/Documents/Personal-Projects/springboot-test/ecommerce-simulator/services/users-service/src/main/java/com/tuempresa/ecommerce/users/infrastructure/web/dto/UserRequest.java
@@
-import jakarta.validation.constraints.Pattern;
+import jakarta.validation.constraints.Pattern;
+import jakarta.validation.constraints.Size;
@@
-    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
     private String password;
*** End Patch
```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos y puede iniciar con +")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String telefono, String direccion, String password) {
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.tuempresa.ecommerce.users.domain.exception.DuplicateResourceException;
import com.tuempresa.ecommerce.users.domain.exception.InvalidCredentialsException;
import com.tuempresa.ecommerce.users.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateResourceException(DuplicateResourceException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}


```

## Cambios Clave
- **Usuarios con contraseña**: `UserRequest` ahora recibe un password (mínimo 8 caracteres). El dominio `User` almacena `passwordHash` (BCrypt) y `rawPassword` efímero; el `UserService` valida unicidad, exige contraseña al registrar y permite cambiarla opcionalmente al actualizar. El mapper/entity/ repositorios fueron ajustados para persistir `password_hash`.
- **Autenticación JWT**: Nuevo `AuthService` genera tokens firmados (JJWT) usando el secreto compartido (`security.jwt.secret`). `AuthController` (ruta `POST /api/v1/auth/login`) recibe email/password y devuelve `LoginResponse` con token. Se manejan errores con `InvalidCredentialsException` → HTTP 401.
- **Infraestructura**: Se añadieron utilidades (`PasswordHashService`, `JwtTokenService`) y dependencias (`spring-security-crypto`, `jjwt`). `application.yaml` de users incluye configuración `security.jwt` alineada con `orders-service`.

## Endpoint de Login (Sample)
1. Registrar usuario (requiere password):
   ```bash
   curl -X POST http://localhost:8082/api/v1/users \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Ana Tester",
           "email": "ana@example.com",
           "telefono": "+34600111222",
           "direccion": "Calle Falsa 123, Madrid",
           "password": "Secreta123"
         }'
   ```
2. Obtener token:
   ```bash
   curl -X POST http://localhost:8082/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{
           "email": "ana@example.com",
           "password": "Secreta123"
         }'
   ```
   → Respuesta:
   ```json
   {
     "userId": 1,
     "email": "ana@example.com",
     "token": "eyJhbGciOiJIUzI1NiIs..."
   }
   ```
3. Usa ese `Bearer <token>` en los endpoints de `orders-service` que requieren autenticación (registro de tarjetas, pagos, etc.).

## Consideraciones
- Cambia los secretos por valores seguros antes de desplegar.
- Se necesita migrar/construir la tabla `users` para añadir `password_hash` (H2 lo hace con `ddl-auto: update`).
- El build no se ejecutó aquí (falta JDK). Compila localmente con `./gradlew :services:users-service:build` cuando tengas Java disponible.

```shellscript
   curl -X POST http://localhost:8082/api/v1/users \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Ana Tester",
           "email": "ana@example.com",
           "telefono": "+34600111222",
           "direccion": "Calle Falsa 123, Madrid",
           "password": "Secreta123"
         }'
```

```shellscript
   curl -X POST http://localhost:8082/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{
           "email": "ana@example.com",
           "password": "Secreta123"
         }'
```

```json
   {
     "userId": 1,
     "email": "ana@example.com",
     "token": "eyJhbGciOiJIUzI1NiIs..."
   }
```

---

**User**

te voy a pedir unos cambios mas, primero al crear la orden, solicita una direccion de entrega, que no necesariamente es la de registro del cliente, y hazme un modelo de settings para el orders service, donde pueda configurar una probabilidad de rechazo de la tarjeta que se use al pagar, y un numero de reintentos que tambien se use al pagar, para que reintente el pago N veces, cada vez con un aleatorio diferente que debe entrar en la probabilidad del setting, y registrar cuantas veces intento el pago al generar la orden

---

**Cursor**

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    public enum Status {
        CREATED,
        PAID,
        SHIPPED,
        COMPLETED,
        CANCELLED
    }

    private Long id;
    private Long userId;
    private String deliveryAddress;
    private List<OrderItem> items;
    private BigDecimal total;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
        this.status = Status.CREATED;
    }

    public Order(Long id,
                 Long userId,
                 String deliveryAddress,
                 List<OrderItem> items,
                 BigDecimal total,
                 Status status,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.deliveryAddress = deliveryAddress;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Order(Long userId, String deliveryAddress, List<OrderItem> items, BigDecimal total) {
        this.userId = userId;
        this.deliveryAddress = deliveryAddress;
        this.items = new ArrayList<>(items);
        this.total = total;
        this.status = Status.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void markAsPaid() {
        this.status = Status.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        this.total = this.items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItemEmbeddable> items = new ArrayList<>();

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Order.Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OrderEntity() {
    }

    public OrderEntity(Long id,
                       Long userId,
                       String deliveryAddress,
                       List<OrderItemEmbeddable> items,
                       BigDecimal total,
                       Order.Status status,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<OrderItemEmbeddable> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEmbeddable> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Order.Status getStatus() {
        return status;
    }

    public void setStatus(Order.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderItemEmbeddable;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setUserId(order.getUserId());
        entity.setDeliveryAddress(order.getDeliveryAddress());
        entity.setItems(toEmbeddableList(order.getItems()));
        entity.setTotal(order.getTotal());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getUserId(),
                entity.getDeliveryAddress(),
                toDomainList(entity.getItems()),
                entity.getTotal(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static List<OrderItemEmbeddable> toEmbeddableList(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemEmbeddable(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }

    private static List<OrderItem> toDomainList(List<OrderItemEmbeddable> items) {
        return items.stream()
                .map(item -> new OrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}



```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.Order;

public interface OrderUseCase {

    Order createFromCart(Long cartId, String deliveryAddress);

    Order findById(Long orderId);

    Order markAsPaid(Long orderId);

    Order cancel(Long orderId);
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.InvalidOrderStateException;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort, CartRepositoryPort cartRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Order createFromCart(Long cartId, String deliveryAddress) {
        Cart cart = cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));

        if (cart.getItems().isEmpty()) {
            throw new InvalidOrderStateException("No se puede crear una orden con un carrito vacío");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(cart.getUserId(), deliveryAddress, orderItems, total);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepositoryPort.save(order);

        cart.clearItems();
        cartRepositoryPort.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));
    }

    @Override
    public Order markAsPaid(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede marcar como pagada una orden cancelada");
        }
        order.markAsPaid();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    @Override
    public Order cancel(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.PAID.equals(order.getStatus()) || Order.Status.COMPLETED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede cancelar una orden pagada o completada");
        }
        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    private OrderItem toOrderItem(CartItem cartItem) {
        return new OrderItem(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.InvalidOrderStateException;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort, CartRepositoryPort cartRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Order createFromCart(Long cartId, String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }

        Cart cart = cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));

        if (cart.getItems().isEmpty()) {
            throw new InvalidOrderStateException("No se puede crear una orden con un carrito vacío");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(cart.getUserId(), deliveryAddress, orderItems, total);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepositoryPort.save(order);

        cart.clearItems();
        cartRepositoryPort.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));
    }

    @Override
    public Order markAsPaid(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede marcar como pagada una orden cancelada");
        }
        order.markAsPaid();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    @Override
    public Order cancel(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.PAID.equals(order.getStatus()) || Order.Status.COMPLETED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede cancelar una orden pagada o completada");
        }
        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    private OrderItem toOrderItem(CartItem cartItem) {
        return new OrderItem(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull
    private Long cartId;

    @NotBlank
    private String deliveryAddress;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long cartId, String deliveryAddress) {
        this.cartId = cartId;
        this.deliveryAddress = deliveryAddress;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateOrderRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.OrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderWebMapper.toResponse(orderUseCase.createFromCart(request.getCartId(), request.getDeliveryAddress())));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> markAsPaid(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.markAsPaid(orderId))
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.cancel(orderId))
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                OrderWebMapper.toResponse(orderUseCase.findById(orderId))
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import com.tuempresa.ecommerce.orders.domain.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long userId;
    private String deliveryAddress;
    private List<OrderItemResponse> items;
    private BigDecimal total;
    private Order.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponse() {
    }

    public OrderResponse(Long id,
                         Long userId,
                         String deliveryAddress,
                         List<OrderItemResponse> items,
                         BigDecimal total,
                         Order.Status status,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Order.Status getStatus() {
        return status;
    }

    public void setStatus(Order.Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderItemResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;

public class OrderWebMapper {

    private OrderWebMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getDeliveryAddress(),
                toItemResponses(order.getItems()),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
    }
}



```

```java
package com.tuempresa.ecommerce.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    public enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private Status status;
    private Long creditCardId;
    private String cardTokenId;
    private String cardLastFour;
    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
    }

    public Payment(Long id,
                   Long orderId,
                   BigDecimal amount,
                   String method,
                   Status status,
                   Long creditCardId,
                   String cardTokenId,
                   String cardLastFour,
                   int attempts,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Payment(Long orderId, BigDecimal amount, String method, Long creditCardId, String cardTokenId, String cardLastFour) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.status = Status.PENDING;
        this.attempts = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getCardTokenId() {
        return cardTokenId;
    }

    public void setCardTokenId(String cardTokenId) {
        this.cardTokenId = cardTokenId;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void markCompleted() {
        this.status = Status.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "method", nullable = false)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Payment.Status status;

    @Column(name = "credit_card_id")
    private Long creditCardId;

    @Column(name = "card_token_id", length = 64)
    private String cardTokenId;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PaymentEntity() {
    }

    public PaymentEntity(Long id,
                         Long orderId,
                         BigDecimal amount,
                         String method,
                         Payment.Status status,
                         Long creditCardId,
                         String cardTokenId,
                         String cardLastFour,
                         Integer attempts,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.creditCardId = creditCardId;
        this.cardTokenId = cardTokenId;
        this.cardLastFour = cardLastFour;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Payment.Status getStatus() {
        return status;
    }

    public void setStatus(Payment.Status status) {
        this.status = status;
    }

    public Long getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(Long creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getCardTokenId() {
        return cardTokenId;
    }

    public void setCardTokenId(String cardTokenId) {
        this.cardTokenId = cardTokenId;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.PaymentEntity;

public class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setOrderId(payment.getOrderId());
        entity.setAmount(payment.getAmount());
        entity.setMethod(payment.getMethod());
        entity.setStatus(payment.getStatus());
        entity.setCreditCardId(payment.getCreditCardId());
        entity.setCardTokenId(payment.getCardTokenId());
        entity.setCardLastFour(payment.getCardLastFour());
        entity.setAttempts(payment.getAttempts());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.getId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getMethod(),
                entity.getStatus(),
                entity.getCreditCardId(),
                entity.getCardTokenId(),
                entity.getCardLastFour(),
                entity.getAttempts() != null ? entity.getAttempts() : 0,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import com.tuempresa.ecommerce.orders.domain.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String cardLastFour;
    private Payment.Status status;
    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id,
                           Long orderId,
                           BigDecimal amount,
                           String method,
                           String cardLastFour,
                           Payment.Status status,
                           int attempts,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.cardLastFour = cardLastFour;
        this.status = status;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public Payment.Status getStatus() {
        return status;
    }

    public void setStatus(Payment.Status status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;

public class PaymentWebMapper {

    private PaymentWebMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getCardLastFour(),
                payment.getStatus(),
                payment.getAttempts(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final CreditCardUseCase creditCardUseCase;
    private final OrderSettingsUseCase orderSettingsUseCase;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort,
                          OrderRepositoryPort orderRepositoryPort,
                          CreditCardUseCase creditCardUseCase,
                          OrderSettingsUseCase orderSettingsUseCase) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.creditCardUseCase = creditCardUseCase;
        this.orderSettingsUseCase = orderSettingsUseCase;
    }

    @Override
    public Payment registerPayment(Long userId,
                                   Long orderId,
                                   BigDecimal amount,
                                   String method,
                                   String cardToken) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("El usuario autenticado no coincide con la orden");
        }

        CreditCard creditCard = creditCardUseCase.findByToken(cardToken)
                .orElseThrow(() -> new UnauthorizedAccessException("Token de tarjeta inválido"));

        if (!creditCard.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("La tarjeta no pertenece al usuario autenticado");
        }

        ensureCardNotExpired(creditCard);

        Payment payment = new Payment(
                orderId,
                amount,
                method,
                creditCard.getId(),
                creditCard.getTokenId(),
                creditCard.getLastFourDigits()
        );

        OrderSettings settings = orderSettingsUseCase.getSettings();
        double rejectionProbability = settings.getCardRejectionProbability() != null
                ? clampProbability(settings.getCardRejectionProbability())
                : 0.0d;
        int maxRetries = settings.getPaymentRetryAttempts() != null && settings.getPaymentRetryAttempts() > 0
                ? settings.getPaymentRetryAttempts()
                : 1;

        int attempts = 0;
        boolean approved = false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (attempts < maxRetries) {
            attempts++;
            double roll = random.nextDouble();
            if (roll >= rejectionProbability) {
                approved = true;
                break;
            }
        }

        payment.setAttempts(attempts);
        payment.setCreatedAt(LocalDateTime.now());

        if (approved) {
            payment.markCompleted();
            Payment savedPayment = paymentRepositoryPort.save(payment);
            order.markAsPaid();
            orderRepositoryPort.save(order);
            return savedPayment;
        } else {
            payment.markFailed();
            return paymentRepositoryPort.save(payment);
        }
    }

    @Override
    public Payment markAsCompleted(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markCompleted();
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));
        order.markAsPaid();
        orderRepositoryPort.save(order);

        return savedPayment;
    }

    @Override
    public Payment markAsFailed(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markFailed();
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long paymentId) {
        return getOrThrow(paymentId);
    }

    private Payment getOrThrow(Long paymentId) {
        return paymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
    }

    private void ensureCardNotExpired(CreditCard creditCard) {
        YearMonth expiry = YearMonth.of(creditCard.getExpiryYear(), creditCard.getExpiryMonth());
        if (expiry.isBefore(YearMonth.now())) {
            throw new UnauthorizedAccessException("La tarjeta se encuentra vencida");
        }
    }

    private double clampProbability(Double value) {
        if (value == null) {
            return 0.0d;
        }
        if (value < 0) {
            return 0.0d;
        }
        if (value > 1) {
            return 1.0d;
    }
        return value;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final CreditCardUseCase creditCardUseCase;
    private final OrderSettingsUseCase orderSettingsUseCase;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort,
                          OrderRepositoryPort orderRepositoryPort,
                          CreditCardUseCase creditCardUseCase,
                          OrderSettingsUseCase orderSettingsUseCase) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.creditCardUseCase = creditCardUseCase;
        this.orderSettingsUseCase = orderSettingsUseCase;
    }

    @Override
    public Payment registerPayment(Long userId,
                                   Long orderId,
                                   BigDecimal amount,
                                   String method,
                                   String cardToken) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("El usuario autenticado no coincide con la orden");
        }

        CreditCard creditCard = creditCardUseCase.findByToken(cardToken)
                .orElseThrow(() -> new UnauthorizedAccessException("Token de tarjeta inválido"));

        if (!creditCard.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("La tarjeta no pertenece al usuario autenticado");
        }

        ensureCardNotExpired(creditCard);

        Payment payment = new Payment(
                orderId,
                amount,
                method,
                creditCard.getId(),
                creditCard.getTokenId(),
                creditCard.getLastFourDigits()
        );

        OrderSettings settings = orderSettingsUseCase.getSettings();
        double rejectionProbability = settings.getCardRejectionProbability() != null
                ? clampProbability(settings.getCardRejectionProbability())
                : 0.0d;
        int maxRetries = settings.getPaymentRetryAttempts() != null && settings.getPaymentRetryAttempts() > 0
                ? settings.getPaymentRetryAttempts()
                : 1;

        int attempts = 0;
        boolean approved = false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (attempts < maxRetries) {
            attempts++;
            double roll = random.nextDouble();
            if (roll >= rejectionProbability) {
                approved = true;
                break;
            }
        }

        payment.setAttempts(attempts);
        payment.setCreatedAt(LocalDateTime.now());

        if (approved) {
            payment.markCompleted();
            Payment savedPayment = paymentRepositoryPort.save(payment);
            order.markAsPaid();
            orderRepositoryPort.save(order);
            return savedPayment;
        } else {
            payment.markFailed();
            return paymentRepositoryPort.save(payment);
        }
    }

    @Override
    public Payment markAsCompleted(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markCompleted();
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));
        order.markAsPaid();
        orderRepositoryPort.save(order);

        return savedPayment;
    }

    @Override
    public Payment markAsFailed(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markFailed();
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long paymentId) {
        return getOrThrow(paymentId);
    }

    private Payment getOrThrow(Long paymentId) {
        return paymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
    }

    private void ensureCardNotExpired(CreditCard creditCard) {
        YearMonth expiry = YearMonth.of(creditCard.getExpiryYear(), creditCard.getExpiryMonth());
        if (expiry.isBefore(YearMonth.now())) {
            throw new UnauthorizedAccessException("La tarjeta se encuentra vencida");
        }
    }

    private double clampProbability(Double value) {
        if (value == null) {
            return 0.0d;
        }
        if (value < 0) {
            return 0.0d;
        }
        if (value > 1) {
            return 1.0d;
    }
        return value;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.domain.model;

public class OrderSettings {

    private Long id;
    private Double cardRejectionProbability;
    private Integer paymentRetryAttempts;

    public OrderSettings() {
    }

    public OrderSettings(Long id, Double cardRejectionProbability, Integer paymentRetryAttempts) {
        this.id = id;
        this.cardRejectionProbability = cardRejectionProbability;
        this.paymentRetryAttempts = paymentRetryAttempts;
    }

    public OrderSettings(Double cardRejectionProbability, Integer paymentRetryAttempts) {
        this.cardRejectionProbability = cardRejectionProbability;
        this.paymentRetryAttempts = paymentRetryAttempts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getCardRejectionProbability() {
        return cardRejectionProbability;
    }

    public void setCardRejectionProbability(Double cardRejectionProbability) {
        this.cardRejectionProbability = cardRejectionProbability;
    }

    public Integer getPaymentRetryAttempts() {
        return paymentRetryAttempts;
    }

    public void setPaymentRetryAttempts(Integer paymentRetryAttempts) {
        this.paymentRetryAttempts = paymentRetryAttempts;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.in;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;

public interface OrderSettingsUseCase {

    OrderSettings getSettings();

    OrderSettings updateSettings(OrderSettings settings);
}


```

```java
package com.tuempresa.ecommerce.orders.domain.port.out;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;

import java.util.Optional;

public interface OrderSettingsRepositoryPort {

    Optional<OrderSettings> findSettings();

    OrderSettings save(OrderSettings settings);
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderSettingsRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderSettingsService implements OrderSettingsUseCase {

    private final OrderSettingsRepositoryPort repositoryPort;

    public OrderSettingsService(OrderSettingsRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSettings getSettings() {
        return repositoryPort.findSettings()
                .orElseGet(() -> new OrderSettings(null, 0.0d, 1));
    }

    @Override
    public OrderSettings updateSettings(OrderSettings settings) {
        if (settings.getCardRejectionProbability() == null) {
            settings.setCardRejectionProbability(0.0d);
        }
        if (settings.getCardRejectionProbability() < 0) {
            settings.setCardRejectionProbability(0.0d);
        }
        if (settings.getCardRejectionProbability() > 1) {
            settings.setCardRejectionProbability(1.0d);
        }
        if (settings.getPaymentRetryAttempts() == null || settings.getPaymentRetryAttempts() < 1) {
            settings.setPaymentRetryAttempts(1);
        }
        return repositoryPort.save(settings);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_settings")
public class OrderSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_rejection_probability", nullable = false)
    private Double cardRejectionProbability;

    @Column(name = "payment_retry_attempts", nullable = false)
    private Integer paymentRetryAttempts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getCardRejectionProbability() {
        return cardRejectionProbability;
    }

    public void setCardRejectionProbability(Double cardRejectionProbability) {
        this.cardRejectionProbability = cardRejectionProbability;
    }

    public Integer getPaymentRetryAttempts() {
        return paymentRetryAttempts;
    }

    public void setPaymentRetryAttempts(Integer paymentRetryAttempts) {
        this.paymentRetryAttempts = paymentRetryAttempts;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderSettingsEntity;

public class OrderSettingsMapper {

    private OrderSettingsMapper() {
    }

    public static OrderSettings toDomain(OrderSettingsEntity entity) {
        if (entity == null) {
            return null;
        }
        return new OrderSettings(
                entity.getId(),
                entity.getCardRejectionProbability(),
                entity.getPaymentRetryAttempts()
        );
    }

    public static OrderSettingsEntity toEntity(OrderSettings settings) {
        if (settings == null) {
            return null;
        }
        OrderSettingsEntity entity = new OrderSettingsEntity();
        entity.setId(settings.getId());
        entity.setCardRejectionProbability(settings.getCardRejectionProbability());
        entity.setPaymentRetryAttempts(settings.getPaymentRetryAttempts());
        return entity;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderSettingsJpaRepository extends JpaRepository<OrderSettingsEntity, Long> {

    Optional<OrderSettingsEntity> findTopByOrderByIdAsc();
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderSettingsRepositoryPort;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.OrderSettingsEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.mapper.OrderSettingsMapper;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.OrderSettingsJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderSettingsRepositoryAdapter implements OrderSettingsRepositoryPort {

    private final OrderSettingsJpaRepository repository;

    public OrderSettingsRepositoryAdapter(OrderSettingsJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OrderSettings> findSettings() {
        return repository.findTopByOrderByIdAsc()
                .map(OrderSettingsMapper::toDomain);
    }

    @Override
    public OrderSettings save(OrderSettings settings) {
        OrderSettingsEntity saved = repository.save(OrderSettingsMapper.toEntity(settings));
        return OrderSettingsMapper.toDomain(saved);
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderSettingsRequest {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "1.0", inclusive = true)
    private Double cardRejectionProbability;

    @NotNull
    @Min(1)
    private Integer paymentRetryAttempts;

    public Double getCardRejectionProbability() {
        return cardRejectionProbability;
    }

    public void setCardRejectionProbability(Double cardRejectionProbability) {
        this.cardRejectionProbability = cardRejectionProbability;
    }

    public Integer getPaymentRetryAttempts() {
        return paymentRetryAttempts;
    }

    public void setPaymentRetryAttempts(Integer paymentRetryAttempts) {
        this.paymentRetryAttempts = paymentRetryAttempts;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.dto;

public class OrderSettingsResponse {

    private Double cardRejectionProbability;
    private Integer paymentRetryAttempts;

    public OrderSettingsResponse() {
    }

    public OrderSettingsResponse(Double cardRejectionProbability, Integer paymentRetryAttempts) {
        this.cardRejectionProbability = cardRejectionProbability;
        this.paymentRetryAttempts = paymentRetryAttempts;
    }

    public Double getCardRejectionProbability() {
        return cardRejectionProbability;
    }

    public void setCardRejectionProbability(Double cardRejectionProbability) {
        this.cardRejectionProbability = cardRejectionProbability;
    }

    public Integer getPaymentRetryAttempts() {
        return paymentRetryAttempts;
    }

    public void setPaymentRetryAttempts(Integer paymentRetryAttempts) {
        this.paymentRetryAttempts = paymentRetryAttempts;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.mapper;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsResponse;

public class OrderSettingsWebMapper {

    private OrderSettingsWebMapper() {
    }

    public static OrderSettingsResponse toResponse(OrderSettings settings) {
        if (settings == null) {
            return null;
        }
        return new OrderSettingsResponse(
                settings.getCardRejectionProbability(),
                settings.getPaymentRetryAttempts()
        );
    }

    public static OrderSettings toDomain(OrderSettingsRequest request) {
        if (request == null) {
            return null;
        }
        return new OrderSettings(
                request.getCardRejectionProbability(),
                request.getPaymentRetryAttempts()
        );
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.OrderSettingsResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.OrderSettingsWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders/settings")
public class OrderSettingsController {

    private final OrderSettingsUseCase orderSettingsUseCase;

    public OrderSettingsController(OrderSettingsUseCase orderSettingsUseCase) {
        this.orderSettingsUseCase = orderSettingsUseCase;
    }

    @GetMapping
    public ResponseEntity<OrderSettingsResponse> getSettings() {
        return ResponseEntity.ok(OrderSettingsWebMapper.toResponse(orderSettingsUseCase.getSettings()));
    }

    @PutMapping
    public ResponseEntity<OrderSettingsResponse> updateSettings(@Valid @RequestBody OrderSettingsRequest request) {
        var updated = orderSettingsUseCase.updateSettings(OrderSettingsWebMapper.toDomain(request));
        return ResponseEntity.ok(OrderSettingsWebMapper.toResponse(updated));
    }
}


```

```java
package com.tuempresa.ecommerce.orders.config;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeOrderSettings(OrderSettingsUseCase orderSettingsUseCase) {
        return args -> {
            OrderSettings settings = orderSettingsUseCase.getSettings();
            boolean needsUpdate = false;

            if (settings.getCardRejectionProbability() == null) {
                settings.setCardRejectionProbability(0.15d);
                needsUpdate = true;
            }

            if (settings.getPaymentRetryAttempts() == null || settings.getPaymentRetryAttempts() < 1) {
                settings.setPaymentRetryAttempts(3);
                needsUpdate = true;
            }

            if (needsUpdate) {
                orderSettingsUseCase.updateSettings(settings);
            }
        };
    }
}


```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.Payment;
import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.PaymentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final CreditCardUseCase creditCardUseCase;
    private final OrderSettingsUseCase orderSettingsUseCase;

    public PaymentService(PaymentRepositoryPort paymentRepositoryPort,
                          OrderRepositoryPort orderRepositoryPort,
                          CreditCardUseCase creditCardUseCase,
                          OrderSettingsUseCase orderSettingsUseCase) {
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.creditCardUseCase = creditCardUseCase;
        this.orderSettingsUseCase = orderSettingsUseCase;
    }

    @Override
    public Payment registerPayment(Long userId,
                                   Long orderId,
                                   BigDecimal amount,
                                   String method,
                                   String cardToken) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("El usuario autenticado no coincide con la orden");
        }

        CreditCard creditCard = creditCardUseCase.findByToken(cardToken)
                .orElseThrow(() -> new UnauthorizedAccessException("Token de tarjeta inválido"));

        if (!creditCard.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("La tarjeta no pertenece al usuario autenticado");
        }

        ensureCardNotExpired(creditCard);

        Payment payment = new Payment(
                orderId,
                amount,
                method,
                creditCard.getId(),
                creditCard.getTokenId(),
                creditCard.getLastFourDigits()
        );

        OrderSettings settings = orderSettingsUseCase.getSettings();
        double rejectionProbability = settings.getCardRejectionProbability() != null
                ? clampProbability(settings.getCardRejectionProbability())
                : 0.0d;
        int maxRetries = settings.getPaymentRetryAttempts() != null && settings.getPaymentRetryAttempts() > 0
                ? settings.getPaymentRetryAttempts()
                : 1;

        int attempts = 0;
        boolean approved = false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (attempts < maxRetries) {
            attempts++;
            double roll = random.nextDouble();
            if (roll >= rejectionProbability) {
                approved = true;
                break;
            }
        }

        payment.setAttempts(attempts);

        if (approved) {
            payment.markCompleted();
            Payment savedPayment = paymentRepositoryPort.save(payment);
            order.markAsPaid();
            orderRepositoryPort.save(order);
            return savedPayment;
        } else {
            payment.markFailed();
            return paymentRepositoryPort.save(payment);
        }
    }

    @Override
    public Payment markAsCompleted(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markCompleted();
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepositoryPort.save(payment);

        Order order = orderRepositoryPort.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + payment.getOrderId()));
        order.markAsPaid();
        orderRepositoryPort.save(order);

        return savedPayment;
    }

    @Override
    public Payment markAsFailed(Long paymentId) {
        Payment payment = getOrThrow(paymentId);
        payment.markFailed();
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepositoryPort.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long paymentId) {
        return getOrThrow(paymentId);
    }

    private Payment getOrThrow(Long paymentId) {
        return paymentRepositoryPort.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));
    }

    private void ensureCardNotExpired(CreditCard creditCard) {
        YearMonth expiry = YearMonth.of(creditCard.getExpiryYear(), creditCard.getExpiryMonth());
        if (expiry.isBefore(YearMonth.now())) {
            throw new UnauthorizedAccessException("La tarjeta se encuentra vencida");
        }
    }

    private double clampProbability(Double value) {
        if (value == null) {
            return 0.0d;
        }
        if (value < 0) {
            return 0.0d;
        }
        if (value > 1) {
            return 1.0d;
    }
        return value;
    }
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.exception.InvalidOrderStateException;
import com.tuempresa.ecommerce.orders.domain.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.orders.domain.model.Cart;
import com.tuempresa.ecommerce.orders.domain.model.CartItem;
import com.tuempresa.ecommerce.orders.domain.model.Order;
import com.tuempresa.ecommerce.orders.domain.model.OrderItem;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.CartRepositoryPort;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort, CartRepositoryPort cartRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Order createFromCart(Long cartId, String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }
        String normalizedAddress = deliveryAddress.trim();
        if (normalizedAddress.isEmpty()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }

        Cart cart = cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + cartId));

        if (cart.getItems().isEmpty()) {
            throw new InvalidOrderStateException("No se puede crear una orden con un carrito vacío");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(cart.getUserId(), normalizedAddress, orderItems, total);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepositoryPort.save(order);

        cart.clearItems();
        cartRepositoryPort.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + orderId));
    }

    @Override
    public Order markAsPaid(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.CANCELLED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede marcar como pagada una orden cancelada");
        }
        order.markAsPaid();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    @Override
    public Order cancel(Long orderId) {
        Order order = findById(orderId);
        if (Order.Status.PAID.equals(order.getStatus()) || Order.Status.COMPLETED.equals(order.getStatus())) {
            throw new InvalidOrderStateException("No se puede cancelar una orden pagada o completada");
        }
        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepositoryPort.save(order);
    }

    private OrderItem toOrderItem(CartItem cartItem) {
        return new OrderItem(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }
}



```

```java
package com.tuempresa.ecommerce.orders.application.service;

import com.tuempresa.ecommerce.orders.domain.model.OrderSettings;
import com.tuempresa.ecommerce.orders.domain.port.in.OrderSettingsUseCase;
import com.tuempresa.ecommerce.orders.domain.port.out.OrderSettingsRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderSettingsService implements OrderSettingsUseCase {

    private final OrderSettingsRepositoryPort repositoryPort;

    public OrderSettingsService(OrderSettingsRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSettings getSettings() {
        return repositoryPort.findSettings()
                .orElseGet(() -> new OrderSettings(null, 0.0d, 1));
    }

    @Override
    public OrderSettings updateSettings(OrderSettings settings) {
        OrderSettings current = repositoryPort.findSettings()
                .orElseGet(() -> new OrderSettings(null, 0.0d, 1));

        if (settings.getCardRejectionProbability() == null || settings.getCardRejectionProbability() < 0) {
            current.setCardRejectionProbability(0.0d);
        } else if (settings.getCardRejectionProbability() > 1) {
            current.setCardRejectionProbability(1.0d);
        } else {
            current.setCardRejectionProbability(settings.getCardRejectionProbability());
        }

        if (settings.getPaymentRetryAttempts() == null || settings.getPaymentRetryAttempts() < 1) {
            current.setPaymentRetryAttempts(1);
        } else {
            current.setPaymentRetryAttempts(settings.getPaymentRetryAttempts());
        }

        return repositoryPort.save(current);
    }
}



```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos y puede iniciar con +")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String telefono, String direccion, String password) {
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


```

## Resumen
- La creación de órdenes ahora exige una dirección de entrega explícita y la propaga en todo el ciclo (modelo, persistencia y respuesta). Si la dirección viene vacía se rechaza la solicitud antes de generar la orden, lo que permite usar domicilios distintos al registrado por el usuario.  
```33:65:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/application/service/OrderService.java
    public Order createFromCart(Long cartId, String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }
        String normalizedAddress = deliveryAddress.trim();
        if (normalizedAddress.isEmpty()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }
        ...
        Order order = new Order(cart.getUserId(), normalizedAddress, orderItems, total);
```
```1:37:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/dto/CreateOrderRequest.java
public class CreateOrderRequest {
    @NotNull
    private Long cartId;

    @NotBlank
    private String deliveryAddress;
```

- Se añadió un nuevo módulo de configuración para `orders-service` con repositorio, servicio y endpoint REST para ajustar la probabilidad de rechazo y los reintentos de pago. Los valores se validan (0–1 y mínimo 1) y se inicializan con defaults si no existen.  
```1:35:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/controller/OrderSettingsController.java
@RestController
@RequestMapping("/orders/settings")
public class OrderSettingsController {
    ...
    @GetMapping
    public ResponseEntity<OrderSettingsResponse> getSettings() { ... }

    @PutMapping
    public ResponseEntity<OrderSettingsResponse> updateSettings(@Valid @RequestBody OrderSettingsRequest request) { ... }
```
```19:46:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/application/service/OrderSettingsService.java
    @Override
    public OrderSettings updateSettings(OrderSettings settings) {
        OrderSettings current = repositoryPort.findSettings()
                .orElseGet(() -> new OrderSettings(null, 0.0d, 1));
        ...
        return repositoryPort.save(current);
    }
```
```12:30:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/config/DataInitializer.java
CommandLineRunner initializeOrderSettings(OrderSettingsUseCase orderSettingsUseCase) {
    return args -> {
        OrderSettings settings = orderSettingsUseCase.getSettings();
        ...
        if (needsUpdate) {
            orderSettingsUseCase.updateSettings(settings);
        }
    };
}
```

- El flujo de pago usa la configuración anterior para simular rechazos: se realizan N intentos con números aleatorios independientes, se marca la transacción como aprobada o fallida y se registra cuántos intentos se hicieron para devolverlos al cliente.  
```41:103:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/application/service/PaymentService.java
        OrderSettings settings = orderSettingsUseCase.getSettings();
        double rejectionProbability = settings.getCardRejectionProbability() != null
                ? clampProbability(settings.getCardRejectionProbability())
                : 0.0d;
        int maxRetries = settings.getPaymentRetryAttempts() != null && settings.getPaymentRetryAttempts() > 0
                ? settings.getPaymentRetryAttempts()
                : 1;

        int attempts = 0;
        boolean approved = false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (attempts < maxRetries) {
            attempts++;
            double roll = random.nextDouble();
            if (roll >= rejectionProbability) {
                approved = true;
                break;
            }
        }

        payment.setAttempts(attempts);
        if (approved) {
            payment.markCompleted();
            Payment savedPayment = paymentRepositoryPort.save(payment);
            order.markAsPaid();
            orderRepositoryPort.save(order);
            return savedPayment;
        } else {
            payment.markFailed();
            return paymentRepositoryPort.save(payment);
        }
```
```1:104:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/dto/PaymentResponse.java
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String cardLastFour;
    private Payment.Status status;
    private int attempts;
```

## Pruebas
- No pude ejecutar `./gradlew :services:orders-service:build` porque la sandbox no dispone de un JDK. Ejecútalo localmente cuando tengas Java instalado para validar el build.

## Próximos pasos / Uso sugerido
- Actualizar la configuración de pagos (probabilidad y reintentos):  
  ```bash
  curl -X PUT http://localhost:8083/api/v1/orders/settings \
    -H "Content-Type: application/json" \
    -d '{ "cardRejectionProbability": 0.25, "paymentRetryAttempts": 5 }'
  ```
- Crear una orden indicando la dirección de entrega:  
  ```bash
  curl -X POST http://localhost:8083/api/v1/orders \
    -H "Content-Type: application/json" \
    -d '{ "cartId": 1, "deliveryAddress": "Av. Siempre Viva 742" }'
  ```
- Registrar un pago y revisar los intentos realizados (campo `attempts` en la respuesta):  
  ```bash
  curl -X POST http://localhost:8083/api/v1/payments \
    -H "Authorization: Bearer <token>" \
    -H "Content-Type: application/json" \
    -d '{ "orderId": 1, "amount": 199.99, "method": "CREDIT_CARD", "cardToken": "<token_tarjeta>" }'
  ```

Estos cambios mantienen el patrón hexagonal e introducen controles configurables para simular rechazos y reintentos de tarjetas, además de permitir direcciones de entrega personalizadas por orden.

```java
    public Order createFromCart(Long cartId, String deliveryAddress) {
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }
        String normalizedAddress = deliveryAddress.trim();
        if (normalizedAddress.isEmpty()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }
        ...
        Order order = new Order(cart.getUserId(), normalizedAddress, orderItems, total);
```

```java
public class CreateOrderRequest {
    @NotNull
    private Long cartId;

    @NotBlank
    private String deliveryAddress;
```

```java
@RestController
@RequestMapping("/orders/settings")
public class OrderSettingsController {
    ...
    @GetMapping
    public ResponseEntity<OrderSettingsResponse> getSettings() { ... }

    @PutMapping
    public ResponseEntity<OrderSettingsResponse> updateSettings(@Valid @RequestBody OrderSettingsRequest request) { ... }
```

```java
    @Override
    public OrderSettings updateSettings(OrderSettings settings) {
        OrderSettings current = repositoryPort.findSettings()
                .orElseGet(() -> new OrderSettings(null, 0.0d, 1));
        ...
        return repositoryPort.save(current);
    }
```

```java
CommandLineRunner initializeOrderSettings(OrderSettingsUseCase orderSettingsUseCase) {
    return args -> {
        OrderSettings settings = orderSettingsUseCase.getSettings();
        ...
        if (needsUpdate) {
            orderSettingsUseCase.updateSettings(settings);
        }
    };
}
```

```java
        OrderSettings settings = orderSettingsUseCase.getSettings();
        double rejectionProbability = settings.getCardRejectionProbability() != null
                ? clampProbability(settings.getCardRejectionProbability())
                : 0.0d;
        int maxRetries = settings.getPaymentRetryAttempts() != null && settings.getPaymentRetryAttempts() > 0
                ? settings.getPaymentRetryAttempts()
                : 1;

        int attempts = 0;
        boolean approved = false;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (attempts < maxRetries) {
            attempts++;
            double roll = random.nextDouble();
            if (roll >= rejectionProbability) {
                approved = true;
                break;
            }
        }

        payment.setAttempts(attempts);
        if (approved) {
            payment.markCompleted();
            Payment savedPayment = paymentRepositoryPort.save(payment);
            order.markAsPaid();
            orderRepositoryPort.save(order);
            return savedPayment;
        } else {
            payment.markFailed();
            return paymentRepositoryPort.save(payment);
        }
```

```java
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String cardLastFour;
    private Payment.Status status;
    private int attempts;
```

```shellscript
  curl -X PUT http://localhost:8083/api/v1/orders/settings \
    -H "Content-Type: application/json" \
    -d '{ "cardRejectionProbability": 0.25, "paymentRetryAttempts": 5 }'
```

```shellscript
  curl -X POST http://localhost:8083/api/v1/orders \
    -H "Content-Type: application/json" \
    -d '{ "cartId": 1, "deliveryAddress": "Av. Siempre Viva 742" }'
```

```shellscript
  curl -X POST http://localhost:8083/api/v1/payments \
    -H "Authorization: Bearer <token>" \
    -H "Content-Type: application/json" \
    -d '{ "orderId": 1, "amount": 199.99, "method": "CREDIT_CARD", "cardToken": "<token_tarjeta>" }'
```

---

**User**

solo me falta agregarle un x-api-key de header al querer hacer cualquier request al modelo de creditCard, sea registrar obtener o pagar, que solo valide si es el mismo que tiene registrado y si no es responda un error

---

**Cursor**

```yaml
server:
  port: 8083

spring:
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:ordersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

clients:
  catalog:
    base-url: http://localhost:8081/api/v1
  users:
    base-url: http://localhost:8082/api/v1

security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
  card-token:
    secret: change-me-card-token-secret
  api-key: change-me-orders-api-key

```

```java
package com.tuempresa.ecommerce.orders.application.service.security;

import com.tuempresa.ecommerce.orders.domain.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyValidator {

    private final String expectedApiKey;

    public ApiKeyValidator(@Value("${security.api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    public void validate(String providedKey) {
        if (expectedApiKey == null || expectedApiKey.isBlank()) {
            throw new IllegalStateException("API key esperada no configurada");
        }

        if (providedKey == null || providedKey.isBlank()) {
            throw new UnauthorizedAccessException("x-api-key faltante");
        }

        if (!expectedApiKey.equals(providedKey)) {
            throw new UnauthorizedAccessException("x-api-key inválida");
        }
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.application.service.security.ApiKeyValidator;
import com.tuempresa.ecommerce.orders.application.service.security.JwtService;
import com.tuempresa.ecommerce.orders.domain.model.CreditCard;
import com.tuempresa.ecommerce.orders.domain.port.in.CreditCardUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreateCreditCardRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreditCardResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.CreditCardWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CreditCardController {

    private final CreditCardUseCase creditCardUseCase;
    private final JwtService jwtService;
    private final ApiKeyValidator apiKeyValidator;

    public CreditCardController(CreditCardUseCase creditCardUseCase,
                                JwtService jwtService,
                                ApiKeyValidator apiKeyValidator) {
        this.creditCardUseCase = creditCardUseCase;
        this.jwtService = jwtService;
        this.apiKeyValidator = apiKeyValidator;
    }

    @PostMapping
    public ResponseEntity<CreditCardResponse> registerCard(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreateCreditCardRequest request) {
        apiKeyValidator.validate(apiKey);
        Long userId = jwtService.extractUserId(authorization);
        CreditCard card = creditCardUseCase.registerCard(
                userId,
                request.getCardNumber(),
                request.getCvv(),
                request.getExpiryMonth(),
                request.getExpiryYear()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CreditCardWebMapper.toResponse(card));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> listUserCards(@RequestHeader("x-api-key") String apiKey,
                                                                  @RequestHeader("Authorization") String authorization) {
        apiKeyValidator.validate(apiKey);
        Long userId = jwtService.extractUserId(authorization);
        List<CreditCardResponse> responses = creditCardUseCase.findByUser(userId).stream()
                .map(CreditCardWebMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.controller;

import com.tuempresa.ecommerce.orders.application.service.security.ApiKeyValidator;
import com.tuempresa.ecommerce.orders.application.service.security.JwtService;
import com.tuempresa.ecommerce.orders.domain.port.in.PaymentUseCase;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.CreatePaymentRequest;
import com.tuempresa.ecommerce.orders.infrastructure.web.dto.PaymentResponse;
import com.tuempresa.ecommerce.orders.infrastructure.web.mapper.PaymentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;
    private final JwtService jwtService;
    private final ApiKeyValidator apiKeyValidator;

    public PaymentController(PaymentUseCase paymentUseCase,
                              JwtService jwtService,
                              ApiKeyValidator apiKeyValidator) {
        this.paymentUseCase = paymentUseCase;
        this.jwtService = jwtService;
        this.apiKeyValidator = apiKeyValidator;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> registerPayment(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreatePaymentRequest request) {
        apiKeyValidator.validate(apiKey);
        Long userId = jwtService.extractUserId(authorization);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(PaymentWebMapper.toResponse(
                        paymentUseCase.registerPayment(
                                userId,
                                request.getOrderId(),
                                request.getAmount(),
                                request.getMethod(),
                                request.getCardToken()
                        )
                ));
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponse> markAsCompleted(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsCompleted(paymentId))
        );
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> markAsFailed(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.markAsFailed(paymentId))
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                PaymentWebMapper.toResponse(paymentUseCase.findById(paymentId))
        );
    }
}


```

## Cambios
- añadí `x-api-key` a la configuración (`security.api-key`) y un `ApiKeyValidator` que compara el valor recibido con el configurado.
- `CreditCardController` ahora exige el header para registrar y listar tarjetas; falla con `UnauthorizedAccessException` si falta o no coincide.
```33:56:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/controller/CreditCardController.java
    public ResponseEntity<CreditCardResponse> registerCard(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreateCreditCardRequest request) {
        apiKeyValidator.validate(apiKey);
        ...
```
- `PaymentController` valida `x-api-key` antes de crear pagos, reutilizando el mismo helper de seguridad.
```24:48:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/controller/PaymentController.java
    public ResponseEntity<PaymentResponse> registerPayment(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreatePaymentRequest request) {
        apiKeyValidator.validate(apiKey);
        ...
```

## Uso
Incluye siempre el header `x-api-key` con el valor configurado (`security.api-key`) en cualquier request de tarjetas o pagos, por ejemplo:
```bash
curl -X POST http://localhost:8083/api/v1/cards \
  -H "x-api-key: change-me-orders-api-key" \
  -H "Authorization: Bearer <jwt>" \
  ...
```

## Pendientes
No se ejecutó `./gradlew :services:orders-service:build` porque la sandbox no dispone de JDK; córrelo localmente para validar la compilación.

```java
    public ResponseEntity<CreditCardResponse> registerCard(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreateCreditCardRequest request) {
        apiKeyValidator.validate(apiKey);
        ...
```

```java
    public ResponseEntity<PaymentResponse> registerPayment(@RequestHeader("x-api-key") String apiKey,
                                                           @RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody CreatePaymentRequest request) {
        apiKeyValidator.validate(apiKey);
        ...
```

```shellscript
curl -X POST http://localhost:8083/api/v1/cards \
  -H "x-api-key: change-me-orders-api-key" \
  -H "Authorization: Bearer <jwt>" \
  ...
```

---

**User**

si ahorita todos los intentos de pago fallan respondo un error?

---

**Cursor**

Hoy la lógica no lanza excepción: si todos los intentos fallan marcamos el pago como `FAILED`, lo persistimos y devolvemos un `PaymentResponse` (incluyendo `status=FAILED` y `attempts` usados). El endpoint sigue respondiendo 201 con ese payload; depende de ti si luego quieres que el controlador devuelva otro código (por ejemplo 402/409) al detectar `status FAILED`.

---

**User**

okok perfecto así, y puedo volver a intentar pagar la orden no?

---

**Cursor**

Sí. Si el intento falló, la orden sigue en estado `CREATED`, así que puedes volver a llamar al endpoint de pagos con la misma orden (ya sea con la misma tarjeta u otra). Cada llamada genera un nuevo `Payment`; sólo cuando uno pasa a `COMPLETED` se marca la orden como pagada.

---

**User**

perfecto ahora si dame un paso a paso de todos los curl para probar desde agregar un carrito hasta pagar

---

**Cursor**

Aquí tienes un flujo completo con `curl` desde crear carrito hasta pagar, usando los endpoints actuales (ajusta dominios y valores según tu entorno):

### 1. Preparación básica

**(Opcional)** crear un producto en catálogo si no tienes datos:  
```bash
curl -X POST http://localhost:8081/api/v1/catalog/products \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Nintendo Switch",
        "description": "Consola híbrida",
        "price": 299.99,
        "stock": 25
      }'
```

### 2. Registrar y autenticar usuario

```bash
curl -X POST http://localhost:8082/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Ana Tester",
        "email": "ana@example.com",
        "telefono": "+34600111222",
        "direccion": "Calle Falsa 123, Madrid",
        "password": "Secreta123"
      }'
```

```bash
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
        "email": "ana@example.com",
        "password": "Secreta123"
      }'
```

Guarda `token` del login (ej. `TOKEN_JWT`).

### 3. Carrito

```bash
curl -X POST http://localhost:8083/api/v1/carts \
  -H "Content-Type: application/json" \
  -d '{ "userId": 1 }'
```

```bash
curl -X POST http://localhost:8083/api/v1/carts/1/items \
  -H "Content-Type: application/json" \
  -d '{ "productId": 1, "quantity": 2 }'
```

```bash
curl http://localhost:8083/api/v1/carts/1
```

### 4. Configuración opcional de pagos

```bash
curl -X PUT http://localhost:8083/api/v1/orders/settings \
  -H "Content-Type: application/json" \
  -d '{ "cardRejectionProbability": 0.2, "paymentRetryAttempts": 4 }'
```

### 5. Registrar tarjeta (requiere `Authorization` y `x-api-key`)

```bash
curl -X POST http://localhost:8083/api/v1/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN_JWT" \
  -H "x-api-key: change-me-orders-api-key" \
  -d '{
        "cardNumber": "4111111111111111",
        "cvv": "123",
        "expiryMonth": 12,
        "expiryYear": 2030
      }'
```

La respuesta te da un `token` de tarjeta (`TOKEN_TARJETA`).

### 6. Crear orden con dirección de entrega

```bash
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
        "cartId": 1,
        "deliveryAddress": "Av. Siempre Viva 742, Springfield"
      }'
```

### 7. Pagar la orden (usa el token de tarjeta y encabezados)

```bash
curl -X POST http://localhost:8083/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN_JWT" \
  -H "x-api-key: change-me-orders-api-key" \
  -d '{
        "orderId": 1,
        "amount": 599.98,
        "method": "CREDIT_CARD",
        "cardToken": "TOKEN_TARJETA"
      }'
```

La respuesta indica `status` (`COMPLETED` o `FAILED`) y `attempts`.  
Si sale `FAILED`, puedes reintentar la llamada a pagos (mismos headers) las veces que quieras; sólo cuando se consiga `COMPLETED` se marcará la orden como pagada.

### 8. Consultas finales

```bash
curl http://localhost:8083/api/v1/orders/1
curl http://localhost:8083/api/v1/payments/1
```

> Recuerda reemplazar `TOKEN_JWT`, `TOKEN_TARJETA`, IDs y `x-api-key` por los valores reales que recibas/uses en tu entorno.

```shellscript
curl -X POST http://localhost:8081/api/v1/catalog/products \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Nintendo Switch",
        "description": "Consola híbrida",
        "price": 299.99,
        "stock": 25
      }'
```

```shellscript
curl -X POST http://localhost:8082/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Ana Tester",
        "email": "ana@example.com",
        "telefono": "+34600111222",
        "direccion": "Calle Falsa 123, Madrid",
        "password": "Secreta123"
      }'
```

```shellscript
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
        "email": "ana@example.com",
        "password": "Secreta123"
      }'
```

```shellscript
curl -X POST http://localhost:8083/api/v1/carts \
  -H "Content-Type: application/json" \
  -d '{ "userId": 1 }'
```

```shellscript
curl -X POST http://localhost:8083/api/v1/carts/1/items \
  -H "Content-Type: application/json" \
  -d '{ "productId": 1, "quantity": 2 }'
```

```shellscript
curl http://localhost:8083/api/v1/carts/1
```

```shellscript
curl -X PUT http://localhost:8083/api/v1/orders/settings \
  -H "Content-Type: application/json" \
  -d '{ "cardRejectionProbability": 0.2, "paymentRetryAttempts": 4 }'
```

```shellscript
curl -X POST http://localhost:8083/api/v1/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN_JWT" \
  -H "x-api-key: change-me-orders-api-key" \
  -d '{
        "cardNumber": "4111111111111111",
        "cvv": "123",
        "expiryMonth": 12,
        "expiryYear": 2030
      }'
```

```shellscript
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
        "cartId": 1,
        "deliveryAddress": "Av. Siempre Viva 742, Springfield"
      }'
```

```shellscript
curl -X POST http://localhost:8083/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN_JWT" \
  -H "x-api-key: change-me-orders-api-key" \
  -d '{
        "orderId": 1,
        "amount": 599.98,
        "method": "CREDIT_CARD",
        "cardToken": "TOKEN_TARJETA"
      }'
```

```shellscript
curl http://localhost:8083/api/v1/orders/1
curl http://localhost:8083/api/v1/payments/1
```

---

**User**

please fix this /Users/gebrayel/Documents/Personal-Projects/springboot-test/ecommerce-simulator/services/users-service/src/main/java/com/tuempresa/ecommerce/users/infrastructure/persistence/adapter/UserRepositoryAdapter.java:15: error: UserRepositoryAdapter is not abstract and does not override abstract method findByEmail(String) in UserRepositoryPort

public class UserRepositoryAdapter implements UserRepositoryPort {

---

**Cursor**

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.adapter;

import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.out.UserRepositoryPort;
import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;
import com.tuempresa.ecommerce.users.infrastructure.persistence.mapper.UserEntityMapper;
import com.tuempresa.ecommerce.users.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntityMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return UserEntityMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        return userJpaRepository.existsByTelefono(telefono);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userJpaRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsByTelefonoAndIdNot(String telefono, Long id) {
        return userJpaRepository.existsByTelefonoAndIdNot(telefono, id);
    }
}


```

Listo: implementé `findByEmail` en `UserRepositoryAdapter` mapeando al repositorio JPA, así que ya satisface la interfaz y no debería volver a marcar error.

---

**User**

el api de registrar la card me responde {

    "timestamp": "2025-11-11T15:35:07.904+00:00",

    "status": 500,

    "error": "Internal Server Error",

    "path": "/api/v1/cards"

} y no se realmente cual es el error, tambien estoy viendo que el token que me genero eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaXNzIjoiZWNvbW1lcmNlLXNpbXVsYXRvciIsImlhdCI6MTc2Mjg3NTE3OSwiZXhwIjoxNzYyODgyMzc5LCJ1c2VySWQiOjEsImVtYWlsIjoiYW5hQGV4YW1wbGUuY29tIn0.5C5EuKe9bPQ3sy8Aawnndny-L168AnY5RN5WdvhbmqU en jwt dice invalid signature

---

**Cursor**

El 500 casi seguro viene de la validación de seguridad, no de la tarjeta en sí. Dos cosas a revisar:

---

### 1. `x-api-key` válido y propiedad configurada

El validador lanza `IllegalStateException("API key esperada no configurada")` si `security.api-key` no existe, y eso termina como 500.  
Asegúrate de tener, en `services/orders-service/src/main/resources/application.yaml`:

```yaml
security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
  card-token:
    secret: change-me-card-token-secret
  api-key: change-me-orders-api-key   # <-- necesario
```

Reinicia el servicio para que tome el valor y envía la cabecera en todas las llamadas de tarjetas/pagos:

```bash
-H "x-api-key: change-me-orders-api-key"
```

Si la propiedad está y la cabecera coincide, ya no deberías ver el 500.

---

### 2. Token JWT con firma válida

El token que recibiste (`eyJhbGciOiJIUzI1NiJ9...`) falla porque el `users-service` lo firmó con un secreto y `orders-service` está intentando verificarlo con otro distinto.

1. Verifica que **ambos** `application.yaml` (users-service y orders-service) tengan exactamente el mismo valor en `security.jwt.secret`.  
2. Después de cambiarlo, vuelve a iniciar sesión (`POST /api/v1/auth/login`) y usa el nuevo token.

Si dejas los secretos distintos, siempre obtendrás “invalid signature”.

---

### Resumen de la llamada correcta al registrar tarjeta

```bash
curl -X POST http://localhost:8083/api/v1/cards \
  -H "Content-Type: application/json" \
  -H "x-api-key: change-me-orders-api-key" \
  -H "Authorization: Bearer <token_JWT_generado_de_nuevo>" \
  -d '{
        "cardNumber": "4111111111111111",
        "cvv": "123",
        "expiryMonth": 12,
        "expiryYear": 2030
      }'
```

Si las dos condiciones anteriores se cumplen, la respuesta dejará de ser 500 y ya podrás usar la API de tarjetas y pagos.

```yaml
security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
  card-token:
    secret: change-me-card-token-secret
  api-key: change-me-orders-api-key   # <-- necesario
```

```shellscript
-H "x-api-key: change-me-orders-api-key"
```

```shellscript
curl -X POST http://localhost:8083/api/v1/cards \
  -H "Content-Type: application/json" \
  -H "x-api-key: change-me-orders-api-key" \
  -H "Authorization: Bearer <token_JWT_generado_de_nuevo>" \
  -d '{
        "cardNumber": "4111111111111111",
        "cvv": "123",
        "expiryMonth": 12,
        "expiryYear": 2030
      }'
```

---

**User**

revisa que tenga los mismos jwt secret porfa

---

**Cursor**

Acabo de revisar los `application.yaml`:

- `users-service`: `security.jwt.secret` = `change-me-jwt-secret-please-change`
- `orders-service`: `security.jwt.secret` = `change-me-jwt-secret-please-change`

Son idénticos, así que ambos servicios usarán la misma clave al firmar y validar el JWT. Después de ajustar cualquier valor recuerda reiniciar ambos servicios y generar un token nuevo (el anterior fue firmado con el secreto viejo).

---

**User**

curl --location 'http://localhost:8083/api/v1/cards' \



--header 'Content-Type: application/json' \

--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaXNzIjoiZWNvbW1lcmNlLXNpbXVsYXRvciIsImlhdCI6MTc2Mjg3NTY5NCwiZXhwIjoxNzYyODgyODk0LCJ1c2VySWQiOjEsImVtYWlsIjoiYW5hQGV4YW1wbGUuY29tIn0.K-aziPLBWDnoLnu1UrbJfz2yZUILJuJedtFvOjvuUXo' \

--header 'x-api-key: change-me-orders-api-key' \

--data '{

        "cardNumber": "4111111111111111",

        "cvv": "123",

        "expiryMonth": 12,

        "expiryYear": 2030

      }'



ese curl me sigue respondiendo 500

---

**Cursor**

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_cards")
public class CreditCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_hash", nullable = false, length = 128)
    private String cardNumberHash;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFourDigits;

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "token_id", unique = true, length = 64)
    private String tokenId;

    @Column(name = "token_signature", length = 128)
    private String tokenSignature;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCardNumberHash() {
        return cardNumberHash;
    }

    public void setCardNumberHash(String cardNumberHash) {
        this.cardNumberHash = cardNumberHash;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public Integer getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public Integer getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenSignature() {
        return tokenSignature;
    }

    public void setTokenSignature(String tokenSignature) {
        this.tokenSignature = tokenSignature;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}



```

El 500 que veías al registrar la tarjeta lo provocaba la columna `token_id` de `credit_cards`, que estaba marcada como `nullable = false`. Cuando insertábamos la tarjeta por primera vez aún no conocíamos el token (se genera después de obtener el `id`), así que la inserción fallaba.

Ya dejé los campos `token_id` y `token_signature` como opcionales en `CreditCardEntity`, lo que permite guardar el registro inicial y, tras generar el token, actualizarlo con los valores finales. Sólo reinicia el servicio de órdenes y vuelve a probar:

```bash
curl -X POST http://localhost:8083/api/v1/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token JWT>" \
  -H "x-api-key: change-me-orders-api-key" \
  -d '{ "cardNumber": "4111111111111111", "cvv": "123", "expiryMonth": 12, "expiryYear": 2030 }'
```

Con ese ajuste la API debería responder 201 con el token generado, sin error 500.

```shellscript
curl -X POST http://localhost:8083/api/v1/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token JWT>" \
  -H "x-api-key: change-me-orders-api-key" \
  -d '{ "cardNumber": "4111111111111111", "cvv": "123", "expiryMonth": 12, "expiryYear": 2030 }'
```

---

**User**

ERROR org.springframework.boot.SpringApplication -- Application run failed

org.yaml.snakeyaml.scanner.ScannerException: while scanning a simple key

 in 'reader', line 34, column 1:

    eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI ... 

    ^

could not find expected ':'

 in 'reader', line 34, column 213:

     ... oLnu1UrbJfz2yZUILJuJedtFvOjvuUXo

                                         ^

---

**Cursor**

Ese error se debe a que el token JWT quedó pegado directamente en el YAML, y SnakeYAML lo interpreta como una clave sin “:”. El bloque `security.jwt` debe tener sólo las propiedades tipo `clave: valor`. Deja el archivo así:

```yaml
security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
    expiration-minutes: 120
```

El token nunca se guarda en la configuración; se usa únicamente a la hora de llamar a los endpoints («Authorization: Bearer …»). Borra la línea del token, guarda y vuelve a levantar el servicio.

```yaml
security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
    expiration-minutes: 120
```

---

**User**

perfecto, ahora en los 3 servicios porfa haz una tabla de logs donde se registre cada evento y transaccion que ocurra en los endpoints, cada uno con un UUID unico

---

**Cursor**

Para levantar esa tabla de logs necesito definir qué información quieres guardar en cada registro. ¿Basta con algo como:

- `id` (UUID)
- `timestamp`
- `service`
- `endpoint`
- `httpMethod`
- `statusCode`
- `message` / `payload`

¿o necesitas otros campos (por ejemplo usuario, cuerpo de la petición/respuesta, duración, etc.)? Con eso puedo preparar la entidad/repo y engancharla en los endpoints de los tres servicios.

---

**User**

si con eso basta, pero agregale el user que la ejecuta si tiene

---

**Cursor**

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "request_logs")
public class RequestLogEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false)
    private UUID id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "endpoint", nullable = false, length = 512)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "user_identifier", length = 128)
    private String userIdentifier;

    @Column(name = "message", length = 2048)
    private String message;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.RequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestLogRepository extends JpaRepository<RequestLogEntity, UUID> {
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.filter;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.RequestLogEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.RequestLogRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository requestLogRepository;
    private final String serviceName;
    private final SecretKey signingKey;

    public RequestLoggingFilter(RequestLogRepository requestLogRepository,
                                @Value("${spring.application.name:orders-service}") String serviceName,
                                @Value("${security.jwt.secret:}") String jwtSecret) {
        this.requestLogRepository = requestLogRepository;
        this.serviceName = serviceName;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            byte[] keyBytes = jwtSecret.length() % 4 == 0
                    ? Decoders.BASE64.decode(jwtSecret)
                    : jwtSecret.getBytes(StandardCharsets.UTF_8);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        } else {
            this.signingKey = null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestLogEntity log = new RequestLogEntity();
            log.setId(UUID.randomUUID());
            log.setTimestamp(LocalDateTime.now());
            log.setServiceName(serviceName);
            log.setEndpoint(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            log.setStatusCode(response.getStatus());
            log.setUserIdentifier(resolveUserIdentifier(request));
            log.setMessage(buildMessage(request));
            requestLogRepository.save(log);
        }
    }

    private String buildMessage(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null ? "query=" + query : "N/A";
    }

    private String resolveUserIdentifier(HttpServletRequest request) {
        String headerUser = request.getHeader("X-User-Id");
        if (headerUser != null && !headerUser.isBlank()) {
            return headerUser;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }

        if (signingKey == null) {
            return null;
        }

        try {
            String token = authorization.substring("Bearer ".length());
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();
            return Optional.ofNullable(claims.get("userId"))
                    .map(Object::toString)
                    .orElseGet(claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}


```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.filter;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.RequestLogEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.RequestLogRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository requestLogRepository;
    private final String serviceName;
    private final SecretKey signingKey;

    public RequestLoggingFilter(RequestLogRepository requestLogRepository,
                                @Value("${spring.application.name:orders-service}") String serviceName,
                                @Value("${security.jwt.secret:}") String jwtSecret) {
        this.requestLogRepository = requestLogRepository;
        this.serviceName = serviceName;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.signingKey = null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestLogEntity log = new RequestLogEntity();
            log.setId(UUID.randomUUID());
            log.setTimestamp(LocalDateTime.now());
            log.setServiceName(serviceName);
            log.setEndpoint(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            log.setStatusCode(response.getStatus());
            log.setUserIdentifier(resolveUserIdentifier(request));
            log.setMessage(buildMessage(request));
            requestLogRepository.save(log);
        }
    }

    private String buildMessage(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null ? "query=" + query : "N/A";
    }

    private String resolveUserIdentifier(HttpServletRequest request) {
        String headerUser = request.getHeader("X-User-Id");
        if (headerUser != null && !headerUser.isBlank()) {
            return headerUser;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }

        if (signingKey == null) {
            return null;
        }

        try {
            String token = authorization.substring("Bearer ".length());
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();
            return Optional.ofNullable(claims.get("userId"))
                    .map(Object::toString)
                    .orElseGet(claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}



```

```java
package com.tuempresa.ecommerce.orders.infrastructure.web.filter;

import com.tuempresa.ecommerce.orders.infrastructure.persistence.entity.RequestLogEntity;
import com.tuempresa.ecommerce.orders.infrastructure.persistence.repository.RequestLogRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository requestLogRepository;
    private final String serviceName;
    private final SecretKey signingKey;

    public RequestLoggingFilter(RequestLogRepository requestLogRepository,
                                @Value("${spring.application.name:orders-service}") String serviceName,
                                @Value("${security.jwt.secret:}") String jwtSecret) {
        this.requestLogRepository = requestLogRepository;
        this.serviceName = serviceName;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.signingKey = null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestLogEntity log = new RequestLogEntity();
            log.setId(UUID.randomUUID());
            log.setTimestamp(LocalDateTime.now());
            log.setServiceName(serviceName);
            log.setEndpoint(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            log.setStatusCode(response.getStatus());
            log.setUserIdentifier(resolveUserIdentifier(request));
            log.setMessage(buildMessage(request));
            requestLogRepository.save(log);
        }
    }

    private String buildMessage(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null ? "query=" + query : "N/A";
    }

    private String resolveUserIdentifier(HttpServletRequest request) {
        String headerUser = request.getHeader("X-User-Id");
        if (headerUser != null && !headerUser.isBlank()) {
            return headerUser;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }

        if (signingKey == null) {
            return null;
        }

        try {
            String token = authorization.substring("Bearer ".length());
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();
            return Optional.ofNullable(claims.get("userId"))
                    .map(Object::toString)
                    .orElseGet(claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}



```

```yaml
*** Begin Patch
*** Update File: services/orders-service/src/main/resources/application.yaml
@@
 spring:
+  application:
+    name: orders-service
   main:
     allow-bean-definition-overriding: true
*** End Patch
```

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "request_logs")
public class RequestLogEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false)
    private UUID id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "endpoint", nullable = false, length = 512)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "user_identifier", length = 128)
    private String userIdentifier;

    @Column(name = "message", length = 2048)
    private String message;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.RequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestLogRepository extends JpaRepository<RequestLogEntity, UUID> {
}


```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.filter;

import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.RequestLogEntity;
import com.tuempresa.ecommerce.users.infrastructure.persistence.repository.RequestLogRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository requestLogRepository;
    private final String serviceName;
    private final SecretKey signingKey;

    public RequestLoggingFilter(RequestLogRepository requestLogRepository,
                                @Value("${spring.application.name:users-service}") String serviceName,
                                @Value("${security.jwt.secret:}") String jwtSecret) {
        this.requestLogRepository = requestLogRepository;
        this.serviceName = serviceName;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.signingKey = null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestLogEntity log = new RequestLogEntity();
            log.setId(UUID.randomUUID());
            log.setTimestamp(LocalDateTime.now());
            log.setServiceName(serviceName);
            log.setEndpoint(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            log.setStatusCode(response.getStatus());
            log.setUserIdentifier(resolveUserIdentifier(request));
            log.setMessage(buildMessage(request));
            requestLogRepository.save(log);
        }
    }

    private String buildMessage(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null ? "query=" + query : "N/A";
    }

    private String resolveUserIdentifier(HttpServletRequest request) {
        String headerUser = request.getHeader("X-User-Id");
        if (headerUser != null && !headerUser.isBlank()) {
            return headerUser;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }

        if (signingKey == null) {
            return null;
        }

        try {
            String token = authorization.substring("Bearer ".length());
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();
            return Optional.ofNullable(claims.get("userId"))
                    .map(Object::toString)
                    .orElseGet(claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}


```

```yaml
server:
  port: 8082

spring:
  application:
    name: users-service
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:usersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

security:
  jwt:
    secret: change-me-jwt-secret-please-change
    issuer: ecommerce-simulator
    expiration-minutes: 120

```

```plaintext
plugins {
    `java`
    id("org.springframework.boot") version "3.5.7"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}



```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "request_logs")
public class RequestLogEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false)
    private UUID id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "endpoint", nullable = false, length = 512)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "user_identifier", length = 128)
    private String userIdentifier;

    @Column(name = "message", length = 2048)
    private String message;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.RequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestLogRepository extends JpaRepository<RequestLogEntity, UUID> {
}


```

```java
package com.tuempresa.ecommerce.catalog.infrastructure.web.filter;

import com.tuempresa.ecommerce.catalog.infrastructure.persistence.entity.RequestLogEntity;
import com.tuempresa.ecommerce.catalog.infrastructure.persistence.repository.RequestLogRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository requestLogRepository;
    private final String serviceName;
    private final SecretKey signingKey;

    public RequestLoggingFilter(RequestLogRepository requestLogRepository,
                                @Value("${spring.application.name:catalog-service}") String serviceName,
                                @Value("${security.jwt.secret:}") String jwtSecret) {
        this.requestLogRepository = requestLogRepository;
        this.serviceName = serviceName;
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.signingKey = null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestLogEntity log = new RequestLogEntity();
            log.setId(UUID.randomUUID());
            log.setTimestamp(LocalDateTime.now());
            log.setServiceName(serviceName);
            log.setEndpoint(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
            log.setStatusCode(response.getStatus());
            log.setUserIdentifier(resolveUserIdentifier(request));
            log.setMessage(buildMessage(request));
            requestLogRepository.save(log);
        }
    }

    private String buildMessage(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null ? "query=" + query : "N/A";
    }

    private String resolveUserIdentifier(HttpServletRequest request) {
        String headerUser = request.getHeader("X-User-Id");
        if (headerUser != null && !headerUser.isBlank()) {
            return headerUser;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }

        if (signingKey == null) {
            return null;
        }

        try {
            String token = authorization.substring("Bearer ".length());
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();
            return Optional.ofNullable(claims.get("userId"))
                    .map(Object::toString)
                    .orElseGet(claims::getSubject);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}


```

```yaml
server:
  port: 8081

spring:
  application:
    name: catalog-service
  main:
    allow-bean-definition-overriding: true
  mvc:
    servlet:
      path: /api/v1
  datasource:
    url: jdbc:h2:mem:catalogdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

```

## Resumen
- Se agregó una tabla `request_logs` en los tres microservicios (orders, users, catalog) con entidad JPA, repositorio y un filtro `RequestLoggingFilter` que registra automáticamente cada request con UUID, timestamp, servicio, endpoint, método, código de respuesta, usuario (si hay token o cabecera `X-User-Id`) y un mensaje básico.
```33:70:services/orders-service/src/main/java/com/tuempresa/ecommerce/orders/infrastructure/web/filter/RequestLoggingFilter.java
try {
    filterChain.doFilter(request, response);
} finally {
    RequestLogEntity log = new RequestLogEntity();
    log.setId(UUID.randomUUID());
    log.setTimestamp(LocalDateTime.now());
    log.setServiceName(serviceName);
    log.setEndpoint(request.getRequestURI());
    log.setHttpMethod(request.getMethod());
    log.setStatusCode(response.getStatus());
    log.setUserIdentifier(resolveUserIdentifier(request));
    log.setMessage(buildMessage(request));
    requestLogRepository.save(log);
}
```

- Cada servicio expone su nombre vía `spring.application.name` para que el registro lo identifique.
- En users-service y catalog-service se añadieron las dependencias de JJWT para poder extraer el usuario del JWT cuando esté presente.
- Se corrigió `CreditCardEntity` para permitir que `token_id` y `token_signature` se guarden después de generar el token, evitando errores 500 al registrar tarjetas.
- Se agregó un `ApiKeyValidator` y se reutiliza en los controladores de tarjetas y pagos para validar la cabecera `x-api-key`.

## Verificación
No se ejecutó `./gradlew :services:...:build` porque la sandbox no dispone de JDK; hazlo localmente cuando puedas para validar la compilación y generar la nueva tabla.

## Recomendaciones
- Ejecuta los servicios tras aplicar los cambios para que Hibernate cree la tabla `request_logs` en cada base H2.
- Repite los flujos de prueba (registro de tarjeta/pago) verificando que ya no hay errores 500 y que los registros aparecen en `request_logs`.
- Si necesitas más detalle en el campo `message`, puedes expandir el filtro (por ejemplo, capturando payloads o tiempos de respuesta).

```java
try {
    filterChain.doFilter(request, response);
} finally {
    RequestLogEntity log = new RequestLogEntity();
    log.setId(UUID.randomUUID());
    log.setTimestamp(LocalDateTime.now());
    log.setServiceName(serviceName);
    log.setEndpoint(request.getRequestURI());
    log.setHttpMethod(request.getMethod());
    log.setStatusCode(response.getStatus());
    log.setUserIdentifier(resolveUserIdentifier(request));
    log.setMessage(buildMessage(request));
    requestLogRepository.save(log);
}
```

