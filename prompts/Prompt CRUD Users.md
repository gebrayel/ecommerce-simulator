# Eliminar datos mock y habilitar CRUD usuarios
_Exported on 11/14/2025 at 08:17:32 GMT-4 from Cursor (2.0.69)_

---

**User**

elimina la data mock del servicio de users y habilita los endpoints CRUD para el usuario, considera que necesito solicitarle al usuario nombre, correo, telefono, direccion, y que email y telefono sean datos unicos en la BD

---

**Cursor**


Revisando la estructura del servicio de usuarios para eliminar la data mock y habilitar los endpoints CRUD.



Revisando si hay DTOs disponibles:



Implementando CRUD para usuarios: eliminando data mock, agregando campos requeridos y validando unicidad de email y teléfono.



Actualizando la entidad User para agregar telefono (único) y direccion:


```java
package com.tuempresa.ecommerce.users.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```

```java
package com.tuempresa.ecommerce.users.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String email, String name, String telefono, String direccion) {
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```

```java
package com.tuempresa.ecommerce.users.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String email, String name, String telefono, String direccion) {
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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


```


Eliminando la data mock del DataInitializer:



Agregando dependencia de validación y creando el servicio con validaciones:



Agregando la dependencia de validación y creando el servicio:


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


Agregando métodos al repositorio para validar unicidad y creando el servicio:


```java
package com.tuempresa.ecommerce.users.repository;

import com.tuempresa.ecommerce.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
}


```

```java
package com.tuempresa.ecommerce.users.service;

import com.tuempresa.ecommerce.users.entity.User;
import com.tuempresa.ecommerce.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        // Validar que email y telefono sean únicos
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }
        if (userRepository.existsByTelefono(user.getTelefono())) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }
        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }

        User existingUser = existingUserOpt.get();

        // Validar que email sea único (excepto el usuario actual)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }

        // Validar que telefono sea único (excepto el usuario actual)
        if (!existingUser.getTelefono().equals(user.getTelefono()) && 
            userRepository.existsByTelefonoAndIdNot(user.getTelefono(), id)) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }

        // Actualizar campos
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setTelefono(user.getTelefono());
        existingUser.setDireccion(user.getDireccion());

        return userRepository.save(existingUser);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
}

```


Actualizando el controlador para implementar los endpoints CRUD:


```java
package com.tuempresa.ecommerce.users.controller;

import com.tuempresa.ecommerce.users.entity.User;
import com.tuempresa.ecommerce.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.status(HttpStatus.OK).body(user))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.create(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            User updatedUser = userService.update(id, user);
            return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Clase interna para respuestas de error
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}


```


Agregando validaciones a la entidad User:



Agregando anotaciones de validación a la entidad User:


```java
package com.tuempresa.ecommerce.users.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String email, String name, String telefono, String direccion) {
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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


```

```java
package com.tuempresa.ecommerce.users.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Column(nullable = false)
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String email, String name, String telefono, String direccion) {
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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


```


Revisando errores de compilación y agregando un manejador de excepciones para validaciones:



Agregando un manejador de excepciones global para manejar errores de validación:


```java
package com.tuempresa.ecommerce.users.controller;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

```

```java
package com.tuempresa.ecommerce.users.controller;

import com.tuempresa.ecommerce.users.entity.User;
import com.tuempresa.ecommerce.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.status(HttpStatus.OK).body(user))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        User updatedUser = userService.update(id, user);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}


```


Verificando errores y actualizando el servicio para manejar mejor el caso de usuario no encontrado:



Revisando si el directorio config está vacío y si debemos eliminarlo:



Creando una excepción personalizada para recursos no encontrados y mejorando el manejo de excepciones:


```java
package com.tuempresa.ecommerce.users.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

```

```java
package com.tuempresa.ecommerce.users.service;

import com.tuempresa.ecommerce.users.entity.User;
import com.tuempresa.ecommerce.users.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        // Validar que email y telefono sean únicos
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }
        if (userRepository.existsByTelefono(user.getTelefono())) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }
        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }

        User existingUser = existingUserOpt.get();

        // Validar que email sea único (excepto el usuario actual)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }

        // Validar que telefono sea único (excepto el usuario actual)
        if (!existingUser.getTelefono().equals(user.getTelefono()) && 
            userRepository.existsByTelefonoAndIdNot(user.getTelefono(), id)) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }

        // Actualizar campos
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setTelefono(user.getTelefono());
        existingUser.setDireccion(user.getDireccion());

        return userRepository.save(existingUser);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
}


```

