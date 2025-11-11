package com.tuempresa.ecommerce.users.config;

import com.tuempresa.ecommerce.users.entity.User;
import com.tuempresa.ecommerce.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("juan.perez@example.com", "Juan Pérez"));
                userRepository.save(new User("maria.garcia@example.com", "María García"));
                userRepository.save(new User("carlos.rodriguez@example.com", "Carlos Rodríguez"));
                userRepository.save(new User("ana.martinez@example.com", "Ana Martínez"));
                userRepository.save(new User("luis.lopez@example.com", "Luis López"));
            }
        };
    }
}

