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