```java
package com.tuempresa.ecommerce.users.service;

import com.tuempresa.ecommerce.users.entity.User;
import com.tuempresa.ecommerce.users.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        // Validar que email y telefono sean únicos
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }
        if (userRepository.existsByTelefono(user.getTelefono())) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }
        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        User existingUser = existingUserOpt.get();

        // Validar que email sea único (excepto el usuario actual)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }

        // Validar que telefono sea único (excepto el usuario actual)
        if (!existingUser.getTelefono().equals(user.getTelefono()) && 
            userRepository.existsByTelefonoAndIdNot(user.getTelefono(), id)) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }

        // Actualizar campos
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setTelefono(user.getTelefono());
        existingUser.setDireccion(user.getDireccion());

        return userRepository.save(existingUser);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
}


```

```java
package com.tuempresa.ecommerce.users.service;

import com.tuempresa.ecommerce.users.entity.User;
import com.tuempresa.ecommerce.users.exception.ResourceNotFoundException;
import com.tuempresa.ecommerce.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        // Validar que email y telefono sean únicos
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }
        if (userRepository.existsByTelefono(user.getTelefono())) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }
        return userRepository.save(user);
    }

    public User update(Long id, User user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        User existingUser = existingUserOpt.get();

        // Validar que email sea único (excepto el usuario actual)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new IllegalArgumentException("El email ya está registrado: " + user.getEmail());
        }

        // Validar que telefono sea único (excepto el usuario actual)
        if (!existingUser.getTelefono().equals(user.getTelefono()) && 
            userRepository.existsByTelefonoAndIdNot(user.getTelefono(), id)) {
            throw new IllegalArgumentException("El teléfono ya está registrado: " + user.getTelefono());
        }

        // Actualizar campos
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setTelefono(user.getTelefono());
        existingUser.setDireccion(user.getDireccion());

        return userRepository.save(existingUser);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
}


```

```java
package com.tuempresa.ecommerce.users.controller;

import com.tuempresa.ecommerce.users.exception.ResourceNotFoundException;
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
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
}


```


Eliminando el import no usado en la entidad User:


```java
package com.tuempresa.ecommerce.users.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Column(nullable = false)
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {
    }

    public User(String email, String name, String telefono, String direccion) {
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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


```


Revisando los archivos finales para confirmar que todo esté correcto:



## Cambios realizados

1. Entidad User actualizada:
   - Campos: `nombre`, `correo`, `telefono`, `direccion`
   - Email y teléfono únicos en BD (`@Column(unique = true)`)
   - Validaciones con `@NotBlank` y `@Email`

2. Data mock eliminada:
   - Eliminado `DataInitializer.java` que creaba usuarios de prueba

3. Servicio creado:
   - `UserService` con lógica de negocio
   - Validación de unicidad de email y teléfono
   - Validación al crear y actualizar

4. Endpoints CRUD:
   - `GET /users` — Listar usuarios
   - `GET /users/{id}` — Obtener usuario por ID
   - `POST /users` — Crear usuario
   - `PUT /users/{id}` — Actualizar usuario
   - `DELETE /users/{id}` — Eliminar usuario

5. Manejo de excepciones:
   - `GlobalExceptionHandler` para validaciones y errores
   - `ResourceNotFoundException` para recursos no encontrados (404)
   - Errores de validación devuelven 400 con detalles

6. Dependencias:
   - Agregada `spring-boot-starter-validation`

## Ejemplo de uso

**Crear usuario:**
```json
POST /users
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "telefono": "123456789",
  "direccion": "Calle 123"
}
```

**Actualizar usuario:**
```json
PUT /users/1
{
  "name": "Juan Pérez",
  "email": "juan.nuevo@example.com",
  "telefono": "987654321",
  "direccion": "Nueva dirección"
}
```

Listo. El servicio valida que email y teléfono sean únicos y maneja errores de validación.

```json
POST /users
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "telefono": "123456789",
  "direccion": "Calle 123"
}
```

```json
PUT /users/1
{
  "name": "Juan Pérez",
  "email": "juan.nuevo@example.com",
  "telefono": "987654321",
  "direccion": "Nueva dirección"
}
```

---

**User**

puedes ajustarme tal cual lo que está en el servicio users siguiendo una arquitectura hexagonal?

---

**Cursor**


Reestructurando el servicio de users a arquitectura hexagonal (puertos y adaptadores). Revisando la estructura actual y reorganizando en dominio, aplicación e infraestructura.



Reestructurando el servicio a arquitectura hexagonal (puertos y adaptadores). Plan:



Creando la estructura de dominio:


```java
package com.tuempresa.ecommerce.users.domain.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String email;
    private String name;
    private String telefono;
    private String direccion;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long id, String email, String name, String telefono, String direccion, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
        this.createdAt = createdAt;
    }

    public User(String email, String name, String telefono, String direccion) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

```

```java
package com.tuempresa.ecommerce.users.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

```

```java
package com.tuempresa.ecommerce.users.domain.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

```

```java
package com.tuempresa.ecommerce.users.domain.port.in;

import com.tuempresa.ecommerce.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserUseCase {
    List<User> findAll();
    Optional<User> findById(Long id);
    User create(User user);
    User update(Long id, User user);
    void delete(Long id);
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
    User save(User user);
    void deleteById(Long id);
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
}

```


Creando la capa de aplicación: implementación de casos de uso.


```java
package com.tuempresa.ecommerce.users.application.service;

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

    public UserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
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


Creando la capa de infraestructura: entidad JPA, repositorio y adaptador.


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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UserEntity() {
    }

    public UserEntity(Long id, String email, String name, String telefono, String direccion, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

```

```java
package com.tuempresa.ecommerce.users.infrastructure.persistence.repository;

import com.tuempresa.ecommerce.users.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByTelefono(String telefono);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
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
            domain.getCreatedAt()
        );
        return entity;
    }
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


Creando DTOs y el controlador REST en la capa de infraestructura.


```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    public UserRequest() {
    }

    public UserRequest(String name, String email, String telefono, String direccion) {
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
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
}

```

```java
package com.tuempresa.ecommerce.users.infrastructure.web.dto;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, String telefono, String direccion, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
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
        return new User(
            request.getEmail(),
            request.getName(),
            request.getTelefono(),
            request.getDireccion()
        );
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

import com.tuempresa.ecommerce.users.domain.exception.DuplicateResourceException;
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
}

```


Eliminando archivos antiguos y actualizando la aplicación principal.



Verificando errores y actualizando la configuración si es necesario:



Verificando la estructura y que los componentes estén correctamente configurados:



Eliminando directorios vacíos y verificando la estructura final:



Eliminando directorios vacíos:



Verificando que el modelo de dominio maneje correctamente el createdAt al crear usuarios:



Revisando el mapper de persistencia para verificar el manejo de createdAt:



Ajustando el `@PrePersist` para establecer `createdAt` solo si es null:


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

    public UserEntity(Long id, String email, String name, String telefono, String direccion, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.telefono = telefono;
        this.direccion = direccion;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


```


Verificando la estructura final y preparando un resumen:



Reestructuración del servicio de usuarios a arquitectura hexagonal completada. Resumen:

## Arquitectura hexagonal implementada

### Estructura de capas

```
users-service/
├── domain/                          # Capa de Dominio (Núcleo)
│   ├── model/
│   │   └── User.java                # Modelo de dominio puro (sin dependencias externas)
│   ├── port/
│   │   ├── in/
│   │   │   └── UserUseCase.java     # Puerto de entrada (interfaz de casos de uso)
│   │   └── out/
│   │       └── UserRepositoryPort.java  # Puerto de salida (interfaz de repositorio)
│   └── exception/
│       ├── ResourceNotFoundException.java
│       └── DuplicateResourceException.java
│
├── application/                     # Capa de Aplicación
│   └── service/
│       └── UserService.java         # Implementación de casos de uso
│
└── infrastructure/                  # Capa de Infraestructura
    ├── persistence/                 # Adaptadores de persistencia
    │   ├── entity/
    │   │   └── UserEntity.java      # Entidad JPA
    │   ├── repository/
    │   │   └── UserJpaRepository.java  # Repositorio JPA
    │   ├── mapper/
    │   │   └── UserEntityMapper.java   # Mapper dominio ↔ entidad
    │   └── adapter/
    │       └── UserRepositoryAdapter.java  # Adaptador que implementa UserRepositoryPort
    │
    └── web/                         # Adaptadores de entrada (REST)
        ├── controller/
        │   ├── UserController.java      # Controlador REST
        │   └── GlobalExceptionHandler.java
        ├── dto/
        │   ├── UserRequest.java         # DTO de entrada
        │   └── UserResponse.java        # DTO de salida
        └── mapper/
            └── UserWebMapper.java       # Mapper DTO ↔ dominio
```

### Principios aplicados

1. Separación de responsabilidades
   - Dominio: lógica de negocio sin dependencias externas
   - Aplicación: orquestación de casos de uso
   - Infraestructura: detalles técnicos (JPA, REST, etc.)

2. Inversión de dependencias
   - El dominio define puertos (interfaces)
   - La infraestructura implementa los puertos (adaptadores)
   - La aplicación depende de interfaces, no de implementaciones

3. Independencia del framework
   - El modelo de dominio no tiene anotaciones JPA
   - La lógica de negocio no depende de Spring

4. Testabilidad
   - Fácil crear mocks de los puertos
   - El dominio se puede probar sin infraestructura

### Flujo de datos

```
HTTP Request
    ↓
UserController (Infrastructure/Web)
    ↓
UserRequest DTO → UserWebMapper → User (Domain)
    ↓
UserUseCase (Application) → UserService
    ↓
UserRepositoryPort (Domain/Port) → UserRepositoryAdapter (Infrastructure)
    ↓
UserJpaRepository → UserEntity (JPA)
    ↓
Database
```

### Características

- Endpoints CRUD
- Validaciones de dominio (email y teléfono únicos)
- Validaciones de entrada con Jakarta Validation
- Manejo de excepciones centralizado
- Mappers para separar capas
- Sin datos mock (DataInitializer eliminado)

El código queda organizado, mantenible y testeable, siguiendo principios de arquitectura hexagonal.

```plaintext
users-service/
├── domain/                          # Capa de Dominio (Núcleo)
│   ├── model/
│   │   └── User.java                # Modelo de dominio puro (sin dependencias externas)
│   ├── port/
│   │   ├── in/
│   │   │   └── UserUseCase.java     # Puerto de entrada (interfaz de casos de uso)
│   │   └── out/
│   │       └── UserRepositoryPort.java  # Puerto de salida (interfaz de repositorio)
│   └── exception/
│       ├── ResourceNotFoundException.java
│       └── DuplicateResourceException.java
│
├── application/                     # Capa de Aplicación
│   └── service/
│       └── UserService.java         # Implementación de casos de uso
│
└── infrastructure/                  # Capa de Infraestructura
    ├── persistence/                 # Adaptadores de persistencia
    │   ├── entity/
    │   │   └── UserEntity.java      # Entidad JPA
    │   ├── repository/
    │   │   └── UserJpaRepository.java  # Repositorio JPA
    │   ├── mapper/
    │   │   └── UserEntityMapper.java   # Mapper dominio ↔ entidad
    │   └── adapter/
    │       └── UserRepositoryAdapter.java  # Adaptador que implementa UserRepositoryPort
    │
    └── web/                         # Adaptadores de entrada (REST)
        ├── controller/
        │   ├── UserController.java      # Controlador REST
        │   └── GlobalExceptionHandler.java
        ├── dto/
        │   ├── UserRequest.java         # DTO de entrada
        │   └── UserResponse.java        # DTO de salida
        └── mapper/
            └── UserWebMapper.java       # Mapper DTO ↔ dominio
```

```plaintext
HTTP Request
    ↓
UserController (Infrastructure/Web)
    ↓
UserRequest DTO → UserWebMapper → User (Domain)
    ↓
UserUseCase (Application) → UserService
    ↓
UserRepositoryPort (Domain/Port) → UserRepositoryAdapter (Infrastructure)
    ↓
UserJpaRepository → UserEntity (JPA)
    ↓
Database
```

---

**User**

agrega un endpoint ping que retorne 200 y el mensaje pong para verificar la disponibilidad del servicio

---

**Cursor**

```java
package com.tuempresa.ecommerce.users.infrastructure.web.controller;

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

## Ping Endpoint Added

- Introduced `HealthController` under `infrastructure/web/controller` with `GET /ping`.
- Returns HTTP 200 and body `pong`, letting you check service availability without touching user flows.
- No other components touched; existing architecture remains unchanged.

Run the service and hit `GET http://<host>:8082/ping` to verify it responds with `pong`.

---

**User**

agrega una validacion para el numero de telefono

---

**Cursor**

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

    public UserRequest() {
    }

    public UserRequest(String name, String email, String telefono, String direccion) {
        this.name = name;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
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
}


```

- `UserRequest` ahora exige que `telefono` cumpla `^\\+?[0-9]{7,15}$`; acepta dígitos con un “+” opcional al inicio y longitud 7–15.  
- Mensaje de error descriptivo en español.  
- Sin cambios en resto de capas; validación se aplica en creación y actualización vía controller.

